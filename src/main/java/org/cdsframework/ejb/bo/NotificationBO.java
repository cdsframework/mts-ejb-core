/**
 * The MTS core EJB project is the base framework for the CDS Framework Middle Tier Service.
 *
 * Copyright (C) 2016 New York City Department of Health and Mental Hygiene, Bureau of Immunization
 * Contributions by HLN Consulting, LLC
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. You should have received a copy of the GNU Lesser
 * General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/> for more details.
 *
 * The above-named contributors (HLN Consulting, LLC) are also licensed by the
 * New York City Department of Health and Mental Hygiene, Bureau of Immunization
 * to have (without restriction, limitation, and warranty) complete irrevocable
 * access and rights to this project.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; THE SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE COPYRIGHT HOLDERS, IF ANY, OR DEVELOPERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES, OR OTHER LIABILITY OF ANY KIND, ARISING FROM, OUT OF, OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information about this software, see
 * https://www.hln.com/services/open-source/ or send correspondence to
 * ice@hln.com.
 */
package org.cdsframework.ejb.bo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.commons.lang3.StringUtils;
import org.cdsframework.base.BaseBO;
import org.cdsframework.dto.NotificationDTO;
import org.cdsframework.dto.NotificationLogDTO;
import org.cdsframework.dto.NotificationRecipientDTO;
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.dto.SessionDTO;
import org.cdsframework.enumeration.NotificationStatus;
import org.cdsframework.enumeration.Operation;
import org.cdsframework.exceptions.AuthenticationException;
import org.cdsframework.exceptions.AuthorizationException;
import org.cdsframework.exceptions.ConstraintViolationException;
import org.cdsframework.exceptions.MtsException;
import org.cdsframework.exceptions.NotFoundException;
import org.cdsframework.exceptions.ValidationException;

/**
 *
 * @author sdn
 */
@Stateless
public class NotificationBO extends BaseBO<NotificationDTO> {

    @EJB
    private NotificationRecipientBO notificationRecipientBO;

    @Override
    protected Map<String, Object> preAddOrUpdate(NotificationDTO baseDTO, Operation operation, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ValidationException, NotFoundException, ConstraintViolationException, MtsException, AuthenticationException, AuthorizationException {
        final String METHODNAME = "preAddOrUpdate ";

        boolean statusChanged = false;
        Boolean timerCall = propertyBagDTO.get("TIMER_CALL", false);

        if (operation != Operation.ADD && !StringUtils.isEmpty(baseDTO.getNotificationId())) {
            // get the notification from the db
            NotificationDTO queryDTO = new NotificationDTO();
            queryDTO.setNotificationId(baseDTO.getNotificationId());
            try {
                queryDTO = findByPrimaryKeyMain(queryDTO, new ArrayList<Class>(), sessionDTO, propertyBagDTO);
            } catch (NotFoundException e) {

            }

            // see if status has changed
            if (baseDTO.getStatus() != queryDTO.getStatus()) {
                statusChanged = true;
            }
        }

        logger.debug(METHODNAME, "preUpdate statusChanged: ", statusChanged);

        if (statusChanged || operation == Operation.ADD) {
            if (baseDTO.getStatus() == NotificationStatus.SCHEDULED) {
                NotificationRecipientDTO notificationRecipientDTO = new NotificationRecipientDTO();
                notificationRecipientDTO.setNotificationId(baseDTO.getNotificationId());
                List<NotificationRecipientDTO> notificationRecipientDTOs = notificationRecipientBO.findByQueryListMain(notificationRecipientDTO, NotificationRecipientDTO.ByNotificationId.class, new ArrayList<Class>(), sessionDTO, propertyBagDTO);
                if (notificationRecipientDTOs.isEmpty() && baseDTO.getNotificationRecipientDTOs().isEmpty()) {
                    throw new ValidationException("There are no recipients for the notification. Recipients must be added in order to schedule a notification.");
                }
            }
            // log it
            NotificationLogDTO notificationLogDTO = new NotificationLogDTO();
            notificationLogDTO.setNotificationId(baseDTO.getNotificationId());
            notificationLogDTO.setStatus(baseDTO.getStatus());
            notificationLogDTO.setType(baseDTO.getType());
            baseDTO.addOrUpdateChildDTO(notificationLogDTO);
        }

        if (!timerCall && (baseDTO.getStatus() == NotificationStatus.DRAFT || baseDTO.getStatus() == NotificationStatus.SCHEDULED)) {
            Date epochDate;
            try {
                epochDate = new SimpleDateFormat("MM/dd/yyyy").parse("01/03/1970");
            } catch (ParseException e) {
                epochDate = new Date();
            }

            logger.warn(METHODNAME, "baseDTO.getNotificationTime(): ", baseDTO.getNotificationTime());

            if (baseDTO.getNotificationTime() != null && baseDTO.getNotificationTime().before(epochDate)) {
                baseDTO.setNotificationTime(null);
                throw new ValidationException("The notifcation date cannot be empty.");
            }

            if (baseDTO.getNotificationTime() != null && baseDTO.getNotificationTime().before(new Date())) {
                throw new ValidationException("The notifcation date cannot be in the past.");
            }
        }

        return super.preAddOrUpdate(baseDTO, operation, queryClass, sessionDTO, propertyBagDTO);
    }

}
