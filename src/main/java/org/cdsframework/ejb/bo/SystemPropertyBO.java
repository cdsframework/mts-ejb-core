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

import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.cdsframework.base.BaseBO;
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.dto.SessionDTO;
import org.cdsframework.dto.SystemPropertyDTO;
import org.cdsframework.dto.UserDTO;
import org.cdsframework.ejb.local.PropertyMGRLocal;
import org.cdsframework.exceptions.AuthenticationException;
import org.cdsframework.exceptions.AuthorizationException;
import org.cdsframework.exceptions.MtsException;
import org.cdsframework.exceptions.ConstraintViolationException;
import org.cdsframework.exceptions.NotFoundException;
import org.cdsframework.exceptions.ValidationException;

/**
 *
 * @author HLN Consulting, LLC
 */
@Stateless
public class SystemPropertyBO extends BaseBO<SystemPropertyDTO> {

    @EJB
    private PropertyMGRLocal propertyMGRLocal;

    @Override
    protected void postAdd(SystemPropertyDTO baseDTO, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) throws
            ConstraintViolationException,
            NotFoundException, MtsException, ValidationException, AuthenticationException, AuthorizationException {
        updatePropertyMGR(baseDTO, sessionDTO);
    }

    @Override
    protected void postUpdate(SystemPropertyDTO baseDTO, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ConstraintViolationException, NotFoundException, MtsException, ValidationException, AuthenticationException,
            AuthorizationException {
        updatePropertyMGR(baseDTO, sessionDTO);
    }

    private void updatePropertyMGR(SystemPropertyDTO systemPropertyDTO, SessionDTO sessionDTO) throws MtsException, ValidationException {
        final String METHODNAME = "updatePropertyMGR ";
        if (sessionDTO == null) {
            logger.error(METHODNAME, "SessionDTO was null!");
            return;
        }
        String username = null;
        UserDTO userDTO = sessionDTO.getUserDTO();
        if (userDTO == null) {
            logger.error(METHODNAME, "userDTO was null!");
        } else {
            username = userDTO.getUsername();
            if (username == null) {
                logger.error(METHODNAME, "username was null!");
            }
        }
        try {
            propertyMGRLocal.registerPropertyWithConversion(systemPropertyDTO.getName(),
                    systemPropertyDTO.getValue(),
                    Class.forName(systemPropertyDTO.getType()),
                    systemPropertyDTO.isObscure());
        } catch (ClassNotFoundException e) {
            logger.error(METHODNAME, e.getMessage());
        }
        logger.debug("Updated property: ", systemPropertyDTO.getName(), " to ", systemPropertyDTO.isObscure() ? "********" : systemPropertyDTO.getValue(), " by ", username);
    }
}
