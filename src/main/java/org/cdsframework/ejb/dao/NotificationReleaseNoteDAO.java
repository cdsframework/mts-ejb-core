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
package org.cdsframework.ejb.dao;

import javax.ejb.Stateless;
import org.cdsframework.base.BaseDAO;
import org.cdsframework.base.BaseDTO;
import org.cdsframework.callback.QueryCallback;
import org.cdsframework.dto.NotificationDTO;
import org.cdsframework.dto.NotificationReleaseNoteDTO;
import org.cdsframework.dto.NotificationStateDTO;
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.dto.SessionDTO;
import org.cdsframework.exceptions.MtsException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 *
 * @author sdn
 */
@Stateless
public class NotificationReleaseNoteDAO extends BaseDAO<NotificationReleaseNoteDTO> {

    @Override
    protected void initialize() throws MtsException {

        this.registerDML(NotificationReleaseNoteDTO.ByNotificationId.class, new QueryCallback(getDtoTableName()) {
            @Override
            public String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                final String METHODNAME = "ByNotificationId getQueryDML ";
                String result = getSelectDML() + " where notification_id = :notification_id";
                logger.warn(METHODNAME, "sql=", result);
                return result;
            }

            @Override
            protected void getCallbackNamedParameters(MapSqlParameterSource namedParameters, BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
                    throws MtsException {
                final String METHODNAME = "ByNotificationId getCallbackNamedParameters ";
                String predicateValue = null;
                if (baseDTO instanceof NotificationStateDTO) {
                    NotificationStateDTO notificationStateDTO = (NotificationStateDTO) baseDTO;
                    logger.warn(METHODNAME, "notificationStateDTO.getNotificationId()=", notificationStateDTO.getNotificationId());
                    predicateValue = notificationStateDTO.getNotificationId();
                } else if (baseDTO instanceof NotificationDTO) {
                    NotificationDTO notificationDTO = (NotificationDTO) baseDTO;
                    logger.warn(METHODNAME, "notificationDTO.getNotificationId()=", notificationDTO.getNotificationId());
                    predicateValue = notificationDTO.getNotificationId();
                } else if (baseDTO instanceof NotificationReleaseNoteDTO)  {
                    NotificationReleaseNoteDTO notificationReleaseNoteDTO = (NotificationReleaseNoteDTO) baseDTO;
                    logger.warn(METHODNAME, "notificationReleaseNoteDTO.getNotificationId()=", notificationReleaseNoteDTO.getNotificationId());
                    predicateValue = notificationReleaseNoteDTO.getNotificationId();
                }
                namedParameters.addValue("notification_id", predicateValue);
            }

        }, false);

    }
}
