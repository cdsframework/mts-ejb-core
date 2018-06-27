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

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.cdsframework.base.BaseDAO;
import org.cdsframework.base.BaseDTO;
import org.cdsframework.callback.QueryCallback;
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.dto.SessionDTO;
import org.cdsframework.enumeration.DatabaseType;
import org.cdsframework.enumeration.Operator;
import org.cdsframework.exceptions.MtsException;
import org.cdsframework.group.ByGeneralProperties;
import org.cdsframework.group.FindAll;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Stateless
@LocalBean
public class SessionDAO extends BaseDAO<SessionDTO> {

    @Override
    protected void initialize() throws MtsException {

        registerDML(ByGeneralProperties.class, new QueryCallback(getDtoTableName()) {

            @Override
            protected String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                logger.debug("ByGeneralProperties queryMapDTO: ", baseDTO.getQueryMap());
                setWildcardPredicateValue(baseDTO, " lower(a.app_name) like :app_name ", "text");
                setWildcardPredicateValue(baseDTO, " lower(u.username) like :username ", "text");

                String sql = "SELECT distinct s.* FROM mt_session s, mt_app a, mt_user u where s.app_id = a.app_id and s.user_id = u.user_id "
                        + getAndClearPredicateMap(" and ( ", " ) ", Operator.OR) + " order by s.last_mod_datetime desc ";
                logger.debug("ByGeneralProperties sql: ", sql);
                return sql;
            }

            @Override
            protected void getCallbackNamedParameters(MapSqlParameterSource namedParameters, BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) throws MtsException {
                setLowerQueryMapValue(baseDTO, "create_datetime", "text", namedParameters);
                setLowerQueryMapValue(baseDTO, "last_mod_datetime", "text", namedParameters);
                setLowerQueryMapValue(baseDTO, "app_name", "text", namedParameters);
                setLowerQueryMapValue(baseDTO, "username", "text", namedParameters);
            }

        }, false);

        registerDML(SessionDTO.FindCountBySessionId.class, new QueryCallback(getDtoTableName()) {

            @Override
            protected String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                String sql = "select count(*) from mt_session where session_id = :session_id";
                logger.debug("SessionDTO.FindCountBySessionId sql: ", sql);
                return sql;
            }

            @Override
            protected void getCallbackNamedParameters(MapSqlParameterSource namedParameters, BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) throws MtsException {
                SessionDTO parameter = (SessionDTO) baseDTO;
                namedParameters.addValue("session_id", parameter.getSessionId());
            }

        }, false);

        registerDML(FindAll.class, new QueryCallback(getDtoTableName()) {

            @Override
            protected String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                return "select * from mt_session order by last_mod_datetime desc";
            }
        }, false);
    
        registerDML(SessionDTO.FindAllReverse.class, new QueryCallback(getDtoTableName()) {

            @Override
            protected String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                return "select * from mt_session order by last_mod_datetime asc";
            }
        }, false);


    }

    @Override
    protected void postProcessNamedParameters(DatabaseType databaseType, MapSqlParameterSource namedParameters, BaseDTO baseDTO, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
        if (baseDTO != null && baseDTO instanceof SessionDTO) {
            SessionDTO parameter = (SessionDTO) baseDTO;
            if (!parameter.isDeleted() && parameter.isProxy() && (parameter.getProxyUserDTO() == null || parameter.getProxyUserDTO().getUserId() == null)) {
                throw new IllegalArgumentException("Proxy session has null proxyUserDTO!");
            }
        }
    }

}
