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

import org.cdsframework.base.BaseDAO;
import org.cdsframework.base.BaseDTO;
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.dto.SecuritySchemeDTO;
import org.cdsframework.dto.SessionDTO;
import org.cdsframework.exceptions.MtsException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.cdsframework.callback.QueryCallback;
import org.cdsframework.enumeration.Operator;
import org.cdsframework.group.ByGeneralProperties;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Stateless
@LocalBean
public class SecuritySchemeDAO extends BaseDAO<SecuritySchemeDTO> {


    @Override
    protected void initialize() throws MtsException {

        this.registerDML(ByGeneralProperties.class, new QueryCallback(getDtoTableName()) {
            @Override
            public String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                setWildcardPredicateValue(baseDTO, " lower(scheme_name) like :text ", "text");
                setWildcardPredicateValue(baseDTO, " lower(description) like :text ", "text");
                String result = getSelectDML() + getAndClearPredicateMap(" where ", "", Operator.OR);
                logger.warn(result);
                return result;
            }

           @Override
           protected void getCallbackNamedParameters(MapSqlParameterSource namedParameters, BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) throws MtsException {
                    setLowerQueryMapValue(baseDTO, "text", "text", namedParameters);
           }
            
        }, false);

        this.registerDML(SecuritySchemeDTO.BySchemeName.class, new QueryCallback(getDtoTableName()) {

            @Override
            protected String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                return "select * from mt_security_scheme where scheme_name = :scheme_name";
            }
        }, false);
//
//        registerTableMapper(getDtoTableName(), new BaseRowMapper<SecuritySchemeDTO>() {
//
//            @Override
//            public MapSqlParameterSource getNamedParameters(BaseDTO baseDTO, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
//                MapSqlParameterSource namedParameters = new MapSqlParameterSource();
//                if (baseDTO == null) {
//                    logger.warn("Null baseDTO submitted. Returning empty MapSqlParameterSource.");
//                    return namedParameters;
//                }
//                if (queryClass == ByGeneralProperties.class) {
//                    setLowerQueryMapValue(baseDTO, "text", "text", namedParameters);
//                } else if (baseDTO instanceof UserDTO) {
//                    UserDTO parameter = (UserDTO) baseDTO;
//                    namedParameters.addValue("user_id", parameter.getUserId());
//                } else if (baseDTO instanceof SecuritySchemeDTO) {
//                    SecuritySchemeDTO parameter = (SecuritySchemeDTO) baseDTO;
//                    namedParameters.addValue("scheme_id", parameter.getSchemeId());
//                    namedParameters.addValue("scheme_name", parameter.getSchemeName());
//                    namedParameters.addValue("description", parameter.getDescription());
//                    namedParameters.addValue("create_datetime", parameter.getCreateDatetime());
//                    namedParameters.addValue("create_id", parameter.getCreateId());
//                    namedParameters.addValue("last_mod_datetime", parameter.getLastModDatetime());
//                    namedParameters.addValue("last_mod_id", parameter.getLastModId());
//                }
//                return namedParameters;
//            }
//
//            @Override
//            public SecuritySchemeDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
//                SecuritySchemeDTO scheme = new SecuritySchemeDTO();
//                scheme.setSchemeId(rs.getString("scheme_id"));
//                scheme.setSchemeName(rs.getString("scheme_name"));
//                scheme.setDescription(rs.getString("description"));
//                scheme.setCreateId(rs.getString("create_id"));
//                scheme.setCreateDatetime(rs.getTimestamp("create_datetime"));
//                scheme.setLastModId(rs.getString("last_mod_id"));
//                scheme.setLastModDatetime(rs.getTimestamp("last_mod_datetime"));
//                return scheme;
//            }
//        });
    }
}
