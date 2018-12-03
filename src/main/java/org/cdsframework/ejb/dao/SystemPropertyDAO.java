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

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import org.cdsframework.base.BaseDAO;
import org.cdsframework.base.BaseDTO;
import org.cdsframework.callback.QueryCallback;
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.dto.SessionDTO;
import org.cdsframework.dto.SystemPropertyDTO;
import org.cdsframework.enumeration.DatabaseType;
import org.cdsframework.enumeration.Operator;
import org.cdsframework.exceptions.ConstraintViolationException;
import org.cdsframework.exceptions.MtsException;
import org.cdsframework.exceptions.NotFoundException;
import org.cdsframework.group.ByGeneralProperties;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 *
 * @author HLN Consulting, LLC
 */
@Stateless
@LocalBean
public class SystemPropertyDAO extends BaseDAO<SystemPropertyDTO> {

    @Override
    protected void initialize() throws MtsException {

        this.registerDML(ByGeneralProperties.class, new QueryCallback(getDtoTableName()) {
            @Override
            public String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                final String METHODNAME = "getQueryDML ";
                logger.logBegin(METHODNAME);
                String queryDML = null;
                try {
                    setWildcardPredicateValue(baseDTO, " lower(name) like :text ", "text");
                    setWildcardPredicateValue(baseDTO, " lower(type) like :text ", "text");
                    setWildcardPredicateValue(baseDTO, " lower(property_group) like :text ", "text");
                    setWildcardPredicateValue(baseDTO, " lower(scope) like :text ", "text");
                    setWildcardPredicateValue(baseDTO, " lower(value) like :text ", "text");

                    queryDML = getSelectDML() + getAndClearPredicateMap("where ( ", " ) ", Operator.OR);
                } finally {
//                    logger.debug(METHODNAME + "queryDML=" + queryDML);
                    logger.logEnd(METHODNAME);
                }
                return queryDML;
            }

            @Override
            protected void getCallbackNamedParameters(MapSqlParameterSource namedParameters, BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) throws MtsException {
                setLowerQueryMapValue(baseDTO, "text", "text", namedParameters);
            }
        }, false);

        this.registerDML(SystemPropertyDTO.ExternallyAvailable.class, new QueryCallback(getDtoTableName()) {

            @Override
            protected String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                return "select * from system_property where mts_only = :mts_only order by lower(name), lower(property_group), scope";
            }

            @Override
            protected void getCallbackNamedParameters(MapSqlParameterSource namedParameters, BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) throws MtsException {
                addBooleanValueToNamedParameters(namedParameters, "mts_only", false);
            }
        }, false);

        this.registerDML(SystemPropertyDTO.ByGroup.class, new QueryCallback(getDtoTableName()) {

            @Override
            protected String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                return "select * from system_property where property_group = :property_group order by lower(name), lower(property_group), scope";
            }
        }, false);

        this.registerDML(SystemPropertyDTO.ByScope.class, new QueryCallback(getDtoTableName()) {

            @Override
            protected String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                return "select * from system_property where scope = :scope order by lower(name), lower(property_group), scope";
            }
        }, false);

        this.registerDML(SystemPropertyDTO.ByNameScope.class, new QueryCallback(getDtoTableName()) {

            @Override
            protected String getQueryDML(BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
                if (baseDTO != null && baseDTO instanceof SystemPropertyDTO && logger.isDebugEnabled()) {
                    SystemPropertyDTO systemPropertyDTO = (SystemPropertyDTO) baseDTO;
                    logger.info("getQueryDML ByNameScope name=", systemPropertyDTO.getName());
                    logger.info("getQueryDML ByNameScope scope=", systemPropertyDTO.getScope());
                }

                return "select * from system_property where name = :name and scope = :scope order by lower(name), lower(property_group), scope";
            }
        }, false);

    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addSystemPropertyInNewTransaction(
            SystemPropertyDTO systemPropertyDTO,
            Class queryClass, SessionDTO sessionDTO,
            PropertyBagDTO propertyBagDTO)
            throws ConstraintViolationException, MtsException, NotFoundException {
        add(systemPropertyDTO, queryClass, sessionDTO, propertyBagDTO);
    }
}
