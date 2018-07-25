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
package org.cdsframework.ejb.bo;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.cdsframework.base.BaseBO;
import org.cdsframework.dto.NotificationDTO;
import org.cdsframework.dto.NotificationLogDTO;
import org.cdsframework.dto.NotificationRecipientDTO;
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.dto.SessionDTO;
import org.cdsframework.enumeration.CoreErrorCode;
import org.cdsframework.enumeration.NotificationStatus;
import org.cdsframework.exceptions.AuthenticationException;
import org.cdsframework.exceptions.AuthorizationException;
import org.cdsframework.exceptions.ConstraintViolationException;
import org.cdsframework.exceptions.MtsException;
import org.cdsframework.exceptions.NotFoundException;
import org.cdsframework.exceptions.ValidationException;
import org.cdsframework.group.Add;

/**
 *
 * @author sdn
 */
@Stateless
public class NotificationBO extends BaseBO<NotificationDTO> {

    @EJB
    private NotificationLogBO notificationLogBO;

    @EJB
    private NotificationRecipientBO notificationRecipientBO;

    @Override
    protected void preUpdate(NotificationDTO baseDTO, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ConstraintViolationException, NotFoundException, MtsException, ValidationException, AuthenticationException, AuthorizationException {

        // get the notification from the db
        NotificationDTO queryDTO = new NotificationDTO();
        queryDTO.setNotificationId(baseDTO.getNotificationId());
        queryDTO = findByPrimaryKeyMain(queryDTO, new ArrayList<Class>(), sessionDTO, propertyBagDTO);

        // see if status has changed
        boolean statusChanged = false;
        if (baseDTO.getStatus() != queryDTO.getStatus()) {
            statusChanged = true;
        }
        logger.debug("preUpdate statusChanged: ", statusChanged);

        if (statusChanged) {
            if (statusChanged && baseDTO.getStatus() == NotificationStatus.SCHEDULED) {
                NotificationRecipientDTO notificationRecipientDTO =new NotificationRecipientDTO();
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
            notificationLogBO.addMain(notificationLogDTO, Add.class, sessionDTO, propertyBagDTO);
        }
    }

}
