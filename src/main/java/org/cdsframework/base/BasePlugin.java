/**
 * The MTS core EJB project is the base framework for the CDS Framework Middle Tier Service.
 *
 * Copyright (C) 2016 New York City Department of Health and Mental Hygiene, Bureau of Immunization
 * Contributions by HLN Consulting, LLC
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. You should have received a copy of the GNU Lesser
 * General Public License along with this program. If not, see <http://www.gnu.org/licenses/> for more
 * details.
 *
 * The above-named contributors (HLN Consulting, LLC) are also licensed by the New York City
 * Department of Health and Mental Hygiene, Bureau of Immunization to have (without restriction,
 * limitation, and warranty) complete irrevocable access and rights to this project.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; THE
 * SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING,
 * BUT NOT LIMITED TO, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE COPYRIGHT HOLDERS, IF ANY, OR DEVELOPERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES, OR OTHER LIABILITY OF ANY KIND, ARISING FROM, OUT OF, OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information about this software, see https://www.hln.com/services/open-source/ or send
 * correspondence to ice@hln.com.
 */
package org.cdsframework.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import org.cdsframework.annotation.JndiReference;
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.dto.SystemPropertyDTO;
import org.cdsframework.dto.UserDTO;
import org.cdsframework.dto.UserPreferenceDTO;
import org.cdsframework.ejb.bo.UserBO;
import org.cdsframework.ejb.dao.SystemPropertyDAO;
import org.cdsframework.ejb.local.DbMGRLocal;
import org.cdsframework.ejb.local.PropertyMGRLocal;
import org.cdsframework.enumeration.DTOState;
import org.cdsframework.exceptions.AuthenticationException;
import org.cdsframework.exceptions.AuthorizationException;
import org.cdsframework.exceptions.ConstraintViolationException;
import org.cdsframework.exceptions.MtsException;
import org.cdsframework.exceptions.NotFoundException;
import org.cdsframework.exceptions.ValidationException;
import org.cdsframework.group.Add;
import org.cdsframework.util.AuthenticationUtils;
import org.cdsframework.util.ClassUtils;
import org.cdsframework.util.Constants;
import org.cdsframework.util.DTOUtils;
import static org.cdsframework.util.DTOUtils.getJndiReference;
import org.cdsframework.util.EJBUtils;
import org.cdsframework.util.LogUtils;
import org.cdsframework.util.StringUtils;

/**
 *
 * @author HLN Consulting, LLC
 */
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public abstract class BasePlugin {

    protected LogUtils logger;
    @EJB
    private PropertyMGRLocal propertyMGRLocal;
    @EJB
    private SystemPropertyDAO systemPropertyDAO;
    @EJB
    private DbMGRLocal dbMGRLocal;
    private final String pluginName;
    @Resource(name = "DB_RESOURCES")
    private String DB_RESOURCES;
    private final List<UserPreferenceDTO> pluginUserPreferences = new ArrayList<UserPreferenceDTO>();

    protected void registerUserPreference(UserPreferenceDTO userPreferenceDTO) {
        pluginUserPreferences.add(userPreferenceDTO);
    }

    public BasePlugin() {
        Class<? extends BasePlugin> pluginClass = getClass();
        logger = LogUtils.getLogger(pluginClass);
        String[] items = pluginClass.getSimpleName().split("Plugin");
        if (items.length == 0) {
            throw new IllegalStateException("Invalid plugin package name: " + pluginClass.getCanonicalName());
        }
        pluginName = items[0].toLowerCase();
        logger.info("Found plugin: ", pluginName);
    }

    @PostConstruct
    public void postConstructor() {
        logger.info("Initializing plugin: ", pluginName);
        try {
            preProcess();

            String fullDbResourceString = getDbResourceString();
            if (fullDbResourceString != null) {
                Constants.loadDbResources(fullDbResourceString);
            }
            if ("core".equalsIgnoreCase(pluginName)) {
                loadPropertiesfromDb("*");
            }
            loadPropertiesfromDb(pluginName);
            initializeEjbJarEntries();
            initializeDb();
            postProcess();
            Constants.INSTALLED_PLUGINS.add(pluginName);
            registerUserPreferences();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        logger.info(String.format("Completed %s plugin initialization", pluginName));
    }

    /**
     * Iterate over the ejb-jar.xml env entries and add them to the system property table if they don't already exist. Also Register
     * the properties with the Property Manager.
     *
     * @throws NamingException
     * @throws MtsException
     */
    private void initializeEjbJarEntries() throws NamingException, MtsException {
        final String METHODNAME = "initializeEjbJarEntries ";
        Context c = new InitialContext();
        String namingContext = "java:comp/env";
        NamingEnumeration<Binding> listBindings = c.listBindings(namingContext);
        logger.info(METHODNAME, "initializing!");
        while (listBindings.hasMore()) {
            Binding next = listBindings.next();
            String key = next.getName().replaceFirst(namingContext + "/", "");
            Object value = next.getObject();
            logger.debug(METHODNAME, "key: ", key, "; value: ", value);
            if (!key.toUpperCase().startsWith("ORG.CDSFRAMEWORK") && !"DB_RESOURCES".equals(key)) {
                logger.debug("FOUND java:comp/env: ", next.getName(), " (", key, ") - ", next.getClassName(), " - ", value, " - ", value.getClass());
                // add it to the system property table for user override
                SystemPropertyDTO systemPropertyDTO = new SystemPropertyDTO();
                systemPropertyDTO.setName(key);
                systemPropertyDTO.setObscure(false);
                systemPropertyDTO.setType(value.getClass().getCanonicalName());
                systemPropertyDTO.setScope(pluginName);
                systemPropertyDTO.setMtsOnly(true);
                systemPropertyDTO.setValue(value.toString());
                systemPropertyDTO.setPropertyId(StringUtils.getHashId());
                systemPropertyDTO.setCreateDatetime(new Date());
                systemPropertyDTO.setLastModDatetime(new Date());
                systemPropertyDTO.setLastModId("INTERNAL");
                systemPropertyDTO.setCreateId("INTERNAL");
                try {
                    try {
                        systemPropertyDAO.findByQuery(systemPropertyDTO, SystemPropertyDTO.ByNameScope.class, AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
                        logger.debug(METHODNAME,
                                "MTS property already exists in the database: ",
                                key,
                                " - new: ", value,
                                " - old: ", propertyMGRLocal.get(key));
                    } catch (NotFoundException nfe) {
                        logger.info(METHODNAME, "Creating new property in db: ", key, "; value: ", value);
                        DTOUtils.setDTOState(systemPropertyDTO, DTOState.NEW);
                        systemPropertyDAO.addSystemPropertyInNewTransaction(systemPropertyDTO, Add.class, AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
                        propertyMGRLocal.registerProperty(key, value);
                    }
                } catch (ConstraintViolationException e) {
                    logger.error(METHODNAME, e);
                } catch (NotFoundException e) {
                    logger.error(METHODNAME, e);
                }
            }
        }
    }

    /**
     * load all properties from the system_property table into the property manager
     *
     * @param scope the plugin scope of the properties
     * @throws NotFoundException
     * @throws MtsException
     * @throws ValidationException
     * @throws AuthenticationException
     * @throws AuthorizationException
     */
    private void loadPropertiesfromDb(String scope) throws NotFoundException, MtsException, ValidationException,
            AuthenticationException, AuthorizationException {
        SystemPropertyDTO systemPropertyDTO = new SystemPropertyDTO();
        systemPropertyDTO.setScope(scope);
        List<SystemPropertyDTO> systemProperties = systemPropertyDAO.findByQueryList(
                systemPropertyDTO,
                SystemPropertyDTO.ByScope.class,
                AuthenticationUtils.getInternalSessionDTO(),
                new PropertyBagDTO());
        for (SystemPropertyDTO item : systemProperties) {
            Class<?> propertyType = null;
            try {
                propertyType = Class.forName(item.getType());
                propertyMGRLocal.registerPropertyWithConversion(item.getName(), item.getValue(), propertyType, item.isObscure());
            } catch (Exception e) {
                logger.error("failed to initialize property: ",
                        item.getPropertyId(),
                        " - ", item.getName(),
                        " - ", item.getValue(),
                        " - ", item.getGroup(),
                        " - ", item.getScope(),
                        " - ", item.getType(),
                        " - ", propertyType);
            }
        }
    }

    /**
     * Registers an application server System property in the property managers map
     *
     * @param systemPropertyName the System property name
     * @param type the type to cast the value to
     * @throws MtsException
     * @throws org.cdsframework.exceptions.ValidationException
     */
    protected void registerSystemProperty(String systemPropertyName, Class type) throws MtsException, ValidationException {
        registerSystemProperty(systemPropertyName, systemPropertyName, type, false);
    }

    /**
     * Registers an application server System property in the property managers map
     *
     * @param systemPropertyName the System property name
     * @param type the type to cast the value to
     * @param obscureLogValue in case you don't want the value to show up in the log (i.e. passwords).
     * @throws MtsException
     * @throws org.cdsframework.exceptions.ValidationException
     */
    protected void registerSystemProperty(String systemPropertyName, Class type, boolean obscureLogValue) throws MtsException, ValidationException {
        registerSystemProperty(systemPropertyName, systemPropertyName, type, obscureLogValue);
    }

    /**
     * Registers an application server System property in the property managers map
     *
     * @param systemPropertyName the System property name
     * @param mtsPropertyName the property name in the property map
     * @param type the type to cast the value to
     * @param obscureLogValue in case you don't want the value to show up in the log (i.e. passwords).
     * @throws MtsException
     * @throws org.cdsframework.exceptions.ValidationException
     */
    protected void registerSystemProperty(String systemPropertyName, String mtsPropertyName, Class type, boolean obscureLogValue)
            throws MtsException, ValidationException {
        final String METHODNAME = "registerPropertyFromFile ";
        if (systemPropertyName == null) {
            throw new MtsException(METHODNAME + "file property name was null!");
        }
        if (mtsPropertyName == null) {
            throw new MtsException(METHODNAME + "MTS property name was null!");
        }
        if (type == null) {
            throw new MtsException(METHODNAME + "MTS property type was null!");
        }
        boolean exists = false;
        String systemProperty = System.getProperty(systemPropertyName);
        if (systemProperty != null) {
            exists = true;
        }
        if (exists) {
            propertyMGRLocal.registerPropertyWithConversion(mtsPropertyName, systemProperty, type, obscureLogValue);
        } else {
            logger.error(METHODNAME, "property does not exist in the System properties: ", systemPropertyName, ". not registering property: ", mtsPropertyName);
        }
    }

    /**
     * Registers a property in the property managers map
     *
     * @param mtsPropertyName the property name in the property map
     * @param mtsPropertyValue the property value
     */
    protected void registerProperty(String mtsPropertyName, Object mtsPropertyValue) {
        propertyMGRLocal.registerProperty(mtsPropertyName, mtsPropertyValue, false);
    }

    /**
     * Registers a property in the property managers map
     *
     * @param mtsPropertyName the property name in the property map
     * @param mtsPropertyValue the property value
     * @param obscureLogValue in case you don't want the value to show up in the log (i.e. passwords).
     */
    protected void registerProperty(String mtsPropertyName, Object mtsPropertyValue, boolean obscureLogValue) {
        propertyMGRLocal.put(mtsPropertyName, mtsPropertyValue);
    }

    protected void initializeDb() throws NotFoundException, MtsException, ConstraintViolationException {
    }

    /**
     * For plugin module override
     *
     * @throws MtsException
     */
    protected void postProcess() throws MtsException {
    }

    /**
     * For plugin module override
     *
     * @throws MtsException
     */
    protected void preProcess() throws MtsException {
    }

    protected <S extends BaseDTO> void initializeDbTables(Class<S>... dtoClasses) throws MtsException {
        for (Class<S> dtoClass : dtoClasses) {
            dbMGRLocal.initializePersistenceMechanism(dtoClass);
        }
    }

    private void registerUserPreferences() throws MtsException {
        if (!pluginUserPreferences.isEmpty()) {
            UserBO userBO = (UserBO) EJBUtils.getDtoBo(UserDTO.class);
            for (UserPreferenceDTO userPreferenceDTO : pluginUserPreferences) {
                userBO.registerUserPreference(userPreferenceDTO);
            }
        }
    }

    private String getDbResourceString() {
        final String METHODNAME = "getDbResourceString ";
        String result = null;
        logger.info(METHODNAME, "DB_RESOURCES=", DB_RESOURCES);

        if (DB_RESOURCES == null) {
            logger.warn(METHODNAME, "DB_RESOURCES is null - skipping db resource initialization.");
        } else {
            String[] dbResources = DB_RESOURCES.split(",");
            logger.info(METHODNAME + "dbResources.length=" + dbResources.length);
            for (String dbResource : dbResources) {
                String[] resourceItems = dbResource.split("\\|");
                if (resourceItems.length > 1) {
                    String resourceId = resourceItems[0].trim().toUpperCase();
                    logger.info(METHODNAME + "resourceId: " + resourceId);
                    String jndiName = resourceItems[1].trim();
                    logger.info(METHODNAME + "jndiName: " + jndiName);
                    String connectedDatabaseInfo = dbMGRLocal.initializeDatabaseResource(resourceId, jndiName);
                    logger.info(METHODNAME, "connectedDatabaseInfo: ", connectedDatabaseInfo);
                    if (connectedDatabaseInfo != null) {
                        String dbResourceFormat = String.format("%s|%s|%s", resourceId, jndiName, connectedDatabaseInfo);
                        if (!StringUtils.isEmpty(result)) {
                            result += "," + dbResourceFormat;
                        } else {
                            result = dbResourceFormat;
                        }
                    } else {
                        logger.error(METHODNAME, "schema or database type not derrived - skipping db resource initialization: ", DB_RESOURCES);
                    }
                } else {
                    logger.error(METHODNAME, "DB_RESOURCES is malformed - skipping db resource initialization: ", DB_RESOURCES);
                }
            }
            logger.info(METHODNAME + "result: " + result);
        }
        return result;
    }
}
