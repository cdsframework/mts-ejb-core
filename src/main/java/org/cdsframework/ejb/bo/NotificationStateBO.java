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
import org.cdsframework.dto.NotificationStateDTO;
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.dto.SecuritySchemeDTO;
import org.cdsframework.dto.SecuritySchemeRelMapDTO;
import org.cdsframework.dto.SessionDTO;
import org.cdsframework.dto.UserDTO;
import org.cdsframework.dto.UserSecurityMapDTO;
import org.cdsframework.enumeration.NotificationState;
import org.cdsframework.exceptions.AuthenticationException;
import org.cdsframework.exceptions.AuthorizationException;
import org.cdsframework.exceptions.ConstraintViolationException;
import org.cdsframework.exceptions.MtsException;
import org.cdsframework.exceptions.NotFoundException;
import org.cdsframework.exceptions.ValidationException;
import org.cdsframework.group.Add;
import org.cdsframework.group.Delete;

/**
 *
 * @author sdn
 */
@Stateless
public class NotificationStateBO extends BaseBO<NotificationStateDTO> {

    @EJB
    private SecuritySchemeBO securitySchemeBO;

    @EJB
    private UserBO userBO;

    @EJB
    private UserSecurityMapBO userSecurityMapBO;

    /**
     * Add NotificationStateDTOs for a SecuritySchemeDTO.
     *
     * @param securitySchemeDTO
     * @param recipientId
     * @param sessionDTO
     * @param propertyBagDTO
     * @throws ValidationException
     * @throws NotFoundException
     * @throws ConstraintViolationException
     * @throws MtsException
     * @throws AuthenticationException
     * @throws AuthorizationException
     */
    public void addNotificationStateDTO(SecuritySchemeDTO securitySchemeDTO, String recipientId, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ValidationException, NotFoundException, ConstraintViolationException, MtsException, AuthenticationException, AuthorizationException {
        List<SecuritySchemeDTO> securitySchemeDTOs = new ArrayList<>();
        getSecuritySchemeDTOs(securitySchemeDTO, securitySchemeDTOs, sessionDTO, propertyBagDTO);

        // iterate over the security schemes and find the user mappings and add NotificationStateDTO for each
        for (SecuritySchemeDTO item : securitySchemeDTOs) {
            UserSecurityMapDTO userSecurityMapDTO = new UserSecurityMapDTO();
            userSecurityMapDTO.setSecuritySchemeDTO(item);
            List<UserSecurityMapDTO> userSecurityMapDTOs = userSecurityMapBO.findByQueryListMain(userSecurityMapDTO, UserSecurityMapDTO.BySchemeId.class, new ArrayList<Class>(), sessionDTO, propertyBagDTO);
            for (UserSecurityMapDTO mapItem : userSecurityMapDTOs) {
                UserDTO userDTO = new UserDTO();
                userDTO.setUserId(mapItem.getUserId());
                userDTO = userBO.findByPrimaryKeyMain(userDTO, new ArrayList<Class>(), sessionDTO, propertyBagDTO);
                addNotificationStateDTO(userDTO, recipientId, sessionDTO, propertyBagDTO);
            }
        }

    }

    /**
     * Add a NotificationStateDTO for a UserDTO.
     *
     * @param userDTO
     * @param recipientId
     * @param sessionDTO
     * @param propertyBagDTO
     * @throws ValidationException
     * @throws NotFoundException
     * @throws ConstraintViolationException
     * @throws MtsException
     * @throws AuthenticationException
     * @throws AuthorizationException
     */
    public void addNotificationStateDTO(UserDTO userDTO, String recipientId, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ValidationException, NotFoundException, ConstraintViolationException, MtsException, AuthenticationException, AuthorizationException {
        NotificationStateDTO notificationStateDTO = new NotificationStateDTO();
        notificationStateDTO.setRecipientId(recipientId);
        notificationStateDTO.setState(NotificationState.INACTIVATED);
        notificationStateDTO.setUserDTO(userDTO);
        addMain(notificationStateDTO, Add.class, sessionDTO, propertyBagDTO);
    }

    /**
     * Delete a NotificationStateDTO for a UserDTO.
     *
     * @param userDTO
     * @param recipientId
     * @param sessionDTO
     * @param propertyBagDTO
     * @throws ValidationException
     * @throws NotFoundException
     * @throws ConstraintViolationException
     * @throws MtsException
     * @throws AuthenticationException
     * @throws AuthorizationException
     */
    public void deleteNotificationStateDTO(UserDTO userDTO, String recipientId, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ValidationException, NotFoundException, ConstraintViolationException, MtsException, AuthenticationException, AuthorizationException {
        NotificationStateDTO notificationStateDTO = new NotificationStateDTO();
        notificationStateDTO.setRecipientId(recipientId);
        notificationStateDTO.setUserDTO(userDTO);
        try {
            notificationStateDTO = findByQueryMain(notificationStateDTO, NotificationStateDTO.ByRecipientIdUserId.class, new ArrayList<Class>(), sessionDTO, propertyBagDTO);
            if (notificationStateDTO != null) {
                notificationStateDTO.delete();
            }
            deleteMain(notificationStateDTO, Delete.class, sessionDTO, propertyBagDTO);
        } catch (NotFoundException e) {
            // nada
        }
    }

    /**
     * Delete a NotificationStateDTO for a recipientId.
     *
     * @param recipientId
     * @param sessionDTO
     * @param propertyBagDTO
     * @throws ValidationException
     * @throws NotFoundException
     * @throws ConstraintViolationException
     * @throws MtsException
     * @throws AuthenticationException
     * @throws AuthorizationException
     */
    public void deleteNotificationStateDTO(String recipientId, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ValidationException, NotFoundException, ConstraintViolationException, MtsException, AuthenticationException, AuthorizationException {
        NotificationStateDTO notificationStateDTO = new NotificationStateDTO();
        notificationStateDTO.setRecipientId(recipientId);
        List<NotificationStateDTO> notificationStateDTOs = findByQueryListMain(notificationStateDTO, NotificationStateDTO.ByRecipientId.class, new ArrayList<Class>(), sessionDTO, propertyBagDTO);
        for (NotificationStateDTO item : notificationStateDTOs) {
            item.delete();
            deleteMain(item, Delete.class, sessionDTO, propertyBagDTO);
        }
    }

    /**
     * Recursively get security schemes.
     *
     * @param securitySchemeDTO
     * @param securitySchemeDTOs
     * @param sessionDTO
     * @param propertyBagDTO
     * @throws MtsException
     * @throws ValidationException
     * @throws NotFoundException
     * @throws AuthenticationException
     * @throws AuthorizationException
     */
    private void getSecuritySchemeDTOs(SecuritySchemeDTO securitySchemeDTO, List<SecuritySchemeDTO> securitySchemeDTOs, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws MtsException, ValidationException, NotFoundException, AuthenticationException, AuthorizationException {
        if (!securitySchemeDTOs.contains(securitySchemeDTO)) {
            // add incoming security scheme to list
            securitySchemeDTOs.add(securitySchemeDTO);

            // see if there are any related security schemes
            List<Class> childClasses = new ArrayList<>();
            childClasses.add(SecuritySchemeRelMapDTO.class);
            SecuritySchemeDTO queryDTO = securitySchemeBO.findByPrimaryKeyMain(securitySchemeDTO, childClasses, sessionDTO, propertyBagDTO);
            for (SecuritySchemeRelMapDTO securitySchemeRelMapDTO : queryDTO.getSecuritySchemeRelMapDTOs()) {
                getSecuritySchemeDTOs(securitySchemeRelMapDTO.getRelatedSecuritySchemeDTO(), securitySchemeDTOs, sessionDTO, propertyBagDTO);
            }
        }
    }

}
