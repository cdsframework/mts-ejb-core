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
package org.cdsframework.ejb.dao;

import javax.ejb.Stateless;
import org.cdsframework.base.BaseDAO;
import org.cdsframework.base.BaseDTO;
import org.cdsframework.callback.QueryCallback;
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
public class NotificationStateDAO extends BaseDAO<NotificationStateDTO> {

    @Override
    protected void initialize() throws MtsException {

        this.registerDML(NotificationStateDTO.ByNotificationId.class, new QueryCallback(getDtoTableName()) {

            @Override
            protected String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                return "select * from vw_notification_state where notification_id = :notification_id";
            }

            @Override
            protected void getCallbackNamedParameters(MapSqlParameterSource namedParameters, BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
                    throws MtsException {
                String notificationId = propertyBagDTO.get("notification_id", String.class);
                logger.warn("getCallbackNamedParameters ByNotificationId notificationId=", notificationId);
                namedParameters.addValue("notification_id", notificationId);
            }
        }, false);

        this.registerDML(NotificationStateDTO.ByRecipientIdUserId.class, new QueryCallback(getDtoTableName()) {

            @Override
            protected String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                return "select * from vw_notification_state where recipient_id = :recipient_id and user_id = :user_id";
            }
        }, false);

        this.registerDML(NotificationStateDTO.ByRecipientId.class, new QueryCallback(getDtoTableName()) {

            @Override
            protected String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                return "select * from vw_notification_state where recipient_id = :recipient_id";
            }
        }, false);

        this.registerDML(NotificationStateDTO.ByUserId.class, new QueryCallback(getDtoTableName()) {

            @Override
            protected String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                return "select * from vw_notification_state where user_id = :user_id";
            }

            @Override
            protected void getCallbackNamedParameters(MapSqlParameterSource namedParameters, BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
                    throws MtsException {
                String userId = propertyBagDTO.get("user_id", String.class);
                logger.warn("getCallbackNamedParameters ByUserId userId=", userId);
                namedParameters.addValue("user_id", userId);
            }

        }, false);

        this.registerDML(NotificationStateDTO.UnreadMessageCount.class, new QueryCallback(getDtoTableName()) {

            @Override
            protected String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                return "select count(*) from vw_notification_state where user_id = :user_id and state = 'UNREAD' "
                        + " and (notification_type = 'INBOX' or notification_type = 'DASHBOARD_INBOX')";
            }

            @Override
            protected void getCallbackNamedParameters(MapSqlParameterSource namedParameters, BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
                    throws MtsException {
                String userId = propertyBagDTO.get("user_id", String.class);
                logger.warn("getCallbackNamedParameters UnreadMessageCount userId=", userId);
                namedParameters.addValue("user_id", userId);
            }

        }, false);

        this.registerDML(NotificationStateDTO.ByDashboard.class, new QueryCallback(getDtoTableName()) {

            @Override
            protected String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                return "select * from vw_notification_state where user_id = :user_id "
                        + " and (state = 'READ' or state = 'UNREAD') "
                        + " and (notification_type = 'DASHBOARD_ONLY' or notification_type = 'DASHBOARD_INBOX')";
            }


            @Override
            protected void getCallbackNamedParameters(MapSqlParameterSource namedParameters, BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
                    throws MtsException {
                String userId = propertyBagDTO.get("user_id", String.class);
                logger.warn("getCallbackNamedParameters ByDashboard userId=", userId);
                namedParameters.addValue("user_id", userId);
            }

        }, false);

        this.registerDML(NotificationStateDTO.ByInbox.class, new QueryCallback(getDtoTableName()) {

            @Override
            protected String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                return "select * from vw_notification_state where user_id = :user_id "
                        + " and (state = 'READ' or state = 'UNREAD') "
                        + " and (notification_type = 'INBOX' or notification_type = 'DASHBOARD_INBOX')";
            }


            @Override
            protected void getCallbackNamedParameters(MapSqlParameterSource namedParameters, BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
                    throws MtsException {
                String userId = propertyBagDTO.get("user_id", String.class);
                logger.warn("getCallbackNamedParameters ByInbox userId=", userId);
                namedParameters.addValue("user_id", userId);
            }

        }, false);

    }
}
