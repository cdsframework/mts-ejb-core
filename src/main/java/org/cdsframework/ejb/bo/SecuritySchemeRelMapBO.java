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
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.dto.SecuritySchemeDTO;
import org.cdsframework.dto.SecuritySchemeRelMapDTO;
import org.cdsframework.dto.SessionDTO;
import org.cdsframework.ejb.local.UserSecurityMGRLocal;
import org.cdsframework.enumeration.Operation;
import org.cdsframework.exceptions.AuthenticationException;
import org.cdsframework.exceptions.AuthorizationException;
import org.cdsframework.exceptions.MtsException;
import org.cdsframework.exceptions.ConstraintViolationException;
import org.cdsframework.exceptions.NotFoundException;
import org.cdsframework.exceptions.ValidationException;
import org.cdsframework.util.BrokenRule;

/**
 *
 * @author HLN Consulting, LLC
 */
@Stateless
public class SecuritySchemeRelMapBO extends BaseBO<SecuritySchemeRelMapDTO> {

    @EJB
    private UserSecurityMGRLocal userSecurityMGRLocal;

    @Override
    protected void validateAddOrUpdate(SecuritySchemeRelMapDTO baseDTO, Operation operation, Class queryClass, List<Class> validationClasses, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ValidationException, NotFoundException, ConstraintViolationException, MtsException, AuthenticationException, AuthorizationException {
        final String METHODNAME = "validateAddOrUpdate ";
        if (baseDTO != null) {
            SecuritySchemeDTO relatedSecuritySchemeDTO = baseDTO.getRelatedSecuritySchemeDTO();
            List<SecuritySchemeDTO> dependncyList = getDependncyList(relatedSecuritySchemeDTO);
            SecuritySchemeDTO parentDTO = propertyBagDTO.getParentDTO(SecuritySchemeDTO.class);
            if (parentDTO != null
                    && relatedSecuritySchemeDTO != null
                    && relatedSecuritySchemeDTO.getSchemeId() != null) {
                if (dependncyList.contains(relatedSecuritySchemeDTO)
                        || dependncyList.contains(parentDTO)
                        || relatedSecuritySchemeDTO.getSchemeId().equals(parentDTO.getSchemeId())) {
                    List<BrokenRule> brokenRules = new ArrayList<BrokenRule>();
                    BrokenRule brokenRule = new BrokenRule("Illegal Operation", "Cannot add a descendent or ancestor to the heirarchy.");
                    brokenRules.add(brokenRule);
                    throw new ValidationException(brokenRules);
                }
            } else {
                logger.warn(METHODNAME, "Parent DTO  or relatedSecuritySchemeDTO or relatedSecuritySchemeDTO.getSchemeId() is null!");
            }
        } else {
            logger.warn(METHODNAME, "baseDTO is null!");
        }
    }

    private List<SecuritySchemeDTO> getDependncyList(SecuritySchemeDTO securitySchemeDTO) {
        List<SecuritySchemeDTO> result = new ArrayList<SecuritySchemeDTO>();
        if (securitySchemeDTO != null) {
            for (SecuritySchemeRelMapDTO securitySchemeRelMapDTO : securitySchemeDTO.getSecuritySchemeRelMapDTOs()) {
                if (securitySchemeRelMapDTO != null) {
                    SecuritySchemeDTO relatedSecuritySchemeDTO = securitySchemeRelMapDTO.getRelatedSecuritySchemeDTO();
                    if (relatedSecuritySchemeDTO != null) {
                        result.add(relatedSecuritySchemeDTO);
                        result.addAll(getDependncyList(relatedSecuritySchemeDTO));
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected void postUpdate(SecuritySchemeRelMapDTO baseDTO, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ConstraintViolationException, NotFoundException, MtsException, ValidationException, AuthenticationException,
            AuthorizationException {
        userSecurityMGRLocal.refreshUserSecuritySchemePermissionMap(baseDTO);
    }

    @Override
    protected void postDelete(SecuritySchemeRelMapDTO baseDTO, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ConstraintViolationException, NotFoundException, MtsException, ValidationException, AuthenticationException,
            AuthorizationException {
        userSecurityMGRLocal.refreshUserSecuritySchemePermissionMap(baseDTO);
    }

    @Override
    protected void postAdd(SecuritySchemeRelMapDTO baseDTO, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ConstraintViolationException, NotFoundException, MtsException, ValidationException, AuthenticationException,
            AuthorizationException {
        userSecurityMGRLocal.refreshUserSecuritySchemePermissionMap(baseDTO);
    }

}
