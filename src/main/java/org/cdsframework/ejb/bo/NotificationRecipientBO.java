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
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.cdsframework.base.BaseBO;
import org.cdsframework.dto.NotificationRecipientDTO;
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.dto.SecuritySchemeDTO;
import org.cdsframework.dto.SessionDTO;
import org.cdsframework.dto.UserDTO;
import org.cdsframework.enumeration.NotificationRecipientType;
import org.cdsframework.enumeration.Operation;
import org.cdsframework.exceptions.AuthenticationException;
import org.cdsframework.exceptions.AuthorizationException;
import org.cdsframework.exceptions.ConstraintViolationException;
import org.cdsframework.exceptions.MtsException;
import org.cdsframework.exceptions.NotFoundException;
import org.cdsframework.exceptions.ValidationException;

/**
 *
 * @author sdn
 */
@Stateless
public class NotificationRecipientBO extends BaseBO<NotificationRecipientDTO> {

    @EJB
    private NotificationStateBO notificationStateBO;

    @Override
    protected Map<String, Object> preAddOrUpdate(NotificationRecipientDTO baseDTO, Operation operation, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ValidationException, NotFoundException, ConstraintViolationException, MtsException, AuthenticationException, AuthorizationException {
        if (baseDTO.getRecipientType() == NotificationRecipientType.USER && baseDTO.getSecuritySchemeDTO() != null) {
            baseDTO.setSecuritySchemeDTO(null);
        } else if (baseDTO.getRecipientType() == NotificationRecipientType.GROUP && baseDTO.getUserDTO() != null) {
            baseDTO.setUserDTO(null);
        }
        return super.preAddOrUpdate(baseDTO, operation, queryClass, sessionDTO, propertyBagDTO);
    }

    @Override
    protected void preDelete(NotificationRecipientDTO baseDTO, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ConstraintViolationException, NotFoundException, MtsException, ValidationException, AuthenticationException, AuthorizationException {
        notificationStateBO.deleteNotificationStateDTO(baseDTO.getRecipientId(), sessionDTO, propertyBagDTO);
    }

    @Override
    protected void preUpdate(NotificationRecipientDTO baseDTO, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ConstraintViolationException, NotFoundException, MtsException, ValidationException, AuthenticationException, AuthorizationException {
        NotificationRecipientDTO queryDTO = new NotificationRecipientDTO();
        queryDTO.setRecipientId(baseDTO.getRecipientId());
        queryDTO = findByPrimaryKeyMain(queryDTO, new ArrayList<Class>(), sessionDTO, propertyBagDTO);

        // check if the userdto changed - if so potentially add the new mapping and delete the old one
        if (baseDTO.getUserDTO() == null) {
            if (queryDTO.getUserDTO() != null) {
                // userdto was nulled - delete the old mapping
                notificationStateBO.deleteNotificationStateDTO(queryDTO.getUserDTO(), queryDTO.getRecipientId(), sessionDTO, propertyBagDTO);
            }
        } else {
            if (!baseDTO.getUserDTO().equals(queryDTO.getUserDTO())) {
                if (queryDTO.getUserDTO() != null && queryDTO.getUserDTO().getUserId() != null) {
                    // delete the old mapping
                    notificationStateBO.deleteNotificationStateDTO(queryDTO.getUserDTO(), queryDTO.getRecipientId(), sessionDTO, propertyBagDTO);
                }
                if (baseDTO.getUserDTO() != null && baseDTO.getUserDTO().getUserId() != null) {
                    // add a new user mapping
                    notificationStateBO.addNotificationStateDTO(baseDTO.getUserDTO(), baseDTO.getRecipientId(), sessionDTO, propertyBagDTO);
                }
            }
        }

        // check if security scheme changed
        if (baseDTO.getSecuritySchemeDTO() == null) {
            if (queryDTO.getSecuritySchemeDTO() != null) {
                // scheme dto was nulled - delete the old mapping
                notificationStateBO.deleteNotificationStateDTO(queryDTO.getRecipientId(), sessionDTO, propertyBagDTO);
            }
        } else {
            if (!baseDTO.getSecuritySchemeDTO().equals(queryDTO.getSecuritySchemeDTO())) {
                if (queryDTO.getSecuritySchemeDTO() != null) {
                    // delete the old mapping
                    notificationStateBO.deleteNotificationStateDTO(queryDTO.getRecipientId(), sessionDTO, propertyBagDTO);
                }
                if (baseDTO.getSecuritySchemeDTO() != null) {
                    // add a new user mapping
                    notificationStateBO.addNotificationStateDTO(baseDTO.getUserDTO(), baseDTO.getRecipientId(), sessionDTO, propertyBagDTO);
                }
            }
        }
    }

    @Override
    protected void postAdd(NotificationRecipientDTO baseDTO, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ConstraintViolationException, NotFoundException, MtsException, ValidationException, AuthenticationException, AuthorizationException {

        // group mapping
        SecuritySchemeDTO securitySchemeDTO = baseDTO.getSecuritySchemeDTO();
        if (securitySchemeDTO != null) {
            notificationStateBO.addNotificationStateDTO(securitySchemeDTO, baseDTO.getRecipientId(), sessionDTO, propertyBagDTO);
        }

        // just a single user mapping
        UserDTO userDTO = baseDTO.getUserDTO();
        if (userDTO != null) {
            notificationStateBO.addNotificationStateDTO(userDTO, baseDTO.getRecipientId(), sessionDTO, propertyBagDTO);
        }
    }

}
