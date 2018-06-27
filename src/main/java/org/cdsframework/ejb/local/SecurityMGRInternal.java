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
package org.cdsframework.ejb.local;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import org.cdsframework.base.BaseDAO;
import org.cdsframework.base.BaseDTO;
import org.cdsframework.base.BaseSecurityInterface;
import org.cdsframework.base.BaseSecurityMGR;
import org.cdsframework.dto.AppDTO;
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.dto.SessionDTO;
import org.cdsframework.dto.UserDTO;
import org.cdsframework.dto.UserSecurityMapDTO;
import org.cdsframework.security.UserSecuritySchemePermissionMap;
import org.cdsframework.ejb.bo.AppBO;
import org.cdsframework.ejb.bo.SessionBO;
import org.cdsframework.ejb.bo.UserBO;
import org.cdsframework.enumeration.ExceptionReason;
import org.cdsframework.enumeration.LogLevel;
import org.cdsframework.enumeration.PermissionType;
import org.cdsframework.exceptions.AuthenticationException;
import org.cdsframework.exceptions.AuthorizationException;
import org.cdsframework.exceptions.MtsException;
import org.cdsframework.exceptions.ConstraintViolationException;
import org.cdsframework.exceptions.NotFoundException;
import org.cdsframework.exceptions.ValidationException;
import org.cdsframework.group.Add;
import org.cdsframework.group.Delete;
import org.cdsframework.group.Update;
import org.cdsframework.util.AuthenticationUtils;
import org.cdsframework.util.Constants;
import org.cdsframework.util.DTOUtils;
import org.cdsframework.util.EJBUtils;
import org.cdsframework.util.LogUtils;
import org.cdsframework.util.PasswordHash;
import org.cdsframework.util.StringUtils;

/**
 * Provides local security manger functions.
 *
 * @author HLN Consulting, LLC
 */
@Stateless
@LocalBean
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class SecurityMGRInternal implements BaseSecurityMGR {

    private final static LogUtils logger = LogUtils.getLogger(SecurityMGRInternal.class);
    @EJB
    private AppBO appBO;
    @EJB
    private UserBO userBO;
    @EJB
    private PropertyMGRLocal propertyMGRLocal;
    @EJB
    private SessionBO sessionBO;
    @EJB
    private UserSecurityMGRLocal userSecurityMGRLocal;
    private static final List<Class> defaultChildClassList = new ArrayList<Class>();

    /**
     * Default no arg constructor initializes the superclass.
     */
    public SecurityMGRInternal() {
        defaultChildClassList.add(UserSecurityMapDTO.class);
    }

    @PostConstruct
    public void postConstructor() {
    }

    @Override
    public void registerApplicationProxy(Class proxyClass)
            throws MtsException {
        final String METHODNAME = "registerApplicationProxy ";
        if (proxyClass == null) {
            throw new MtsException(METHODNAME + "proxyClass cannot be null!");
        }
        String simpleName = proxyClass.getSimpleName();
        logger.debug("Adding ", simpleName, " to applicationProxyRegistrations map: ", proxyClass);
        Map<String, Class> applicationProxyRegistrations = (Map<String, Class>) propertyMGRLocal.get("APPLICATION_PROXY_REGISTRATIONS");
        applicationProxyRegistrations.put(simpleName, proxyClass);
    }

    /**
     * Evaluates whether a cachedUserDTO has the authority to make a particular
     * request. Returns whether result should cascade.
     *
     * @param permissionType
     * @param dtoClass
     * @param sessionDTO
     * @param propertyBagDTO
     * @throws AuthenticationException
     * @throws AuthorizationException
     * @throws MtsException
     * @throws NotFoundException
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public void checkAuthority(PermissionType permissionType, Class<? extends BaseDTO> dtoClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws AuthenticationException, AuthorizationException, MtsException, NotFoundException {
        final String METHODNAME = "checkAuthority ";
        long start = System.nanoTime();

        // we only want to reload the session objects UserDT once per process execution
        Boolean checkAuthSessionReloaded = propertyBagDTO.get("checkAuthSessionReloaded", Boolean.class);
        if (checkAuthSessionReloaded == null) {
            checkAuthSessionReloaded = false;
            propertyBagDTO.put("checkAuthSessionReloaded", false);
        }

        // we only want to set the new last mod datetime once per process execution
        Boolean checkAuthLastModSet = propertyBagDTO.get("checkAuthLastModSet", Boolean.class);
        if (checkAuthLastModSet == null) {
            checkAuthLastModSet = false;
            propertyBagDTO.put("checkAuthLastModSet", false);
        }

        // initialize checkAuthDuration to track cumulative duration of check authority calls - debug only
        Double checkAuthDuration = 0.0;
        if (logger.isDebugEnabled()) {
            checkAuthDuration = propertyBagDTO.get("checkAuthDuration", Double.class);
            if (checkAuthDuration == null) {
                checkAuthDuration = 0.0;
                propertyBagDTO.put("checkAuthDuration", checkAuthDuration);
            }
        }

        // map to track what has been checked so far in this process execution
        // - short circuit if we have already checked this permission
        Map<Class, List<PermissionType>> checkAuthMap = propertyBagDTO.get("checkAuthMap", Map.class);
        if (checkAuthMap == null) {
            checkAuthMap = new HashMap<Class, List<PermissionType>>();
            propertyBagDTO.put("checkAuthMap", checkAuthMap);
        }
        if (checkAuthMap.containsKey(dtoClass) && checkAuthMap.get(dtoClass).contains(permissionType)) {
            if (logger.isDebugEnabled()) {
                logger.debug(METHODNAME, "returning - ", dtoClass, "/", permissionType, " has already been checked.");
                logger.logDuration(LogLevel.DEBUG, METHODNAME, start);
                checkAuthDuration += (System.nanoTime() - start) / 1000000.0;
                propertyBagDTO.put("checkAuthDuration", checkAuthDuration);
                logger.debug(METHODNAME, propertyBagDTO.get("checkAuthSrc"), " - ", permissionType, " cumulative duration(ms): " + checkAuthDuration);
            }
            return;
        }
        List<PermissionType> checkAuthClassPermList = checkAuthMap.get(dtoClass);
        if (checkAuthClassPermList == null) {
            checkAuthClassPermList = new ArrayList<PermissionType>();
            checkAuthMap.put(dtoClass, checkAuthClassPermList);
        }

        // finally - begin the checking
        boolean internalSession;
        SessionDTO trueSessionDTO = null;
        if (sessionDTO == null) {
            throw new AuthenticationException(logger.error("Session was null."), ExceptionReason.MISSING_ENTRY);
        }
        if (dtoClass == null) {
            throw new MtsException(logger.error(METHODNAME, "dto parameter was null"));
        }
        // Determine if the sessionDTO is internal
        internalSession = AuthenticationUtils.isInternalSession(sessionDTO) || AuthenticationUtils.isInternalApp(sessionDTO.getAppDTO());
        logger.debug(METHODNAME, "internalSession=", internalSession);
        // Internal excludes this logic
        try {
            if (!internalSession) {
//                long findSessionStart = System.nanoTime();
                if (!checkAuthSessionReloaded) {
                    try {
                        if (logger.isDebugEnabled()) {
                            logger.debug(METHODNAME, "sessionDTO.getSessionId()=", sessionDTO.getSessionId());
                            logger.debug(METHODNAME, "sessionDTO.getUserDTO()=", sessionDTO.getUserDTO());
                            logger.debug(METHODNAME, "sessionDTO.getUserDTO().getUserId()=", sessionDTO.getUserDTO().getUserId());
                            logger.debug(METHODNAME, "sessionDTO.getUserDTO().getUsername()=", sessionDTO.getUserDTO().getUsername());
                            logger.debug(METHODNAME, "sessionDTO.getProxyUserDTO()=", sessionDTO.getProxyUserDTO());
                        }

                        trueSessionDTO = sessionBO.findByPrimaryKeyMain(sessionDTO, defaultChildClassList, AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
                        propertyBagDTO.put("checkAuthSessionReloaded", true);

                        logger.debug(METHODNAME, "About to call Security Map Implementations");
                        Map<String, BaseSecurityInterface> securityImpl = userSecurityMGRLocal.getSecurityImpl();
                        for (Map.Entry<String, BaseSecurityInterface> mapEntry : securityImpl.entrySet()) {
                            mapEntry.getValue().processSession(trueSessionDTO);
                        }
                    } catch (ValidationException e) {
                        logger.error(e);
                        throw new MtsException(e.getMessage());
                    } catch (NotFoundException e) {
                        logger.warn(METHODNAME,
                                "sesson not found: ", sessionDTO.getSessionId(),
                                " app: ", sessionDTO.getAppDTO() != null ? sessionDTO.getAppDTO().getAppName() : null,
                                " user: ", sessionDTO.getUserDTO() != null ? sessionDTO.getUserDTO().getUsername() : null);
                    }
                } else {
                    trueSessionDTO = sessionDTO;
                }
//                logger.logDuration(METHODNAME + "findSessionStart ", findSessionStart);
                if (trueSessionDTO == null) {
                    logger.error(METHODNAME, "session missing: ", sessionDTO.getSessionId());
                    throw new AuthenticationException("Session missing.", ExceptionReason.MISSING_ENTRY);
                }
                UserDTO userDTO = trueSessionDTO.getUserDTO();
                if (userDTO == null) {
                    throw new AuthenticationException("User missing from session.", ExceptionReason.MISSING_ENTRY);
                }
                logger.debug(
                        METHODNAME,
                        " called on session: ", trueSessionDTO.getSessionId(),
                        " for user ", userDTO.getUsername(),
                        " for dtoType ", dtoClass.getSimpleName(),
                        " for permission type ", permissionType);
//                long findProxyUserStart = System.nanoTime();
                if (!checkAuthSessionReloaded && trueSessionDTO.isProxy()
                        && (userDTO.getUsername() == null
                        || userDTO.getUsername().trim().isEmpty())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(METHODNAME, "sessionDTO.getSessionId()=", sessionDTO.getSessionId());
                        logger.debug(METHODNAME, "sessionDTO.isProxy()=", sessionDTO.isProxy());
                        logger.debug(METHODNAME, "sessionDTO.getUserDTO()=", sessionDTO.getUserDTO());
                        logger.debug(METHODNAME, "sessionDTO.getUserDTO().getUserId()=", sessionDTO.getUserDTO().getUserId());
                        logger.debug(METHODNAME, "sessionDTO.getUserDTO().getUsername()=", sessionDTO.getUserDTO().getUsername());
                        logger.debug(METHODNAME, "sessionDTO.getProxyUserDTO()=", sessionDTO.getProxyUserDTO());
                        logger.debug(METHODNAME, "trueSessionDTO.getSessionId()=", trueSessionDTO.getSessionId());
                        logger.debug(METHODNAME, "trueSessionDTO.getAppDTO()=", trueSessionDTO.getAppDTO());
                        logger.debug(METHODNAME, "trueSessionDTO.getProxyUserDTO()=", trueSessionDTO.getProxyUserDTO());
                        logger.debug(METHODNAME, "trueSessionDTO.getUserDTO()=", trueSessionDTO.getUserDTO());
                        logger.debug(METHODNAME, "trueSessionDTO.isProxy()=", trueSessionDTO.isProxy());
                    }

//                    UserDTO proxiedUserDTO = getProxiedUserDTO(userDTO.getUserId(), trueSessionDTO.getAppDTO(), trueSessionDTO.getProxyUserDTO());
//                    UserDTO proxiedUserDTO = getProxiedUserDTO(sessionDTO.getUserDTO().getUsername(), trueSessionDTO.getAppDTO(), trueSessionDTO.getProxyUserDTO());
                    UserDTO proxiedUserDTO = getProxiedUserDTO(trueSessionDTO.getUserDTO().getUserId(), trueSessionDTO.getAppDTO(), trueSessionDTO.getProxyUserDTO());

                    if (proxiedUserDTO == null) {
                        logger.error(METHODNAME, "proxy user missing");
                        throw new AuthenticationException("Proxy user not found.", ExceptionReason.MISSING_ENTRY);
                    }
                    trueSessionDTO.setUserDTO(proxiedUserDTO);
                    logger.debug(METHODNAME, "Detected proxied session - retrieving UserDTO from getProxiedUserDTO: ", proxiedUserDTO.getUsername());
                }
//                logger.logDuration(METHODNAME + "findProxyUserStart ", findProxyUserStart);
                if (trueSessionDTO.getUserDTO() == null) {
                    logger.error(METHODNAME, "user missing");
                    throw new AuthenticationException("User not found.", ExceptionReason.MISSING_ENTRY);
                }
//                long isSessionValidStart = System.nanoTime();
                boolean sessionValid = isSessionValid(trueSessionDTO, true);
//                logger.logDuration(METHODNAME + "isSessionValidStart ", isSessionValidStart);
                logger.debug(METHODNAME, "sessionValid=", sessionValid);
                logger.debug(
                        METHODNAME,
                        " checking ",
                        trueSessionDTO.getUserDTO().getUserId(),
                        "/",
                        trueSessionDTO.getUserDTO().getUsername(),
                        "'s authority to call ",
                        permissionType,
                        " for class ",
                        dtoClass);
//                long checkAuthorityFromPermissionMapStart = System.nanoTime();
                checkAuthorityFromPermissionMap(permissionType, dtoClass, trueSessionDTO);
                checkAuthClassPermList.add(permissionType);
//                logger.logDuration(METHODNAME + "checkAuthorityFromPermissionMapStart ", checkAuthorityFromPermissionMapStart);
            }
            if (checkAuthLastModSet && logger.isDebugEnabled()) {
                logger.debug(METHODNAME, "checkAuthLastModSet is set - skipping update...");
            }
            if (!checkAuthSessionReloaded && !checkAuthLastModSet && trueSessionDTO != null && !internalSession) {
//                long SessionUpdateStart = System.nanoTime();
                // update the last mod date on the original sessionDTO
                logger.debug(METHODNAME,
                        "updating sessionDTO last mod date for: ",
                        trueSessionDTO.getSessionId(),
                        " app: ", trueSessionDTO.getAppDTO().getAppName(),
                        " user: ", trueSessionDTO.getUserDTO().getUsername());
                trueSessionDTO.setLastModDatetime(new Date());
                sessionBO.updateMain(trueSessionDTO, Update.class, AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
                propertyBagDTO.put("checkAuthLastModSet", true);
                sessionDTO.setProxy(trueSessionDTO.isProxy());
                sessionDTO.setProxyUserDTO(trueSessionDTO.getProxyUserDTO());
                sessionDTO.setAppDTO(trueSessionDTO.getAppDTO());
                sessionDTO.setUserDTO(trueSessionDTO.getUserDTO());
                sessionDTO.setCreateDatetime(trueSessionDTO.getCreateDatetime());
                sessionDTO.setCreateId(trueSessionDTO.getCreateId());
                sessionDTO.setLastModDatetime(trueSessionDTO.getLastModDatetime());
                sessionDTO.setLastModId(trueSessionDTO.getLastModId());
                sessionDTO.getPropertyMap().putAll(trueSessionDTO.getPropertyMap());
//                logger.logDuration(METHODNAME + "SessionUpdateStart ", SessionUpdateStart);
            }

        } catch (ValidationException ex) {
            logger.error(METHODNAME, ex);
        } catch (ConstraintViolationException ex) {
            logger.error(METHODNAME, ex);
        } finally {
            if (!internalSession && logger.isDebugEnabled()) {
//                logger.logDuration(METHODNAME, start);
                checkAuthDuration += (System.nanoTime() - start) / 1000000.0;
                propertyBagDTO.put("checkAuthDuration", checkAuthDuration);
                logger.debug(METHODNAME, propertyBagDTO.get("checkAuthSrc"), " - ", permissionType, " cumulative duration(ms): " + checkAuthDuration);
            }
        }
    }

    private void checkAuthorityFromPermissionMap(PermissionType permissionType, Class<? extends BaseDTO> dtoClass, SessionDTO sessionDTO)
            throws AuthorizationException, MtsException {
        final String METHODNAME = "checkAuthorityFromPermissionMap ";
//        logger.debug(METHODNAME, "permissionType=", permissionType);
//        logger.debug(METHODNAME, "dtoClass=", dtoClass);
//        logger.debug(METHODNAME, "sessionDTO=", sessionDTO);
        boolean throwExceptionOnAuthFailure = true;
        boolean hasAuthority = false;  // until proven True
        String username = "<unset>";
        if (sessionDTO == null) {
            throw new MtsException("param sessionDTO is null!");
        }
        if (permissionType == null) {
            throw new MtsException("param permissionType is null!");
        }
        if (dtoClass == null) {
            throw new MtsException("param dtoClass is null!");
        }
        if (sessionDTO.getUserDTO() != null) {
            username = sessionDTO.getUserDTO().getUsername();
        }

        UserSecuritySchemePermissionMap userSecuritySchemePermissionMapDTO = userSecurityMGRLocal.getUserSecuritySchemePermissionMapDTO(sessionDTO);
        Map<String, List<PermissionType>> permissionDenyMap = userSecuritySchemePermissionMapDTO.getPermissionDenyMap();
        Map<String, List<PermissionType>> permissionAllowMap = userSecuritySchemePermissionMapDTO.getPermissionAllowMap();

        if (logger.isDebugEnabled()) {
            for (String key : permissionDenyMap.keySet()) {
                logger.debug(METHODNAME, "found deny perm on ", username, " - ", key, " - ", permissionDenyMap.get(key));
            }
            for (String key : permissionAllowMap.keySet()) {
                logger.debug(METHODNAME, "found allow perm on ", username, " - ", key, " - ", permissionAllowMap.get(key));
            }
        }
        List<PermissionType> denyMapValue = permissionDenyMap.get(dtoClass.getCanonicalName());
        List<PermissionType> wildcardDenyMapValue = permissionDenyMap.get(BaseDTO.class.getCanonicalName());
        List<PermissionType> allowMapValue = permissionAllowMap.get(dtoClass.getCanonicalName());
        List<PermissionType> wildcardAllowMapValue = permissionAllowMap.get(BaseDTO.class.getCanonicalName());
//        logger.debug(METHODNAME, "    denyMapValue: ", denyMapValue);
//        logger.debug(METHODNAME, "    wildcardDenyMapValue: ", wildcardDenyMapValue);
//        logger.debug(METHODNAME, "    allowMapValue: ", allowMapValue);
//        logger.debug(METHODNAME, "    wildcardAllowMapValue: ", wildcardAllowMapValue);
        if (wildcardDenyMapValue != null || wildcardAllowMapValue != null) {
            if (logger.isDebugEnabled()) {
                logger.debug(METHODNAME, "detected wildcard permission: ");
                logger.debug(METHODNAME, "    wildcardDenyMapValue: ", wildcardDenyMapValue);
                logger.debug(METHODNAME, "    wildcardAllowMapValue: ", wildcardAllowMapValue);
            }
        }
        if ((wildcardDenyMapValue != null && (wildcardDenyMapValue.contains(PermissionType.FULL) || wildcardDenyMapValue.contains(permissionType)))
                || (denyMapValue != null && (denyMapValue.contains(PermissionType.FULL) || denyMapValue.contains(permissionType)))) {
            logger.debug(METHODNAME, "- ", username, " DENIED ACCESS TO ", permissionType.toString(), "@", dtoClass.getSimpleName());
            if (throwExceptionOnAuthFailure) {
                throw new AuthorizationException(
                        ExceptionReason.ILLEGAL_ACCESS,
                        String.format("%s does not have access to %s (%s).", username, dtoClass.getSimpleName(), permissionType.toString()));
            } else {
                logger.debug(METHODNAME + "***** Exception not thrown for development purposes. *****");
            }
        }
        if ((wildcardAllowMapValue != null && (wildcardAllowMapValue.contains(PermissionType.FULL) || wildcardAllowMapValue.contains(permissionType)))
                || (allowMapValue != null && (allowMapValue.contains(PermissionType.FULL) || allowMapValue.contains(permissionType)))) {
            hasAuthority = true;
        }
        if (!hasAuthority) {
            logger.debug(METHODNAME, "- ", username, " NO ACCESS ASSIGNED TO ", permissionType.toString(), "@", dtoClass.getSimpleName());
            if (throwExceptionOnAuthFailure) {
                throw new AuthorizationException(
                        ExceptionReason.ILLEGAL_ACCESS,
                        String.format("%s does not have access to %s (%s).", username, dtoClass.getSimpleName(), permissionType.toString()));
            } else {
                logger.debug(METHODNAME + "***** Exception not thrown for development purposes. *****");
            }
        }
    }

    /**
     * Evaluates a sessionDTO for validity.
     *
     * @param sessionDTO
     * @return
     * @throws AuthenticationException
     * @throws AuthorizationException
     * @throws MtsException
     * @throws NotFoundException
     */
    @Override
    public boolean isSessionValid(SessionDTO sessionDTO)
            throws AuthenticationException, AuthorizationException, MtsException, NotFoundException {
        return isSessionValid(sessionDTO, false);
    }

    /**
     * Evaluates a sessionDTO for validity.
     *
     * @param sessionDTO
     * @param skipFind
     * @return
     * @throws AuthenticationException
     * @throws AuthorizationException
     * @throws MtsException
     * @throws NotFoundException
     */
    private boolean isSessionValid(SessionDTO sessionDTO, boolean skipFind)
            throws AuthenticationException, AuthorizationException, MtsException, NotFoundException {
        final String METHODNAME = "isSessionValid ";
        boolean result;
        if (!skipFind) {
            try {
                sessionDTO = sessionBO.findByPrimaryKeyMain(sessionDTO, new ArrayList<Class>(), AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
            } catch (NotFoundException e) {
                logger.error(METHODNAME, "session was not found - invalid.");
                throw new AuthenticationException("Session ID not found: " + sessionDTO.getSessionId(), ExceptionReason.MISSING_ENTRY);
            } catch (ValidationException e) {
                logger.error(e);
                throw new AuthenticationException("Unexpected exception: " + sessionDTO.getSessionId(), ExceptionReason.FATAL_ERROR);
            }
        }
        boolean sessionExpired = isSessionExpired(sessionDTO);
        logger.debug(METHODNAME, "session expired=", sessionExpired);
        if (sessionExpired) {
            try {
                removeSession(sessionDTO);
            } catch (NotFoundException nfe) {
                logger.error(METHODNAME, "Session missing: ", nfe.getMessage());
            }
            logger.error(METHODNAME, "Session ID expired: ", sessionDTO.getSessionId());
            throw new AuthenticationException("Session ID expired: " + sessionDTO.getSessionId(), ExceptionReason.SESSION_EXPIRED);
        }
        try {
            if (sessionDTO.getUserDTO() == null) {
                logger.error(METHODNAME, "User ID was null: ", sessionDTO.getSessionId());
                throw new AuthenticationException("User ID was null: " + sessionDTO.getSessionId(), ExceptionReason.BAD_CREDENTIALS);
            }
            boolean disabled = sessionDTO.getUserDTO().isDisabled();
            logger.debug(METHODNAME, "disabled=", disabled);
            if (disabled) {
                removeSession(sessionDTO);
                logger.error(METHODNAME, "Session userId disabled.");
                throw new AuthenticationException(ExceptionReason.USER_DISABLED);
            }
            boolean expired = sessionDTO.getUserDTO().isExpired();
            logger.debug(METHODNAME, "user expired=", expired);
            if (expired) {
                removeSession(sessionDTO);
                logger.error(METHODNAME, "Session userId expired.");
                throw new AuthenticationException(ExceptionReason.USER_EXPIRED);
            }
//            boolean changePassword = sessionDTO.getUserDTO().isChangePassword();
//            logger.debug(METHODNAME, "change password=", changePassword);
//            if (changePassword) {
//                removeSession(sessionDTO);
//                logger.error(METHODNAME, "Session userId needs to change their password.");
//                throw new AuthenticationException(ExceptionReason.SESSION_NOT_VALID);
//            }
        } catch (NotFoundException nfe) {
            logger.error(METHODNAME, "Session userId not found.");
            throw new AuthenticationException(ExceptionReason.BAD_CREDENTIALS);
        }
        sessionDTO.setLastModDatetime(new Date());
        result = true;
        logger.debug(METHODNAME, "result=", result);
        return result;
    }

    @Override
    public UserDTO getUserDTO(String username)
            throws MtsException, NotFoundException, AuthenticationException, AuthorizationException {
        final String METHODNAME = "getUserDTO ";
        logger.logBegin(METHODNAME);
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);
        try {
            userDTO = userBO.findByQueryMain(
                    userDTO,
                    UserDTO.DtoByUsername.class,
                    new ArrayList<Class>(),
                    AuthenticationUtils.getInternalSessionDTO(),
                    new PropertyBagDTO());
            if (userDTO == null) {
                throw new AuthenticationException(ExceptionReason.MISSING_ENTRY);
            }
        } catch (ValidationException e) {
            throw new MtsException("Unexpected ValidationException: ", e);
        } finally {
            logger.logEnd(METHODNAME);
        }
        return userDTO;
    }

    @Override
    public AppDTO getAppDTO(String appName)
            throws MtsException, NotFoundException, AuthenticationException, AuthorizationException {
        final String METHODNAME = "getAppDTO ";
        logger.logBegin(METHODNAME);
        AppDTO appDTO = new AppDTO();
        appDTO.setAppName(appName);
        try {
            appDTO = appBO.findByQueryMain(
                    appDTO,
                    AppDTO.DtoByAppName.class,
                    new ArrayList<Class>(),
                    AuthenticationUtils.getInternalSessionDTO(),
                    new PropertyBagDTO());
            if (appDTO == null) {
                throw new AuthenticationException(ExceptionReason.MISSING_ENTRY);
            }
        } catch (NotFoundException e) {
            logger.error(METHODNAME + "NotFoundException on: " + appName);
            logger.error(e);
            throw e;
        } catch (ValidationException ve) {
            throw new MtsException("Unexpected ValidationException: ", ve);
        } finally {
            logger.logEnd(METHODNAME);
        }
        return appDTO;
    }

    /**
     * Check a users credentials against those in the database. Returns a
     * UserDTO instance if successful.
     *
     * @param userDTO
     * @param password
     * @return {@link org.cdsframework.dto.UserDTO [UserDTO]} class instance.
     * @throws NotFoundException if cachedUserDTO is not found.
     * @throws AuthenticationException if session is bad.
     * @throws MtsException
     * @throws AuthorizationException
     */
    @Override
    public boolean authenticate(UserDTO userDTO, String password)
            throws NotFoundException, AuthenticationException, MtsException, AuthorizationException {
        final String METHODNAME = "authenticate ";
        logger.logBegin(METHODNAME);
        boolean result = false;
        try {
            if (userDTO != null) {
                // check if old password hash algo check works
                if (userDTO.getPasswordHash() != null) {
                    result = userDTO.getPasswordHash().equals(StringUtils.getShaHashFromString(password));
                } else {
                    logger.error(METHODNAME, "userDTO.getPasswordHash() is null!");
                }
                if (result) {
                    // update the user to the new password algo
                    String newHash = PasswordHash.createHash(password);
                    userDTO.setPasswordHash(newHash);
                    try {
                        userBO.updateMainNew(userDTO, UserDTO.UpdatePasswordHash.class, AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
                    } catch (ConstraintViolationException e) {
                        logger.error("Something went wrong updating the password hash for user_id: ", userDTO.getUserId(), e);
                    }
                    logger.debug(METHODNAME, "Updated the password hash with the new algorithm for user: ", userDTO.getUsername());
                } else {
                    // check new password algo
                    if (userDTO.getPasswordHash() != null) {
                        result = PasswordHash.validatePassword(password, userDTO.getPasswordHash());
                    } else {
                        logger.error(METHODNAME, "userDTO.getPasswordHash() is null!");
                    }
                }
                if (!result) {
                    logger.debug("Bad authentication: db - ", userDTO.getPasswordHash(), " incoming - ", StringUtils.getShaHashFromString(password));
                    userDTO.setFailedLoginAttempts(userDTO.getFailedLoginAttempts() + 1);
                    try {
                        logger.debug("Updating db for user_id: ", userDTO.getUserId(), " - ", userDTO.getDTOStates());
                        userBO.updateMainNew(userDTO, Update.class, AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
                    } catch (ConstraintViolationException e) {
                        logger.error("Something went wrong updating the failed login attempts value for user_id: ", userDTO.getUserId(), e);
                    }
                    logger.warn(userDTO.getUsername(), " failed to login.");
                } else {
                    userDTO.setFailedLoginAttempts(0);
                    try {
                        if (userDTO.isUpdated()) {
                            logger.debug("Updating db for user_id: ", userDTO.getUserId(), " - ", userDTO.getDTOStates());
                            userBO.updateMainNew(userDTO, Update.class, AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
                        }
                    } catch (ConstraintViolationException e) {
                        logger.error("Something went wrong updating the failed login attempts value for user_id: ", userDTO.getUserId(), e);
                    }
                }
            } else {
                throw new AuthenticationException(ExceptionReason.MISSING_ENTRY);
            }
        } catch (ValidationException e) {
            logger.error(e);
        } catch (NoSuchAlgorithmException e) {
            logger.error(e);
        } catch (InvalidKeySpecException e) {
            logger.error(e);
        } finally {
            logger.logEnd(METHODNAME);
        }
        return result;
    }

    /**
     * Create a new session based on the UserDTO and AppDTO object instances.
     *
     * @param userDTO
     * @param proxyingUserDTO
     * @param appDTO
     * @return {@link org.cdsframework.dto.SessionDTO [SessionDTO]} class
     * instance.
     * @throws MtsException
     * @throws NotFoundException
     * @throws AuthenticationException
     * @throws AuthorizationException
     */
    @Override
    public SessionDTO createSession(UserDTO userDTO, UserDTO proxyingUserDTO, AppDTO appDTO)
            throws MtsException, NotFoundException, AuthenticationException, AuthorizationException {
        final String METHODNAME = "createSession ";
        logger.logBegin(METHODNAME);
        SessionDTO sessionDTO = null;
        if (logger.isDebugEnabled()) {
            logger.debug(METHODNAME, "proxyingUserDTO: ", proxyingUserDTO);
            if (proxyingUserDTO != null) {
                logger.debug(METHODNAME, "proxyingUserDTO.getUserId(): ", proxyingUserDTO.getUserId());
                logger.debug(METHODNAME, "proxyingUserDTO.getUsername(): ", proxyingUserDTO.getUsername());
            }
            logger.debug(METHODNAME, "userDTO: ", userDTO);
            if (userDTO != null) {
                logger.debug(METHODNAME, "userDTO.getUserId(): ", userDTO.getUserId());
                logger.debug(METHODNAME, "userDTO.getUsername(): ", userDTO.getUsername());
            }
            logger.debug(METHODNAME, "appDTO: ", appDTO);
            if (appDTO != null) {
                logger.debug(METHODNAME, "appDTO.getAppId(): ", appDTO.getAppId());
                logger.debug(METHODNAME, "appDTO.getAppName(): ", appDTO.getAppName());
            }
        }
        boolean internalApp = AuthenticationUtils.isInternalApp(appDTO);
        try {
            sessionDTO = new SessionDTO();
            if (proxyingUserDTO != null) {
                sessionDTO.setProxy(true);
                sessionDTO.setProxyUserDTO(proxyingUserDTO);
            } else {
                sessionDTO.setProxy(false);
            }
            sessionDTO.setUserDTO(userDTO);
            sessionDTO.setAppDTO(appDTO);
            if (!internalApp) {
                sessionDTO = sessionBO.addMain(sessionDTO, Add.class, AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
            }
            if (sessionDTO.isProxy()) {
                sessionDTO.setUserDTO(userDTO);
            }
            sessionDTO.getUserDTO().setPasswordHash("");
            DTOUtils.unsetDTOState(sessionDTO.getUserDTO());
            if (logger.isDebugEnabled()) {
                logger.debug(METHODNAME, "Post SessionDTO create - UserDTO: ", sessionDTO.getUserDTO().getUserId(), " - ", sessionDTO.getUserDTO().getUsername());
                logger.debug(METHODNAME, "Post SessionDTO create - AppDTO: ", sessionDTO.getAppDTO().getAppId(), " - ", sessionDTO.getAppDTO().getAppName());
            }

            logger.debug(METHODNAME, "About to call Security Map Implementations");
            Map<String, BaseSecurityInterface> securityImpl = userSecurityMGRLocal.getSecurityImpl();
            for (Map.Entry<String, BaseSecurityInterface> mapEntry : securityImpl.entrySet()) {
                mapEntry.getValue().postCreateSession(sessionDTO);
            }

        } catch (ConstraintViolationException de) {
            throw new MtsException("Error creating session.", de);
        } catch (ValidationException ve) {
            throw new MtsException("Error creating session.", ve);
        } finally {
            logger.logEnd(METHODNAME);
        }
        return sessionDTO;
    }

    /**
     * Checks whether a session is expired.
     *
     * @param session
     * @return
     */
    @Override
    public boolean isSessionExpired(SessionDTO session) {
        final String METHODNAME = "isSessionExpired ";
        logger.logBegin(METHODNAME);
        try {
            if (AuthenticationUtils.isInternalSession(session)) {
                return false;
            }
            long currentTimeMillis = System.currentTimeMillis();
            long sessionLastModDatetime = session.getLastModDatetime().getTime();
            long difference = (currentTimeMillis - sessionLastModDatetime) / 1000;
            Integer sessionTimeout = propertyMGRLocal.get("SESSION_TIMEOUT", Integer.class);
            if (sessionTimeout != null && (difference > sessionTimeout)) {
                logger.error("session timeout: " + currentTimeMillis + " - " + sessionLastModDatetime + " > " + sessionTimeout);
                return true;
            } else {
                return false;
            }
        } finally {
            logger.logEnd(METHODNAME);
        }
    }

    /**
     * Removes a session from the cache and database.
     *
     * @param sessionDTO
     * @throws NotFoundException
     * @throws MtsException
     */
    //@TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public void removeSession(SessionDTO sessionDTO) throws NotFoundException, MtsException {
        final String METHODNAME = "removeSession ";
        logger.logBegin(METHODNAME);
        try {
            if (!AuthenticationUtils.isInternalSession(sessionDTO)) {
                // The session does not contain the Application DTO (for RS clients?)
                sessionDTO = sessionBO.findByPrimaryKeyMain(sessionDTO, defaultChildClassList, AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
                //DTOUtils.setDTOState(sessionDTO, DTOState.UNSET);
                sessionDTO.delete(true);
                sessionBO.deleteMain(sessionDTO, Delete.class, AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());

                logger.debug(METHODNAME, "About to call Security Map Implementations");
                Map<String, BaseSecurityInterface> securityImpl = userSecurityMGRLocal.getSecurityImpl();
                for (Map.Entry<String, BaseSecurityInterface> mapEntry : securityImpl.entrySet()) {
                    mapEntry.getValue().postRemoveSession(sessionDTO);
                }
            }
        } catch (ConstraintViolationException ex) {
            logger.error(METHODNAME + "A ConstraintViolationException has occurred; Message: " + ex.getMessage());
        } catch (ValidationException ve) {
            throw new MtsException("Error removing session.", ve);
        } catch (AuthenticationException ae) {
            throw new MtsException("Error removing session.", ae);
        } catch (AuthorizationException ae) {
            throw new MtsException("Error removing session.", ae);
        } finally {
            logger.logEnd(METHODNAME);
        }
    }

    @Override
    public SessionDTO getProxiedUserSession(String userId, SessionDTO sessionDTO)
            throws MtsException, NotFoundException, AuthorizationException, AuthenticationException {
        final String METHODNAME = "getProxiedUserSession ";
        Long start = System.nanoTime();
        logger.logBegin(METHODNAME);
        try {
            logger.debug(METHODNAME, "userId/session: ", userId, "/", sessionDTO);
            if (userId == null) {
                throw new MtsException(METHODNAME + "userId is null!");
            }
            if (sessionDTO == null) {
                throw new MtsException(METHODNAME + "session is null!");
            }
            // this calls findbyprimarykey, last mod date update, and refreshes the SessionDTO passed in...
            PropertyBagDTO propertyBagDTO = new PropertyBagDTO();
            propertyBagDTO.put("checkAuthSrc", "getProxiedUserSession");
            checkAuthority(PermissionType.SELECT, SessionDTO.class, sessionDTO, propertyBagDTO);

            // get proxying AppDTO
            AppDTO proxyingUserAppDTO = sessionDTO.getAppDTO();
            logger.debug(METHODNAME, "proxying app: ", proxyingUserAppDTO);

            // get proxying UserDTO
            UserDTO proxyingUserDTO = sessionDTO.getUserDTO();
            logger.debug(METHODNAME, "proxying user: ", proxyingUserAppDTO);

            if (proxyingUserDTO != null) {
                proxyingUserDTO.setPasswordHash("");
                DTOUtils.unsetDTOState(proxyingUserDTO);
            }

            if (proxyingUserDTO == null) {
                throw new MtsException(logger.error(METHODNAME, "proxyingUserDTO is null!"));
            }
            if (proxyingUserAppDTO == null) {
                throw new MtsException(logger.error(METHODNAME, "proxyingUserApp is null!"));
            }

            if (!AuthenticationUtils.isInternalSession(sessionDTO)
                    && (proxyingUserDTO.getProxyAppId() == null
                    || !proxyingUserDTO.getProxyAppId().equals(proxyingUserAppDTO.getAppId())
                    || !proxyingUserAppDTO.isProxyUserTable()
                    || !proxyingUserDTO.isAppProxyUser())) {
                if (proxyingUserDTO.getProxyAppId() == null) {
                    logger.error(METHODNAME, "proxyingUserDTO.getProxyAppId() is null!");
                } else if (!proxyingUserDTO.getProxyAppId().equals(proxyingUserAppDTO.getAppId())) {
                    logger.error(METHODNAME, "proxyingUserDTO.getProxyAppId() != proxyingUserApp.getAppId()");
                }
                if (!proxyingUserAppDTO.isProxyUserTable()) {
                    logger.error(METHODNAME, "not proxyingUserApp.isProxyUserTable()");
                }
                if (!proxyingUserDTO.isAppProxyUser()) {
                    logger.error(METHODNAME, "not proxyingUserDTO.isAppProxyUser()");
                }
                throw new MtsException(logger.error(METHODNAME, "proxyingUserDTO is not authorized to proxy sessions!"));
            }
            UserDTO proxiedUserDTO = getProxiedUserDTO(userId, proxyingUserAppDTO, proxyingUserDTO);
            if (logger.isDebugEnabled()) {
                logger.debug(METHODNAME, "userId: ", userId);
                logger.debug(METHODNAME, "proxiedUserDTO: ", proxiedUserDTO.getUsername());
                logger.debug(METHODNAME, "proxyingUserDTO: ", proxyingUserDTO.getUsername());
                logger.debug(METHODNAME, "proxyingUserApp: ", proxyingUserAppDTO.getAppName());
            }
            return createSession(proxiedUserDTO, proxyingUserDTO, proxyingUserAppDTO);
        } finally {
            logger.logDuration(LogLevel.DEBUG, METHODNAME + " userId=" + userId, start);
            logger.logEnd(METHODNAME);
        }
    }

    //@TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public UserDTO getProxiedUserDTO(String userId, AppDTO proxyingUserAppDTO, UserDTO proxyingUserDTO)
            throws AuthorizationException, MtsException, NotFoundException, AuthenticationException {
        final String METHODNAME = "getProxiedUserDTO ";
        logger.logBegin(METHODNAME);
        UserDTO proxiedUserDTO = null;
        if (userId != null) {
            try {
                if (proxyingUserDTO == null) {
                    throw new MtsException(logger.error(METHODNAME, "proxyingUserDTO is null!"));
                }
                if (proxyingUserAppDTO == null) {
                    throw new MtsException(logger.error(METHODNAME, "proxyingUserAppDTO is null!"));
                }
                boolean internalApp = AuthenticationUtils.isInternalApp(proxyingUserAppDTO);
                if (!internalApp) {
                    logger.debug("proxyingUserAppDTO.getProxyUserFindClass()=", proxyingUserAppDTO.getProxyUserFindClass());
                    if (proxyingUserAppDTO.getProxyUserFindClass() == null) {
                        throw new MtsException(logger.error(METHODNAME, "proxyingUserAppDTO.getProxyUserFindClass() is null!"));
                    }
                    if (proxyingUserAppDTO.getProxyUserFindClass().isEmpty()) {
                        throw new MtsException(logger.error(METHODNAME, "proxyingUserAppDTO.getProxyUserFindClass() is empty!"));
                    }
                    Map<String, Class> applicationProxyRegistrations = (Map<String, Class>) propertyMGRLocal.get("APPLICATION_PROXY_REGISTRATIONS");
                    Class findClass = applicationProxyRegistrations.get(proxyingUserAppDTO.getProxyUserFindClass());
                    if (findClass == null) {
                        throw new MtsException("No app proxy registered for: " + proxyingUserAppDTO.getProxyUserFindClass());
                    }

                    proxiedUserDTO = new UserDTO();
                    proxiedUserDTO.setUserId(userId);
                    if (logger.isDebugEnabled()) {
                        logger.debug(METHODNAME, "findClass: ", findClass);
                        logger.debug(METHODNAME, "userId: ", userId);
                        logger.debug(METHODNAME, "appName: ", proxyingUserAppDTO.getAppName());
                    }
                    try {
                        try {
                            if (!Constants.NATIVE_PROXY_USERS.contains(findClass)) {
                                Class enclosingClass = findClass.getEnclosingClass();
                                BaseDAO dtoDao = EJBUtils.getDtoDao(enclosingClass);
                                proxiedUserDTO = (UserDTO) dtoDao.findByQuery(
                                        proxiedUserDTO,
                                        findClass,
                                        AuthenticationUtils.getInternalSessionDTO(),
                                        new PropertyBagDTO());
                                if (proxiedUserDTO == null) {
                                    logger.error(METHODNAME, "Proxy user error: ", userId, " not a native user proxy and proxyingUserDTO was not found!");
                                }
                            } else {
                                proxiedUserDTO = userBO.findByQueryMain(
                                        proxiedUserDTO,
                                        findClass,
                                        defaultChildClassList,
                                        AuthenticationUtils.getInternalSessionDTO(),
                                        new PropertyBagDTO());
                            }
                        } catch (NotFoundException e) {
                            logger.error(METHODNAME, "proxy user missing, userId ", userId, " not found via find class: ", findClass);
                            logger.warn("Internal Session: ", AuthenticationUtils.getInternalSessionDTO().getSessionId());
                            logger.warn("Internal User: ", AuthenticationUtils.getInternalUserDTO().getUserId());
                            throw new AuthenticationException("Proxy user not found.", ExceptionReason.MISSING_ENTRY);
                        }
                    } catch (ValidationException e) {
                        logger.error(METHODNAME, "Unexpected ValidationException: ", e);
                    }
                } else {
                    logger.debug(METHODNAME, "internal app retrieving user dto for: ", userId);
                    proxiedUserDTO = new UserDTO();
                    proxiedUserDTO.setUsername(userId);
                    try {
                        proxiedUserDTO = userBO.findByQueryMain(
                                proxiedUserDTO,
                                UserDTO.DtoByUsername.class,
                                defaultChildClassList,
                                AuthenticationUtils.getInternalSessionDTO(),
                                new PropertyBagDTO());
                    } catch (ValidationException e) {
                        logger.error(METHODNAME, "Unexpected ValidationException: ", e);
                    }
                }
                logger.debug(METHODNAME, "proxiedUserDTO: ", proxiedUserDTO != null ? proxiedUserDTO.getUsername() : null);
            } finally {
                logger.logEnd(METHODNAME);
            }
        }
        return proxiedUserDTO;
    }
}
