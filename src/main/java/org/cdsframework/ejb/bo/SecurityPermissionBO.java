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

import java.util.Iterator;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.cdsframework.base.BaseBO;
import org.cdsframework.base.BaseDTO;
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.dto.SecurityPermissionDTO;
import org.cdsframework.dto.SessionDTO;
import org.cdsframework.ejb.local.UserSecurityMGRLocal;
import org.cdsframework.enumeration.Operation;
import org.cdsframework.exceptions.AuthenticationException;
import org.cdsframework.exceptions.AuthorizationException;
import org.cdsframework.exceptions.ConstraintViolationException;
import org.cdsframework.exceptions.MtsException;
import org.cdsframework.exceptions.NotFoundException;
import org.cdsframework.exceptions.ValidationException;

@Stateless
public class SecurityPermissionBO extends BaseBO<SecurityPermissionDTO> {

    @EJB
    private UserSecurityMGRLocal userSecurityMGRLocal;

    @Override
    protected void postProcessBaseDTOs(BaseDTO parentDTO, List<SecurityPermissionDTO> baseDTOs, Operation operation, Class queryClass,
            List<Class> validationClasses, List<Class> childClassDTOs, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws MtsException, ValidationException, NotFoundException, AuthenticationException, AuthorizationException {
        final String METHODNAME = "postProcessBaseDTOs ";
        Iterator<SecurityPermissionDTO> iterator = baseDTOs.iterator();
        while (iterator.hasNext()) {
            SecurityPermissionDTO item = iterator.next();
            if (item.getPermissionClass() == null || item.getPermissionType() == null) {
                logger.error(METHODNAME, "Bad permission: ", item.getMapId(), " - ", item.getPermissionClass(), " - ", item.getPermissionType());
                iterator.remove();
            }
        }
    }

    @Override
    protected void postUpdate(SecurityPermissionDTO baseDTO, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ConstraintViolationException, NotFoundException, MtsException, ValidationException, AuthenticationException,
            AuthorizationException {
        userSecurityMGRLocal.refreshUserSecuritySchemePermissionMap(baseDTO);
    }

    @Override
    protected void postDelete(SecurityPermissionDTO baseDTO, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ConstraintViolationException, NotFoundException, MtsException, ValidationException, AuthenticationException,
            AuthorizationException {
        userSecurityMGRLocal.refreshUserSecuritySchemePermissionMap(baseDTO);
    }

    @Override
    protected void postAdd(SecurityPermissionDTO baseDTO, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ConstraintViolationException, NotFoundException, MtsException, ValidationException, AuthenticationException,
            AuthorizationException {
        userSecurityMGRLocal.refreshUserSecuritySchemePermissionMap(baseDTO);
    }
}
