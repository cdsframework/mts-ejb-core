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
import org.cdsframework.dto.AppDTO;
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.dto.SessionDTO;
import org.cdsframework.enumeration.Operator;
import org.cdsframework.exceptions.MtsException;
import org.cdsframework.group.ByGeneralProperties;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Stateless
@LocalBean
public class AppDAO extends BaseDAO<AppDTO> {

    @Override
    protected void initialize() throws MtsException {

        // General search query
        this.registerDML(ByGeneralProperties.class, new QueryCallback(getDtoTableName()) {
            @Override
            public String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                String sql;
                String text = (String) baseDTO.getQueryMap().get("text");
                String appId = (String) baseDTO.getQueryMap().get("app_id");
                logger.debug("ByGeneralProperties text: ", text);
                logger.debug("ByGeneralProperties appId: ", appId);
                if ((text != null && !text.trim().isEmpty())) {
                    setWildcardPredicateValue(baseDTO, " (lower(app_name) like :text OR lower(description) like :text) ", "text");
                    sql = "select * from mt_app where " + getAndClearPredicateMap("", "", Operator.AND);
                } else {
                    sql = getSelectDML();
                }
                logger.debug(sql);
                return sql;
            }

            @Override
            protected void getCallbackNamedParameters(MapSqlParameterSource namedParameters, BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) throws MtsException {
                String text = (String) baseDTO.getQueryMap().get("text");
                if (text != null && !text.trim().isEmpty()) {
                    setLowerQueryMapValue(baseDTO, "text", "text", namedParameters);
                    logger.debug("nameparameter text: ", namedParameters.getValue("text"));
                }
            }

        }, false);

        this.registerDML(AppDTO.AllAppNames.class, new QueryCallback(getDtoTableName()) {

            @Override
            protected String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                return "select app_name from mt_app";
            }
        }, false);

        this.registerDML(AppDTO.DtoByAppName.class, new QueryCallback(getDtoTableName()) {

            @Override
            protected String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                return "select * from mt_app where lower(app_name) = lower(:app_name)";
            }
        }, false);

        this.registerDML(AppDTO.AppNameByAppId.class, new QueryCallback(getDtoTableName()) {

            @Override
            protected String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                return "select app_name from mt_app where app_id = :app_id";
            }
        }, false);

    }
}
