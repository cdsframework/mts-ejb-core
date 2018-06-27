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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import org.cdsframework.base.BaseSecurityInterface;
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.dto.SecurityPermissionDTO;
import org.cdsframework.dto.SecuritySchemeDTO;
import org.cdsframework.dto.SecuritySchemeRelMapDTO;
import org.cdsframework.dto.SessionDTO;
import org.cdsframework.dto.UserDTO;
import org.cdsframework.dto.UserSecurityMapDTO;
import org.cdsframework.ejb.bo.SecuritySchemeBO;
import org.cdsframework.security.UserSecuritySchemePermissionMap;
import org.cdsframework.ejb.bo.UserBO;
import org.cdsframework.ejb.bo.UserSecurityMapBO;
import org.cdsframework.exceptions.AuthenticationException;
import org.cdsframework.exceptions.AuthorizationException;
import org.cdsframework.exceptions.MtsException;
import org.cdsframework.exceptions.NotFoundException;
import org.cdsframework.exceptions.ValidationException;
import org.cdsframework.group.FindAll;
import org.cdsframework.util.AuthenticationUtils;
import org.cdsframework.util.LogUtils;

/**
 *
 * @author sdn
 */
@Startup
@Singleton
@DependsOn({"CorePlugin"})
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@Lock(LockType.READ)
public class UserSecurityMGRLocal {

    private static final LogUtils logger = LogUtils.getLogger(UserSecurityMGRLocal.class);
    private Map<String, UserSecuritySchemePermissionMap> userSecuritySchemePermissionMap;
    private final Map<String, BaseSecurityInterface> securityMap = Collections.synchronizedMap(new LinkedHashMap<String, BaseSecurityInterface>());

    @EJB
    private UserBO userBO;
    @EJB
    private UserSecurityMapBO userSecurityMapBO;
    @EJB
    private SecuritySchemeBO securitySchemeBO;
    List<Class> childClasses = Arrays.asList(new Class[]{UserSecurityMapDTO.class});

    @PostConstruct
    public void postConstructor() {
        final String METHODNAME = "postConstructor ";
        try {
            logger.debug(METHODNAME, "rebuilding user security scheme maps...");
            userSecuritySchemePermissionMap = new HashMap<String, UserSecuritySchemePermissionMap>();
            List<UserDTO> userDTOs = userBO.findByQueryListMain(new UserDTO(), FindAll.class, childClasses, AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
            for (UserDTO userDTO : userDTOs) {
                UserSecuritySchemePermissionMap userSecuritySchemePermissionMapDTO = new UserSecuritySchemePermissionMap(userDTO);
                userSecuritySchemePermissionMap.put(userDTO.getUserId(), userSecuritySchemePermissionMapDTO);
            }
        logger.info(METHODNAME, "userSecuritySchemePermissionMap size: ", userSecuritySchemePermissionMap.size());
        } catch (ValidationException e) {
            logger.error(e);
            throw new IllegalStateException(e.getMessage());
        } catch (AuthenticationException e) {
            logger.error(e);
            throw new IllegalStateException(e.getMessage());
        } catch (AuthorizationException e) {
            logger.error(e);
            throw new IllegalStateException(e.getMessage());
        } catch (NotFoundException e) {
            logger.error(e);
            throw new IllegalStateException(e.getMessage());
        } catch (MtsException e) {
            logger.error(e);
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Lock(LockType.WRITE)
    public void refreshUserSecuritySchemePermissionMap(SecuritySchemeDTO securitySchemeDTO)
            throws MtsException, ValidationException, AuthenticationException, AuthorizationException {
        final String METHODNAME = "refreshUserSecuritySchemePermissionMap - securitySchemeDTO ";
        if (securitySchemeDTO != null) {
            UserSecurityMapDTO userSecurityMapDTO = new UserSecurityMapDTO();
            userSecurityMapDTO.setSecuritySchemeDTO(securitySchemeDTO);
            try {
                List<UserSecurityMapDTO> userSecurityMapDTOs = userSecurityMapBO.findByQueryListMain(userSecurityMapDTO, UserSecurityMapDTO.BySchemeId.class, new ArrayList(), AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
                for (UserSecurityMapDTO item : userSecurityMapDTOs) {
                    refreshUserSecuritySchemePermissionMap(item);
                }
            } catch (NotFoundException e) {
                logger.warn(METHODNAME, e);
            }
        }
    }

    @Lock(LockType.WRITE)
    public void refreshUserSecuritySchemePermissionMap(SecurityPermissionDTO securityPermissionDTO)
            throws MtsException, ValidationException, AuthenticationException, AuthorizationException {
        final String METHODNAME = "refreshUserSecuritySchemePermissionMap - securityPermissionDTO ";
        if (securityPermissionDTO != null && securityPermissionDTO.getSchemeId() != null) {
            SecuritySchemeDTO securitySchemeDTO = new SecuritySchemeDTO();
            securitySchemeDTO.setSchemeId(securityPermissionDTO.getSchemeId());
            try {
                securitySchemeDTO = securitySchemeBO.findByPrimaryKeyMain(securitySchemeDTO, new ArrayList<Class>(), AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
                refreshUserSecuritySchemePermissionMap(securitySchemeDTO);
            } catch (NotFoundException e) {
                logger.warn(METHODNAME, e);
            }
        }
    }

    @Lock(LockType.WRITE)
    public void refreshUserSecuritySchemePermissionMap(SecuritySchemeRelMapDTO securitySchemeRelMapDTO)
            throws MtsException, ValidationException, AuthenticationException, AuthorizationException {
        final String METHODNAME = "refreshUserSecuritySchemePermissionMap - securitySchemeRelMapDTO ";
        if (securitySchemeRelMapDTO != null && securitySchemeRelMapDTO.getRelatedSecuritySchemeDTO()!= null) {
            SecuritySchemeDTO securitySchemeDTO = securitySchemeRelMapDTO.getRelatedSecuritySchemeDTO();
            refreshUserSecuritySchemePermissionMap(securitySchemeDTO);
        }
    }

    @Lock(LockType.WRITE)
    public void refreshUserSecuritySchemePermissionMap(UserSecurityMapDTO userSecurityMapDTO)
            throws MtsException, ValidationException, AuthenticationException, AuthorizationException {
        final String METHODNAME = "refreshUserSecuritySchemePermissionMap - userSecurityMapDTO ";
        if (userSecurityMapDTO != null && userSecurityMapDTO.getUserId() != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setUserId(userSecurityMapDTO.getUserId());
            try {
                userDTO = userBO.findByPrimaryKeyMain(userDTO, new ArrayList<Class>(), AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
                refreshUserSecuritySchemePermissionMap(userDTO);
            } catch (NotFoundException e) {
                logger.warn(METHODNAME, e);
            }
        }
    }

    @Lock(LockType.WRITE)
    public void refreshUserSecuritySchemePermissionMap(UserDTO userDTO) throws MtsException {
        final String METHODNAME = "refreshUserSecuritySchemePermissionMap - userDTO ";
        if (userDTO != null) {
            if (userDTO.getUserId() != null) {
                logger.debug(METHODNAME, "refreshing user: ", userDTO.getUsername());
                UserDTO foundUser = new UserDTO();
                foundUser.setUserId(userDTO.getUserId());
                try {
                    foundUser = userBO.findByPrimaryKeyMain(foundUser, childClasses, AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
                    UserSecuritySchemePermissionMap userSecuritySchemePermissionMapDTO = new UserSecuritySchemePermissionMap(foundUser);
                    userSecuritySchemePermissionMap.put(userDTO.getUserId(), userSecuritySchemePermissionMapDTO);
                    logger.info(METHODNAME, "userSecuritySchemePermissionMap size: ", userSecuritySchemePermissionMap.size());
                } catch (ValidationException e) {
                    logger.error(e);
                    throw new MtsException(e.getMessage());
                } catch (AuthenticationException e) {
                    logger.error(e);
                    throw new MtsException(e.getMessage());
                } catch (AuthorizationException e) {
                    logger.error(e);
                    throw new MtsException(e.getMessage());
                } catch (NotFoundException e) {
                    logger.error(METHODNAME, "A NotFoundException has occurred; Message: ", e.getMessage());
                    //logger.error(e);
                    throw new MtsException(e.getMessage());
                }
            } else {
                logger.error(METHODNAME, "userDTO.getUserId() is null!");
            }
        } else {
            logger.error(METHODNAME, "userDTO is null!");
        }
    }

    @Lock(LockType.WRITE)
    public void deleteUserSecuritySchemePermissionMap(UserDTO userDTO) throws MtsException {
        final String METHODNAME = "deleteUserSecuritySchemePermissionMap ";
        if (userDTO != null) {
            if (userDTO.getUserId() != null) {
                logger.info(METHODNAME, "removing user: ", userDTO.getUsername());
                userSecuritySchemePermissionMap.remove(userDTO.getUserId());
            } else {
                logger.error(METHODNAME, "userDTO.getUserId() is null!");
            }
        } else {
            logger.error(METHODNAME, "userDTO is null!");
        }
    }

    @Lock(LockType.READ)
    public void registerySecurityImpl(BaseSecurityInterface securitySecurityImpl) {
        securityMap.put(securitySecurityImpl.getClass().getCanonicalName(), securitySecurityImpl);
    }

    @Lock(LockType.READ)
    public Map<String, BaseSecurityInterface> getSecurityImpl() {
        return securityMap;
    }

    public UserSecuritySchemePermissionMap getUserSecuritySchemePermissionMapDTO(SessionDTO sessionDTO) throws MtsException {
        final String METHODNAME = "getUserSecuritySchemePermissionMapDTO ";
        if (sessionDTO == null) {
            throw new MtsException(METHODNAME + "sessionDTO is null");
        }
        UserDTO userDTO = null;
        if (sessionDTO.isProxy()) {
            if (sessionDTO.getProxyUserDTO() == null) {
                throw new MtsException(METHODNAME + "proxy user is null on proxy session: " + sessionDTO);
            } else if (sessionDTO.getUserDTO() == null) {
                throw new MtsException(METHODNAME + "user is null on proxy session: " + sessionDTO);
            }
            if (sessionDTO.getUserDTO().isProxiedUser()) {
                userDTO = sessionDTO.getProxyUserDTO();
            } else {
                userDTO = sessionDTO.getUserDTO();
            }
        } else {
            if (sessionDTO.getUserDTO() == null) {
                throw new MtsException(METHODNAME + "user is null on session: " + sessionDTO);
            }
            userDTO = sessionDTO.getUserDTO();
        }
        if (userDTO == null) {
            throw new MtsException(METHODNAME + "user is null: " + sessionDTO);
        }
        if (userDTO.getUserId() == null) {
            throw new MtsException(METHODNAME + "userId is null: " + sessionDTO);
        }
        logger.debug(METHODNAME, "looking up map for userDTO.getUserId()=", userDTO.getUserId());
        UserSecuritySchemePermissionMap userSecuritySchemePermissionMapDTO = userSecuritySchemePermissionMap.get(userDTO.getUserId());
        if (userSecuritySchemePermissionMapDTO == null) {
            logger.warn(METHODNAME, "userSecuritySchemePermissionMapDTO not found in cache - adding it.");
            refreshUserSecuritySchemePermissionMap(userDTO);
        }
        return userSecuritySchemePermissionMapDTO;
    }

    public Map<String, UserSecuritySchemePermissionMap> getUserSecuritySchemePermissionMap() {
        return userSecuritySchemePermissionMap;
    }
}
