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
package org.cdsframework.ejb.mgr;

import java.util.HashMap;
import org.cdsframework.base.BaseMGR;
import org.cdsframework.dto.AppDTO;
import org.cdsframework.dto.SessionDTO;
import org.cdsframework.dto.UserDTO;
import org.cdsframework.ejb.local.PropertyMGRLocal;
import org.cdsframework.ejb.remote.SecurityMGRRemote;
import org.cdsframework.enumeration.ExceptionReason;
import org.cdsframework.exceptions.MtsException;
import org.cdsframework.exceptions.NotFoundException;
import org.cdsframework.exceptions.AuthenticationException;
import org.cdsframework.exceptions.AuthorizationException;
import org.cdsframework.security.PermissionObject;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.ejb.local.SecurityMGRLocal;
import org.cdsframework.ejb.local.SecurityMGRInternal;
import org.cdsframework.ejb.local.UserSecurityMGRLocal;
import org.cdsframework.enumeration.LogLevel;
import org.cdsframework.enumeration.PermissionType;
import org.cdsframework.security.UserSecuritySchemePermissionMap;

/**
 * The SecurityMGR implements the {@link org.cdsframework.ejb.remote.SecurityMGRRemote [SecurityMGRRemote]} interface.
 *
 * @author HLN Consulting, LLC
 */
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class SecurityMGR extends BaseMGR<SessionDTO> implements SecurityMGRRemote, SecurityMGRLocal {

    @EJB
    private SecurityMGRInternal securityMGRInternal;
    @EJB
    private PropertyMGRLocal propertyMGRLocal;
    @EJB
    private UserSecurityMGRLocal userSecurityMGRLocal;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public boolean authenticate(String username, String password, String appName)
            throws AuthenticationException, AuthorizationException, MtsException {
        final String METHODNAME = "authenticate ";
        Long start = System.nanoTime();
        logger.info(METHODNAME + "called by " + username + " - appname: " + appName);
        boolean result = false;
        if (password == null || password.isEmpty()) {
            logger.error("Password is null but app doesn't allow null passwords: " + appName + " - " + username);
            throw new AuthenticationException(ExceptionReason.BAD_CREDENTIALS);
        }
        try {
            UserDTO userDTO = securityMGRInternal.getUserDTO(username);
            Integer MAX_FAILED_LOGIN_ATTEMPTS = (Integer) propertyMGRLocal.get("MAX_FAILED_LOGIN_ATTEMPTS");
            if (userDTO.getFailedLoginAttempts() >= MAX_FAILED_LOGIN_ATTEMPTS) {
                logger.error("User has exceeded their max failed login attempts.");
                throw new AuthenticationException(ExceptionReason.MAX_FAILED_LOGINS);
            }
            if (userDTO.isDisabled()) {
                logger.error("User account is disabled.");
                throw new AuthenticationException(ExceptionReason.USER_DISABLED);
            }
            if (userDTO.isExpired()) {
                logger.error("User account is expired.");
                throw new AuthenticationException(ExceptionReason.USER_EXPIRED);
            }
            AppDTO appDTO = securityMGRInternal.getAppDTO(appName);
            result = securityMGRInternal.authenticate(userDTO, password);
        } catch (NotFoundException nfe) {
            logger.error(username, " not found.");
            logger.debug(nfe);
            throw new AuthenticationException(ExceptionReason.BAD_CREDENTIALS);
        } finally {
            logger.logDuration(LogLevel.DEBUG, METHODNAME + " username=" + username, start);                                                                
        }
        return result;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public SessionDTO login(String username, String password, String appName)
            throws AuthenticationException, AuthorizationException, MtsException {
        final String METHODNAME = "login ";
        Long start = System.nanoTime();
        SessionDTO sessionDTO = null;
        if (username == null || password == null || appName == null) {
            throw new AuthenticationException(ExceptionReason.BAD_CREDENTIALS);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("login attempted: ", username, "/********/", appName);
        }
        try {
            UserDTO userDTO = securityMGRInternal.getUserDTO(username);
            logger.debug("getUserDTO returned: ", userDTO.getUserId(), " - ", userDTO.getUsername());
            AppDTO appDTO = securityMGRInternal.getAppDTO(appName);
            logger.debug("getAppDTO returned: ", appDTO.getAppId(), " - ", appDTO.getAppName());
            if (authenticate(username, password, appName)) {
                sessionDTO = securityMGRInternal.createSession(userDTO, null, appDTO);
                logger.info("Session successfully created for: ", sessionDTO.getUserDTO().getUsername());
            } else {
                throw new AuthenticationException(ExceptionReason.BAD_CREDENTIALS);
            }
        } catch (NotFoundException nfe) {
            logger.error(username, " not found.");
            logger.error(nfe);
            throw new AuthenticationException(ExceptionReason.BAD_CREDENTIALS);
        } finally {
            logger.logDuration(LogLevel.DEBUG, METHODNAME + " username=" + username, start);                                                                
        }
        return sessionDTO;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public void logout(SessionDTO session) throws AuthenticationException, MtsException {
        final String METHODNAME = "logout ";
        try {
            if (session == null) {
                logger.warn(METHODNAME + "Session null");
                return;
            }
            logger.info(METHODNAME + "logout called by " + session.getUserDTO().getUsername() + " - appname: " + session.getAppDTO().getAppName());
            try {
                securityMGRInternal.removeSession(session);
            } catch (NotFoundException nfe) {
                logger.error(METHODNAME + "Session missing: " + nfe.getMessage());
            }
        } finally {
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public SessionDTO getProxiedUserSession(String userId, SessionDTO session)
            throws AuthenticationException, AuthorizationException, MtsException {
        final String METHODNAME = "getProxiedUserSession ";
        try {
            SessionDTO proxySessionDTO = securityMGRInternal.getProxiedUserSession(userId, session);
            logger.info("Proxy session successfully created for: "
                    + proxySessionDTO.getUserDTO().getUsername()
                    + " - appname: "
                    + proxySessionDTO.getAppDTO().getAppName()
                    + " by "
                    + session.getUserDTO().getUsername());
            return proxySessionDTO;
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            throw new AuthenticationException(ExceptionReason.BAD_CREDENTIALS);
        } finally {
        }
    }

    @Override
    public Map<String, PermissionObject> getPermissionObjects(SessionDTO session)
            throws AuthenticationException, AuthorizationException, MtsException {
        return propertyMGRLocal.getPermissionMap();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public boolean isSessionValid(SessionDTO sessionDTO)
            throws MtsException {
        final String METHODNAME = "isSessionValid ";
        boolean result;
        try {
            result = securityMGRInternal.isSessionValid(sessionDTO);
        } catch (Exception nfe) {
            logger.error(nfe.getMessage());
            result = false;
        }
        return result;
    }

    @Override
    public Map<String, UserSecuritySchemePermissionMap> getUserSecuritySchemePermissionMaps(SessionDTO session)
            throws AuthenticationException, AuthorizationException, MtsException {
        final String METHODNAME = "geUserSecuritySchemePermissionMaps ";
        Map<String, UserSecuritySchemePermissionMap> result = new HashMap<String, UserSecuritySchemePermissionMap>();
        try {
            PropertyBagDTO propertyBagDTO = new PropertyBagDTO();
            propertyBagDTO.put("checkAuthSrc", "getUserSecuritySchemePermissionMaps");
            securityMGRInternal.checkAuthority(PermissionType.SELECT, UserDTO.class, session, propertyBagDTO);
            result = userSecurityMGRLocal.getUserSecuritySchemePermissionMap();
        } catch (NotFoundException e) {
            logger.error(METHODNAME, e);
        }
        return result;
    }
}
