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
import org.cdsframework.dto.UserDTO;
import org.cdsframework.enumeration.Operator;
import org.cdsframework.exceptions.MtsException;
import org.cdsframework.group.ByGeneralProperties;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Stateless
@LocalBean
public class UserDAO extends BaseDAO<UserDTO> {

    @Override
    protected void initialize() throws MtsException {

        setUpdateDML("UPDATE mt_user SET prefix = :prefix, last_name = :last_name, last_mod_datetime = :last_mod_datetime, middle_name = :middle_name, suffix = :suffix, failed_login_attempts = :failed_login_attempts, expiration_date = :expiration_date, change_password = :change_password, last_mod_id = :last_mod_id, app_proxy_user = :app_proxy_user, proxy_app_id = :proxy_app_id, user_id = :user_id, phone = :phone, disabled = :disabled, first_name = :first_name, email = :email, username = :username WHERE user_id = :user_id");

        this.registerDML(UserDTO.UpdatePasswordHash.class, new QueryCallback(getDtoTableName()) {
            @Override
            public String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                String sql = "update mt_user set change_password = 'N', password_hash = :password_hash, "
                        + "last_mod_id = :last_mod_id, last_mod_datetime = :last_mod_datetime where user_id = :user_id";
//                logger.warn(sql);
                return sql;
            }
        }, false);

        
        this.registerDML(UserDTO.DtoByUsername.class, new QueryCallback(getDtoTableName()) {
            @Override
            public String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                String sql = "select * from mt_user where username = :username";
//                logger.warn(sql);
                return sql;
            }
        }, false);

        
        this.registerDML(UserDTO.FailedLoginAttemptsByUserId.class, new QueryCallback(getDtoTableName()) {
            @Override
            public String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                String sql = "select failed_login_attempts from mt_user where user_id = :user_id";
//                logger.warn(sql);
                return sql;
            }
        }, false);

        
        this.registerDML(UserDTO.FindCatProxyUser.class, new QueryCallback(getDtoTableName()) {
            @Override
            public String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                String sql = "select * from mt_user where username = :username or user_id = :username";
//                logger.warn(sql);
                return sql;
            }

            @Override
            protected void getCallbackNamedParameters(MapSqlParameterSource namedParameters, BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) throws MtsException {
                UserDTO parameter = (UserDTO) baseDTO;
                namedParameters.addValue("username", parameter.getUserId());
            }

        }, false);

        
        this.registerDML(UserDTO.ByUserId.class, new QueryCallback(getDtoTableName()) {
            @Override
            public String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                String sql = "select * from mt_user where user_id = :user_id";
//                logger.warn(sql);
                return sql;
            }

            @Override
            protected void getCallbackNamedParameters(MapSqlParameterSource namedParameters, BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) throws MtsException {
                final String METHODNAME = "getCallbackNamedParameters ";
                if (baseDTO instanceof SessionDTO) {
                    SessionDTO parameter = (SessionDTO) baseDTO;
                    if (queryClass == UserDTO.ByUserId.class) {
                        namedParameters.addValue("user_id", parameter.getUserDTO().getUserId());
                    }
                }
                else if (baseDTO instanceof UserDTO) {
                    UserDTO parameter = (UserDTO) baseDTO;
                    if (queryClass == UserDTO.ByUserId.class) {
                        namedParameters.addValue("user_id", parameter.getUserId());
                    }
                } else {
                    logger.error(METHODNAME, "Got wrong DTO type: ", baseDTO);
                }
            }

        }, false);

        
        this.registerDML(ByGeneralProperties.class, new QueryCallback(getDtoTableName()) {
            @Override
            public String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                setWildcardPredicateValue(baseDTO, " lower(username) like :text ", "text");
                setWildcardPredicateValue(baseDTO, " lower(first_name) like :text ", "text");
                setWildcardPredicateValue(baseDTO, " lower(middle_name) like :text ", "text");
                setWildcardPredicateValue(baseDTO, " lower(last_name) like :text ", "text");
                String result = getSelectDML() + getAndClearPredicateMap(" where ", "", Operator.OR);
                logger.warn(result);
                return result;
            }

            @Override
            protected void getCallbackNamedParameters(MapSqlParameterSource namedParameters, BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) throws MtsException {
                setLowerQueryMapValue(baseDTO, "text", "text", namedParameters);
            }

        }, false);
    }
}
