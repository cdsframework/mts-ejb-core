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
import java.util.Date;
import java.util.List;
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
import org.cdsframework.dto.NotificationDTO;
import org.cdsframework.dto.NotificationStateDTO;
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.ejb.bo.NotificationBO;
import org.cdsframework.ejb.bo.NotificationStateBO;
import org.cdsframework.ejb.local.PropertyMGRLocal;
import org.cdsframework.enumeration.NotificationState;
import org.cdsframework.enumeration.NotificationStatus;
import org.cdsframework.exceptions.AuthenticationException;
import org.cdsframework.exceptions.AuthorizationException;
import org.cdsframework.exceptions.ConstraintViolationException;
import org.cdsframework.exceptions.MtsException;
import org.cdsframework.exceptions.NotFoundException;
import org.cdsframework.exceptions.ValidationException;
import org.cdsframework.group.Update;
import org.cdsframework.util.AuthenticationUtils;
import org.cdsframework.util.LogUtils;

/**
 *
 * @author sdn
 */
@Startup
@Singleton
@LocalBean
@DependsOn({"CorePlugin"})
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class NotificationMgrTimer {

    private static final LogUtils logger = LogUtils.getLogger(NotificationMgrTimer.class);

    @Resource
    private SessionContext sessionCtx;

    @EJB
    private PropertyMGRLocal propertyMGRLocal;

    @EJB
    private NotificationBO notificationBO;

    @EJB
    private NotificationStateBO notificationStateBO;

    @PostConstruct
    public void postConstructor() {
        final String METHODNAME = "postConstructor ";
        logger.logBegin(METHODNAME);

        boolean notificationsEnabled = propertyMGRLocal.get("NOTIFICATIONS_ENABLED", Boolean.class);
        logger.info(METHODNAME, "notificationsEnabled=", notificationsEnabled);
        if (notificationsEnabled) {
            TimerConfig timerConfig = new TimerConfig();
            timerConfig.setPersistent(false);
            timerConfig.setInfo("Session expiration timer");
            ScheduleExpression scheduleExpression = new ScheduleExpression();
            scheduleExpression.hour("*");
            scheduleExpression.minute("*");
            scheduleExpression.second("*/30");
            sessionCtx.getTimerService().createCalendarTimer(scheduleExpression, timerConfig);
            logger.info("NotificationMgrTimer enabled");
        }
    }

    @Timeout
    private void processNotifications() {
        final String METHODNAME = "processNotifications ";
        logger.debug(METHODNAME, "processing notifications...");
        try {
            processScheduled();
            processCanceled();
            processInactivated();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void processScheduled() {
        final String METHODNAME = "processScheduled ";
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setStatus(NotificationStatus.SCHEDULED);
        try {
            List<NotificationDTO> notificationDTOs = notificationBO.findByQueryListMain(notificationDTO, NotificationDTO.ByNotificationStatus.class, new ArrayList<Class>(), AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
            for (NotificationDTO item : notificationDTOs) {

                // process the notifications
                if (item.getNotificationTime() == null || item.getNotificationTime().before(new Date())) {
                    scheduleNotification(item);
                }
            }
        } catch (AuthenticationException | AuthorizationException | MtsException | NotFoundException | ValidationException | ConstraintViolationException e) {
            logger.error(e);
        }
    }

    private void scheduleNotification(NotificationDTO notificationDTO)
            throws ValidationException, NotFoundException, MtsException, AuthenticationException, AuthorizationException, ConstraintViolationException {
        final String METHODNAME = "scheduleNotification ";
        NotificationStateDTO notificationStateDTO = new NotificationStateDTO();
        PropertyBagDTO propertyBagDTO = new PropertyBagDTO();
        propertyBagDTO.put("notification_id", notificationDTO.getNotificationId());
        List<NotificationStateDTO> notificationStateDTOs = notificationStateBO.findByQueryListMain(notificationStateDTO, NotificationStateDTO.ByNotificationId.class, new ArrayList<Class>(), AuthenticationUtils.getInternalSessionDTO(), propertyBagDTO);
        for (NotificationStateDTO userState : notificationStateDTOs) {
            userState.setState(NotificationState.UNREAD);
            notificationStateBO.updateMain(userState, Update.class, AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
        }
        notificationDTO.setStatus(NotificationStatus.SENT);
        PropertyBagDTO updatePropertyBagDTO = new PropertyBagDTO();
        updatePropertyBagDTO.put("TIMER_CALL", true);
        notificationBO.updateMain(notificationDTO, Update.class, AuthenticationUtils.getInternalSessionDTO(), updatePropertyBagDTO);
    }

    private void processCanceled() {
        final String METHODNAME = "processCanceled ";
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setStatus(NotificationStatus.CANCELED);
        try {
            List<NotificationDTO> notificationDTOs = notificationBO.findByQueryListMain(notificationDTO, NotificationDTO.ByNotificationStatus.class, new ArrayList<Class>(), AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
            for (NotificationDTO item : notificationDTOs) {
                inactivateNotification(item);
            }
        } catch (AuthenticationException | AuthorizationException | MtsException | NotFoundException | ValidationException | ConstraintViolationException e) {
            logger.error(e);
        }
    }

    private void processInactivated() {
        final String METHODNAME = "processInactivated ";
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setStatus(NotificationStatus.INACTIVATED);
        try {
            List<NotificationDTO> notificationDTOs = notificationBO.findByQueryListMain(notificationDTO, NotificationDTO.ByNotificationStatus.class, new ArrayList<Class>(), AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
            for (NotificationDTO item : notificationDTOs) {
                inactivateNotification(item);
            }
        } catch (AuthenticationException | AuthorizationException | MtsException | NotFoundException | ValidationException | ConstraintViolationException e) {
            logger.error(e);
        }
    }

    private void inactivateNotification(NotificationDTO notificationDTO)
            throws ValidationException, NotFoundException, MtsException, AuthenticationException, AuthorizationException, ConstraintViolationException {
        final String METHODNAME = "inactivateNotification ";
        NotificationStateDTO notificationStateDTO = new NotificationStateDTO();
        PropertyBagDTO propertyBagDTO = new PropertyBagDTO();
        propertyBagDTO.put("notification_id", notificationDTO.getNotificationId());
        List<NotificationStateDTO> notificationStateDTOs = notificationStateBO.findByQueryListMain(notificationStateDTO, NotificationStateDTO.ByNotificationId.class, new ArrayList<Class>(), AuthenticationUtils.getInternalSessionDTO(), propertyBagDTO);
        for (NotificationStateDTO userState : notificationStateDTOs) {
            if (userState.getState() != NotificationState.INACTIVATED) {
                userState.setState(NotificationState.INACTIVATED);
                notificationStateBO.updateMain(userState, Update.class, AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
            }
        }
    }
}
