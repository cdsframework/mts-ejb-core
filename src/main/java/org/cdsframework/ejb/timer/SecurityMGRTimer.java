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
package org.cdsframework.ejb.timer;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.ScheduleExpression;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import org.cdsframework.dto.AppDTO;
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.dto.SessionDTO;
import org.cdsframework.dto.UserDTO;
import org.cdsframework.ejb.bo.AppBO;
import org.cdsframework.ejb.bo.SessionBO;
import org.cdsframework.ejb.bo.UserBO;
import org.cdsframework.ejb.local.SecurityMGRInternal;
import org.cdsframework.exceptions.ConstraintViolationException;
import org.cdsframework.exceptions.NotFoundException;
import org.cdsframework.group.Update;
import org.cdsframework.util.AuthenticationUtils;
import org.cdsframework.util.EjbCoreConfiguration;
import org.cdsframework.util.LogUtils;

/**
 *
 * @author HLN Consulting, LLC
 */
@Startup
@Singleton
@LocalBean
@DependsOn({"CorePlugin"})
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class SecurityMGRTimer {

    private LogUtils logger;
    @EJB
    private SecurityMGRInternal securityMGRInternal;
    @EJB
    private SessionBO sessionBO;
    @EJB
    private AppBO appBO;
    @EJB
    private UserBO userBO;
    @Resource
    private SessionContext sessionCtx;
    private final int PROCESS_LIMIT = 100;

    private static final Properties INSTANCE_PROPERTIES = new Properties();
    
    /**
     * Default no arg constructor initializes the superclass.
     */
    public SecurityMGRTimer() {
        logger = LogUtils.getLogger(SecurityMGRTimer.class);
    }

    @PostConstruct
    public void postConstructor() {
        final String METHODNAME = "postConstructor ";
        logger.logBegin(METHODNAME);
        
        boolean mtsMaster = EjbCoreConfiguration.isMtsMaster();
        logger.info(METHODNAME, "mtsMaster=", mtsMaster);
        if (mtsMaster) {
            TimerConfig timerConfig = new TimerConfig();
            timerConfig.setPersistent(false);
            timerConfig.setInfo("Session expiration timer");
            ScheduleExpression scheduleExpression = new ScheduleExpression();
            scheduleExpression.hour("*");
            scheduleExpression.minute("*/5");
            sessionCtx.getTimerService().createCalendarTimer(scheduleExpression, timerConfig);
            logger.debug("SecurityMGRTimer garbageCollectSessionMap initiated @2 minute intervals");
        }
    }

    /**
     * Iterates through the sessionDTO map and cleans out bad, or expired sessions.
     *
     * Performs various operations to determine if a session is bad. Sessions are bad if one of the following conditions are true:
     *
     * session == null;
     *
     * session.appDTO == null;
     *
     * session.userDTO == null;
     *
     * sessionDTO.isProxy() && (sessionDTO.getProxyUserDTO() == null || sessionDTO.getProxyUserDTO().getUserId() == null);
     *
     * sessionDTO.isProxy() && securityMGRInternal.getProxiedUserDTO returns null

 sessionDTO is expired;
     */
    @Timeout
    private void garbageCollectSessionMap() {
        final String METHODNAME = "garbageCollectSessionMap ";
        logger.debug("Running garbage collection on session table...");
        List<SessionDTO> sessionsToDelete = new ArrayList<SessionDTO>();
        try {
            PropertyBagDTO propertyBagDTO = new PropertyBagDTO();
            propertyBagDTO.put("adHocQueryLimit", 100);
            List<SessionDTO> sessionDTOs = sessionBO.findByQueryListMain(
                    new SessionDTO(),
                    SessionDTO.FindAllReverse.class,
                    new ArrayList<Class>(),
                    AuthenticationUtils.getInternalSessionDTO(),
                    propertyBagDTO);
            Iterator<SessionDTO> sessionIterator = sessionDTOs.iterator();

            while (sessionIterator.hasNext()) {
                SessionDTO sessionDTO = sessionIterator.next();

                if (sessionDTO == null) {
                    logger.error("Session is null - skipping - this shouldn't happen...");
                    continue;
                }

                if (sessionsToDelete.size() > PROCESS_LIMIT) {
                    break;
                }

                // internal sessionDTO never expires...
                if (AuthenticationUtils.isInternalSession(sessionDTO)) {
                    logger.debug("Session ", sessionDTO.getSessionId(), " is internal - skipping.");
                    continue;
                }
                // refesh the appDTO dto - if it is null remove the sessionDTO...
                AppDTO appDTO = null;
                try {
                    appDTO = appBO.findByPrimaryKeyMain(
                            sessionDTO.getAppDTO(),
                            new ArrayList<Class>(),
                            AuthenticationUtils.getInternalSessionDTO(),
                            new PropertyBagDTO());
                } catch (NotFoundException e) {
                    // nada
                }
                if (appDTO == null) {
                    sessionsToDelete.add(sessionDTO);
                    logger.debug("Session ", sessionDTO.getSessionId(), " queued for deletion - app is null.");
                    continue;
                }
                sessionDTO.setAppDTO(appDTO);

                // refesh the userDTO dto - if it is null remove the sessionDTO...
                UserDTO userDTO = null;
                if (sessionDTO.isProxy()) {
                    if (sessionDTO.getProxyUserDTO() == null || sessionDTO.getProxyUserDTO().getUserId() == null) {
                        sessionsToDelete.add(sessionDTO);
                        logger.debug("Proxy session ", sessionDTO.getSessionId(), " queued for deletion - proxyUserDTO is null.");
                        continue;
                    }
                    if (sessionDTO.getUserDTO() == null) {
                        sessionsToDelete.add(sessionDTO);
                        logger.debug("Proxy session ", sessionDTO.getSessionId(), " queued for deletion - sessionDTO.getUserDTO() is null.");
                        continue;
                    }
                    logger.debug(METHODNAME,
                            "looking up proxy user: ", sessionDTO.getUserDTO().getUserId(),
                            " - proxying user id: ", sessionDTO.getProxyUserDTO().getUserId(),
                            " - proxying app: ", appDTO.getAppName());

                    try {
                    userDTO = securityMGRInternal.getProxiedUserDTO(
                                sessionDTO.getUserDTO().getUserId(),
                            appDTO,
                            sessionDTO.getProxyUserDTO());
                    } catch (NotFoundException e) {
                        sessionsToDelete.add(sessionDTO);
                        logger.debug("Proxy session ", sessionDTO.getSessionId(), " queued for deletion - not found exception on proxy user: ", sessionDTO.getUserDTO().getUserId());
                        continue;
                    }
                } else {
                    try {
                    userDTO = userBO.findByPrimaryKeyMain(
                            sessionDTO.getUserDTO(),
                            new ArrayList<Class>(),
                            AuthenticationUtils.getInternalSessionDTO(),
                            new PropertyBagDTO());
                    } catch (NotFoundException e) {
                        sessionsToDelete.add(sessionDTO);
                        logger.debug("Session ", sessionDTO.getSessionId(), " queued for deletion - not found exception on UserDTO: ", sessionDTO.getUserDTO());
                        continue;
                    }
                }
                if (userDTO == null) {
                    sessionsToDelete.add(sessionDTO);
                    logger.debug("Session ", sessionDTO.getSessionId(), " queued for deletion - user is null.");
                    continue;
                }
                sessionDTO.setUserDTO(userDTO);

                if (securityMGRInternal.isSessionExpired(sessionDTO)) {
                        logger.debug(sessionDTO.getAppDTO().getAppName(),
                                " - ", sessionDTO.getUserDTO().getUsername(),
                            " is expired.");
                        logger.debug("Session ", sessionDTO.getSessionId(), " queued for deletion.");
                    sessionsToDelete.add(sessionDTO);
                } else {
                    try {
                        if (sessionDTO.isUpdated()) {
                            sessionBO.updateMain(sessionDTO, Update.class, AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
                        }
                    } catch (ConstraintViolationException cve) {
                        logger.error("Unexpected ConstraintViolationException on session update.", cve);
                    }
                }
            }

            for (SessionDTO session : sessionsToDelete) {
                try {
                    securityMGRInternal.removeSession(session);
                    logger.debug("Session ", session.getSessionId(), " has been removed.");
                } catch (NotFoundException nfe) {
                    logger.debug("Session not found: " + session.getSessionId());
                }
            }

        } catch (ConcurrentModificationException cme) {
            logger.error(METHODNAME, "ConcurrentModificationException - aborting...");
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
