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
package org.cdsframework.ejb.bo;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import org.cdsframework.base.BaseBO;
import org.cdsframework.base.BaseDTO;
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.dto.SessionDTO;
import org.cdsframework.dto.UserDTO;
import org.cdsframework.dto.UserPreferenceDTO;
import org.cdsframework.ejb.local.PropertyMGRLocal;
import org.cdsframework.ejb.local.SecurityMGRInternal;
import org.cdsframework.ejb.local.SmtpMGRLocal;
import org.cdsframework.ejb.local.UserSecurityMGRLocal;
import org.cdsframework.enumeration.CoreErrorCode;
import org.cdsframework.enumeration.DTOState;
import org.cdsframework.enumeration.ExceptionReason;
import org.cdsframework.enumeration.Operation;
import org.cdsframework.enumeration.PasswordResetContext;
import org.cdsframework.exceptions.AuthenticationException;
import org.cdsframework.exceptions.AuthorizationException;
import org.cdsframework.exceptions.MtsException;
import org.cdsframework.exceptions.ConstraintViolationException;
import org.cdsframework.exceptions.NotFoundException;
import org.cdsframework.exceptions.ValidationException;
import org.cdsframework.group.FindAll;
import org.cdsframework.group.Update;
import org.cdsframework.util.AuthenticationUtils;
import org.cdsframework.util.BrokenRule;
import org.cdsframework.util.DTOUtils;
import org.cdsframework.util.EjbCoreConfiguration;
import org.cdsframework.util.ObjectUtils;
import org.cdsframework.util.PasswordHash;
import org.cdsframework.util.StringUtils;
import org.cdsframework.util.support.DeepCopy;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

@Stateless
public class UserBO extends BaseBO<UserDTO> {

    @EJB
    private SecurityMGRInternal securityMGRInternal;
    @EJB
    private UserSecurityMGRLocal userSecurityMGRLocal;
    @EJB
    private PropertyMGRLocal propertyMGRLocal;
    @EJB
    private SmtpMGRLocal smtpMGRLocal;

    private final Map<String, UserPreferenceDTO> defaultPreferenceMap = new HashMap<>();

    @Override
    protected void preAdd(UserDTO baseDTO, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ConstraintViolationException, NotFoundException, MtsException, ValidationException,
            AuthenticationException, AuthorizationException {
        Boolean isPreferenceUpdate = propertyBagDTO.get("isPreferenceUpdate", false);
        if (baseDTO.isSendInitialEmail()) {
            setPasswordToken(baseDTO);
        } else {
            if (!isPreferenceUpdate) {
                UserDTO userDTO = baseDTO;
                if (userDTO.getPassword() == null
                        || userDTO.getPassword().isEmpty()
                        || userDTO.getPasswordConfirm() == null
                        || userDTO.getPasswordConfirm().isEmpty()
                        || !userDTO.getPassword().equals(userDTO.getPasswordConfirm())) {
                    throw new ValidationException(new BrokenRule(CoreErrorCode.PasswordMismatch, "Mismatched password"));
                }
                if (!validatePasswordCriteria(userDTO.getPassword())) {
                    throw new ValidationException(new BrokenRule(CoreErrorCode.PasswordCriteriaViolation, getPasswordCriteriaViolationMessage()));
                }
                try {
                    userDTO.setPasswordHash(PasswordHash.createHash(userDTO.getPassword()));
                } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                    throw new MtsException(e.getMessage(), e);
                }
                setPasswordExpiration(userDTO);
            }
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    protected void preUpdate(UserDTO baseDTO, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ConstraintViolationException, NotFoundException, MtsException, ValidationException,
            AuthenticationException, AuthorizationException {
        UserDTO userDTO = baseDTO;

        if (userDTO.getPassword() != null
                && !userDTO.getPassword().isEmpty()) {
            if (!validatePasswordCriteria(userDTO.getPassword())) {
                throw new ValidationException(new BrokenRule(CoreErrorCode.PasswordCriteriaViolation, getPasswordCriteriaViolationMessage()));
            }
            if (userDTO.getPassword().equals(userDTO.getPasswordConfirm())) {
                try {
                    userDTO.setPasswordHash(PasswordHash.createHash(userDTO.getPassword()));
                    userDTO.setFailedLoginAttempts(0);
                    setPasswordExpiration(userDTO);
                    logger.debug("UserBO preUpdate new password DML update for user: ", userDTO.getUsername());
                    getDao().update(baseDTO, UserDTO.UpdatePasswordHash.class, sessionDTO, propertyBagDTO);
                    userDTO.setSendInitialEmail(false);
                    userDTO.setTokenExpiration(null);
                    userDTO.setPasswordToken(null);
                } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                    throw new MtsException(e.getMessage(), e);
                }
            } else {
                throw new ValidationException(new BrokenRule(CoreErrorCode.PasswordMismatch, "Mismatched password"));
            }
        }
    }

    @Override
    protected UserDTO customSave(UserDTO parentDTO, Class queryClass, List<Class> childClassDTOs, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ValidationException, NotFoundException, MtsException, AuthenticationException, AuthorizationException, ConstraintViolationException {
        final String METHODNAME = "customSave ";
        if (queryClass == UserDTO.UpdatePasswordHash.class) {
            logger.debug(METHODNAME, "parentDTO=", parentDTO);
            logger.debug(METHODNAME, "queryClass=", queryClass);
            // logger.warn(METHODNAME, "propertyBagDTO.getPropertyMap()=", propertyBagDTO.getPropertyMap());
            if (parentDTO != null) {
                UserDTO userDTO = findByPrimaryKeyMain(parentDTO, new ArrayList<>(), AuthenticationUtils.getInternalSessionDTO(), propertyBagDTO);
                logger.debug(METHODNAME, "userDTO.getUserId()=", userDTO.getUserId());
                logger.debug(METHODNAME, "userDTO.getUsername()=", userDTO.getUsername());
                String userId = userDTO.getUserId();
                String passwordToken = ObjectUtils.objectToString(propertyBagDTO.get("passwordToken"));
                String oldPassword = ObjectUtils.objectToString(propertyBagDTO.get("oldPassword"));
                String newPassword = ObjectUtils.objectToString(propertyBagDTO.get("newPassword"));
                String confirmPassword = ObjectUtils.objectToString(propertyBagDTO.get("confirmPassword"));
                if (StringUtils.isEmpty(userId)) {
                    throw new NotFoundException("userId is empty!");
                }
                if (!confirmPassword.equals(newPassword)) {
                    throw new NotFoundException("confirmPassword and newPassword do not match!");
                }
                boolean oldPasswordOrTokenMatches = securityMGRInternal.authenticate(userDTO, oldPassword, passwordToken);
                logger.debug(METHODNAME, "oldPasswordOrTokenMatches=", oldPasswordOrTokenMatches);
                if (oldPasswordOrTokenMatches) {
                    userDTO.setPassword(newPassword);
                    userDTO.setPasswordConfirm(confirmPassword);
                    userDTO.setChangePassword(false);
                    setPasswordExpiration(userDTO);
                    updateMain(userDTO, Update.class, AuthenticationUtils.getInternalSessionDTO(), propertyBagDTO);
                } else {
                    throw new AuthenticationException("Old password did not match!", ExceptionReason.BAD_CREDENTIALS);
                }
            } else {
                throw new NotFoundException("UserDTO was null!");
            }
            return null;
        } else if (queryClass == UserDTO.UpdatePasswordToken.class) {
            setPasswordToken(parentDTO);
            logger.info(METHODNAME, "parentDTO.getDTOState()=", parentDTO.getDTOState());
            updateMain(parentDTO, Update.class, AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
            generateTokenEmail(parentDTO, propertyBagDTO);
            logger.info(METHODNAME, "parentDTO.getDTOState()=", parentDTO.getDTOState());
            return null;
        } else {
            return super.customSave(parentDTO, queryClass, childClassDTOs, sessionDTO, propertyBagDTO);
        }
    }

    @Override
    protected void postAdd(UserDTO baseDTO, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) throws
            ConstraintViolationException, NotFoundException, MtsException, ValidationException, AuthenticationException,
            AuthorizationException {

        final String METHODNAME = "postAdd ";
        Boolean isPreferenceUpdate = propertyBagDTO.get("isPreferenceUpdate", false);
        logger.debug(METHODNAME, "isPreferenceUpdate: ", isPreferenceUpdate);
        if (!isPreferenceUpdate) {
            logger.debug(METHODNAME, "updating user prefs for user: ", baseDTO);
            DTOUtils.setDTOState(baseDTO, DTOState.UPDATED);
            setDefaultPrefs(baseDTO);
        }
        userSecurityMGRLocal.refreshUserSecuritySchemePermissionMap(baseDTO);
        if (baseDTO.isSendInitialEmail()) {
            propertyBagDTO.put("context", PasswordResetContext.NEW_ACCOUNT);
            generateTokenEmail(baseDTO, propertyBagDTO);
        }
    }

    @Override
    protected void postUpdate(UserDTO baseDTO, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ConstraintViolationException, NotFoundException, MtsException, ValidationException, AuthenticationException,
            AuthorizationException {

        final String METHODNAME = "postUpdate ";
        logger.debug(METHODNAME, "refreshing user security prefs for user: ", baseDTO);
        userSecurityMGRLocal.refreshUserSecuritySchemePermissionMap(baseDTO);
    }

    @Override
    protected void postDelete(UserDTO baseDTO, Class queryClass, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws ConstraintViolationException, NotFoundException, MtsException, ValidationException, AuthenticationException,
            AuthorizationException {

        final String METHODNAME = "postDelete ";
        logger.debug(METHODNAME, "deleting user security prefs for user: ", baseDTO);
        userSecurityMGRLocal.deleteUserSecuritySchemePermissionMap(baseDTO);
    }

    @Override
    protected void postProcessBaseDTOs(BaseDTO parentDTO, List<UserDTO> baseDTOs, Operation operation, Class queryClass, List<Class> validationClasses, List<Class> childClassDTOs, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO)
            throws MtsException, ValidationException, NotFoundException, AuthenticationException, AuthorizationException {
        final String METHODNAME = "postProcessBaseDTOs ";
//        logger.warn(METHODNAME, "parentDTO=", parentDTO);
//        logger.warn(METHODNAME, "baseDTOs=", baseDTOs);

        if (!AuthenticationUtils.isInternalSession(sessionDTO)) {
            logger.debug(METHODNAME, "blanking out password fields");
            for (UserDTO userDTO : baseDTOs) {
                userDTO.setPassword(null);
                userDTO.setPasswordConfirm(null);
                userDTO.setPasswordHash(null);
                userDTO.setPasswordToken(null);
                userDTO.setTokenExpiration(null);
            }
        }
    }

    private String getPasswordCriteriaViolationMessage() {

        Integer passwordVettingLength = propertyMGRLocal.get("PASSWORD_VETTING_LENGTH", Integer.class);
        if (passwordVettingLength == null || passwordVettingLength == 0) {
            passwordVettingLength = 8;
        }

        Boolean passwordVettingUppercaseReqd = propertyMGRLocal.get("PASSWORD_VETTING_UPPERCASE_REQD", Boolean.class);
        if (passwordVettingUppercaseReqd == null) {
            passwordVettingUppercaseReqd = false;
        }

        Boolean passwordVettingLowercaseReqd = propertyMGRLocal.get("PASSWORD_VETTING_LOWERCASE_REQD", Boolean.class);
        if (passwordVettingLowercaseReqd == null) {
            passwordVettingLowercaseReqd = false;
        }

        Boolean passwordVettingNumberReqd = propertyMGRLocal.get("PASSWORD_VETTING_NUMBER_REQD", Boolean.class);
        if (passwordVettingNumberReqd == null) {
            passwordVettingNumberReqd = false;
        }

        Boolean passwordVettingNonAlphaReqd = propertyMGRLocal.get("PASSWORD_VETTING_NON_ALPHA_REQD", Boolean.class);
        if (passwordVettingNonAlphaReqd == null) {
            passwordVettingNonAlphaReqd = false;
        }

        StringBuilder stringBuilder = new StringBuilder("Passwords must be at least ").append(passwordVettingLength.toString()).append(" characters in length");
        if (passwordVettingUppercaseReqd) {
            if (!passwordVettingLowercaseReqd && !passwordVettingNumberReqd && !passwordVettingNonAlphaReqd) {
                stringBuilder.append(" and ");
            } else {
                stringBuilder.append(", ");
            }
            stringBuilder.append("contain at least one uppercase letter");
        }
        if (passwordVettingLowercaseReqd) {
            if (!passwordVettingNonAlphaReqd && !passwordVettingNumberReqd) {
                stringBuilder.append(" and ");
            } else {
                stringBuilder.append(", ");
            }
            stringBuilder.append("contain at least one lowercase letter");
        }
        if (passwordVettingNumberReqd) {
            if (!passwordVettingNonAlphaReqd) {
                stringBuilder.append(" and ");
            } else {
                stringBuilder.append(", ");
            }
            stringBuilder.append("contain at least one number");
        }
        if (passwordVettingNonAlphaReqd) {
            stringBuilder.append(" and contain at least one non-alphanumeric character");
        }

        return stringBuilder.toString();
    }

    /**
     * Checks a password for CIR password rules - should generalize this so the
     * criteria checker is plug-able
     *
     * @param password
     * @return
     */
    private boolean validatePasswordCriteria(String password) {
        final String METHODNAME = "validatePasswordCriteria ";
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasNumber = false;
        boolean hasNonAlpha = false;

        Integer passwordVettingLength = propertyMGRLocal.get("PASSWORD_VETTING_LENGTH", Integer.class);
        if (passwordVettingLength == null || passwordVettingLength == 0) {
            passwordVettingLength = 8;
        }
        logger.debug(METHODNAME, "passwordVettingLength=", passwordVettingLength);

        Boolean passwordVettingUppercaseReqd = propertyMGRLocal.get("PASSWORD_VETTING_UPPERCASE_REQD", Boolean.class);
        if (passwordVettingUppercaseReqd == null) {
            passwordVettingUppercaseReqd = false;
        }
        logger.debug(METHODNAME, "passwordVettingUppercaseReqd=", passwordVettingUppercaseReqd);

        Boolean passwordVettingLowercaseReqd = propertyMGRLocal.get("PASSWORD_VETTING_LOWERCASE_REQD", Boolean.class);
        if (passwordVettingLowercaseReqd == null) {
            passwordVettingLowercaseReqd = false;
        }
        logger.debug(METHODNAME, "passwordVettingLowercaseReqd=", passwordVettingLowercaseReqd);

        Boolean passwordVettingNumberReqd = propertyMGRLocal.get("PASSWORD_VETTING_NUMBER_REQD", Boolean.class);
        if (passwordVettingNumberReqd == null) {
            passwordVettingNumberReqd = false;
        }
        logger.debug(METHODNAME, "passwordVettingNumberReqd=", passwordVettingNumberReqd);

        Boolean passwordVettingNonAlphaReqd = propertyMGRLocal.get("PASSWORD_VETTING_NON_ALPHA_REQD", Boolean.class);
        if (passwordVettingNonAlphaReqd == null) {
            passwordVettingNonAlphaReqd = false;
        }
        logger.debug(METHODNAME, "passwordVettingNonAlphaReqd=", passwordVettingNonAlphaReqd);

        if (password.length() >= passwordVettingLength) {
            for (Character c : password.toCharArray()) {
                if (Character.isAlphabetic(c)) {
                    if (Character.isUpperCase(c)) {
                        logger.debug(METHODNAME, "found upper!");
                        hasUpper = true;
                    } else {
                        logger.debug(METHODNAME, "found lower!");
                        hasLower = true;
                    }
                } else if (Character.isDigit(c)) {
                    logger.debug(METHODNAME, "found number!");
                    hasNumber = true;
                } else {
                    logger.debug(METHODNAME, "found non-alpha!");
                    hasNonAlpha = true;
                }
            }
            if (!passwordVettingUppercaseReqd) {
                logger.debug(METHODNAME, "not counting uppers");
                hasUpper = true;
            }
            if (!passwordVettingLowercaseReqd) {
                logger.debug(METHODNAME, "not counting lowers");
                hasLower = true;
            }
            if (!passwordVettingNumberReqd) {
                logger.debug(METHODNAME, "not counting numbers");
                hasNumber = true;
            }
            if (!passwordVettingNonAlphaReqd) {
                logger.debug(METHODNAME, "not counting non-alphas");
                hasNonAlpha = true;
            }

            logger.debug(METHODNAME, "hasUpper=", hasUpper);
            logger.debug(METHODNAME, "hasLower=", hasLower);
            logger.debug(METHODNAME, "hasNumber=", hasNumber);
            logger.debug(METHODNAME, "hasNonAlpha=", hasNonAlpha);

            return hasUpper && hasLower && hasNumber && hasNonAlpha;
        }
        return false;
    }

    private void setPasswordExpiration(UserDTO userDTO) {
        final String METHODNAME = "setPasswordExpiration ";

        if (!userDTO.isAppProxyUser()) {
            Integer expireDays = (Integer) propertyMGRLocal.get("DEFAULT_PASSWORD_EXPIRATION");
            logger.info(METHODNAME, "expireDays=", expireDays);
            if (expireDays == null) {
                expireDays = 90;
            }
            if (expireDays != 0) {
                userDTO.setExpirationDate(LocalDate.now().plusDays(expireDays).toDate());
            } else {
                logger.info(METHODNAME, "not setting an expiration");
            }
        }
    }

    /**
     * Goes through the list of default user preferences and initializes them
     * for a supplied user.
     *
     * @param userDTO
     * @throws MtsException
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    private void setDefaultPrefs(UserDTO userDTO) throws MtsException {
        final String METHODNAME = "setDefaultPrefs ";
        if (userDTO == null) {
            throw new MtsException(METHODNAME + "userDTO was null!");
        }
        for (UserPreferenceDTO userPreferenceDTO : defaultPreferenceMap.values()) {
            logger.debug(METHODNAME, "userPreferenceDTO: ", userPreferenceDTO.getName());
            setDefaultPref(userDTO, userPreferenceDTO);
        }
    }

    /**
     * Sets the default user preference value for a non-proxied/non-internal
     * user if the preference doesn't exist.
     *
     * @param userDTO
     * @param userPreferenceDTO
     * @throws MtsException
     */
    private void setDefaultPref(UserDTO userDTO, UserPreferenceDTO userPreferenceDTO) throws MtsException {
        final String METHODNAME = "setDefaultPref ";
        if (userDTO != null) {
            // don't run on internal or proxy users...
            if (userDTO.isAppProxyUser() || userDTO.isProxiedUser() || userDTO.equals(AuthenticationUtils.getInternalUserDTO())) {
                logger.debug(METHODNAME, "skipping proxied or internal user: ", userDTO.getUsername());
                return;
            }
            if (userPreferenceDTO != null) {
                logger.debug(METHODNAME, "checking for user pref ", userPreferenceDTO.getName(), " for user ", userDTO.getUsername());
                if (!userDTO.getUserPreferenceDTOMap().containsKey(userPreferenceDTO.getName())) {
                    logger.debug(METHODNAME, "creating default pref ", userPreferenceDTO.getName(), " for user ", userDTO.getUsername());
                    userDTO.addOrUpdateChildDTO(DeepCopy.copy(userPreferenceDTO));
                    PropertyBagDTO propertyBagDTO = new PropertyBagDTO();
                    propertyBagDTO.put("isPreferenceUpdate", true);
                    try {
                        this.updateMain(userDTO, Update.class, AuthenticationUtils.getInternalSessionDTO(), propertyBagDTO);
                    } catch (ValidationException e) {
                        logger.error(METHODNAME, "ValidationException caught on user pref add for ", userPreferenceDTO.getName(), ":  ", e.getMessage());
                        logger.error(e);
                    } catch (NotFoundException e) {
                        logger.error(METHODNAME, "NotFoundException caught on user pref add for ", userPreferenceDTO.getName(), ":  ", e.getMessage());
                        logger.error(e);
                    } catch (ConstraintViolationException e) {
                        logger.error(METHODNAME, "ConstraintViolationException caught on user pref add for ", userPreferenceDTO.getName(), ":  ", e.getMessage());
                        logger.error(e);
                    } catch (AuthenticationException e) {
                        logger.error(METHODNAME, "AuthenticationException caught on user pref add for ", userPreferenceDTO.getName(), ":  ", e.getMessage());
                        logger.error(e);
                    } catch (AuthorizationException e) {
                        logger.error(METHODNAME, "AuthorizationException caught on user pref add for ", userPreferenceDTO.getName(), ":  ", e.getMessage());
                        logger.error(e);
                    }
                } else {
                    logger.debug(METHODNAME, "user pref ", userPreferenceDTO.getName(), " already exists for user ", userDTO.getUsername());
                }
            } else {
                logger.error(METHODNAME, "userPreferenceDTO is null!");
            }
        } else {
            logger.error(METHODNAME, "userDTO is null!");
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void registerUserPreference(UserPreferenceDTO userPreferenceDTO) throws MtsException {
        final String METHODNAME = "registerUserPreference ";
        if (userPreferenceDTO != null) {
            if (!defaultPreferenceMap.containsKey(userPreferenceDTO.getName())) {
                try {
                    Class<?> prefType = Class.forName(userPreferenceDTO.getType());
                    Object cast = prefType.cast(userPreferenceDTO.getValue());
                    cast = prefType.cast(userPreferenceDTO.getDefaultValue());
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException(METHODNAME + "userPreferenceDTO class type not found! " + userPreferenceDTO.getType());
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException(METHODNAME + "userPreferenceDTO class type cast failed! " + userPreferenceDTO.getType() + " - " + userPreferenceDTO.getValue() + " - " + userPreferenceDTO.getDefaultValue());
                }
                logger.debug(METHODNAME, "Registering user preference: ", userPreferenceDTO.getName());
                defaultPreferenceMap.put(userPreferenceDTO.getName(), userPreferenceDTO);
                try {
                    List<UserDTO> userDTOs = findByQueryListMain(new UserDTO(), FindAll.class, new ArrayList<>(), AuthenticationUtils.getInternalSessionDTO(), new PropertyBagDTO());
                    for (UserDTO userDTO : userDTOs) {
                        setDefaultPref(userDTO, userPreferenceDTO);
                    }
                } catch (AuthenticationException | AuthorizationException | ValidationException | NotFoundException e) {
                    logger.error(e);
                }

            } else {
                throw new IllegalArgumentException(METHODNAME + "userPreferenceDTO already registered! " + userPreferenceDTO.getName());
            }
        } else {
            throw new IllegalArgumentException(METHODNAME + "userPreferenceDTO was null!");
        }
    }

    /**
     * Give a user ID or name - return the userDTO...
     *
     * @param idOrName
     * @param sessionDTO
     * @return
     * @throws ValidationException
     * @throws MtsException
     * @throws NotFoundException
     * @throws AuthenticationException
     * @throws AuthorizationException
     */
    public UserDTO getUserDTOByIdOrName(String idOrName, SessionDTO sessionDTO) throws ValidationException, MtsException,
            NotFoundException, AuthenticationException, AuthorizationException {
        final String METHODNAME = "getUserDTOByIdOrName ";
        UserDTO userDTO = null;
        if (idOrName != null) {
            userDTO = new UserDTO();
            userDTO.setUsername(idOrName);
            try {
                userDTO = findByQueryMain(userDTO, UserDTO.DtoByUsername.class, new ArrayList<>(), sessionDTO, new PropertyBagDTO());
            } catch (NotFoundException e) {
                userDTO = null;
            }
            if (userDTO == null) {
                userDTO = new UserDTO();
                userDTO.setUserId(idOrName);
                try {
                    userDTO = findByPrimaryKeyMain(userDTO, new ArrayList<>(), sessionDTO, new PropertyBagDTO());
                } catch (NotFoundException e) {
                    userDTO = null;
                    logger.debug(METHODNAME, "idOrName not found: ", idOrName);
                }
            }
        }
        return userDTO;
    }

    private void generateTokenEmail(UserDTO userDTO, PropertyBagDTO propertyBagDTO)
            throws MtsException, ValidationException, NotFoundException, ConstraintViolationException, AuthenticationException, AuthorizationException {
        final String METHODNAME = "generateTokenEmail ";
        logger.info(METHODNAME, "called!");
        logger.info(METHODNAME, "userDTO=", userDTO);

        String passwordToken = userDTO.getPasswordToken();
        String username = userDTO.getUsername();
        String email = userDTO.getEmail();
        String firstName = userDTO.getFirstName();
        logger.info(METHODNAME, "passwordToken=", passwordToken);
        logger.info(METHODNAME, "username=", username);
        logger.info(METHODNAME, "email=", email);
        logger.info(METHODNAME, "firstName=", firstName);

        PasswordResetContext context = propertyBagDTO.get("context", PasswordResetContext.class);
        logger.info(METHODNAME, "context=", context);
        String htmlEmailBody;
        String emailSubject;
        if (null == context) {
            throw new MtsException("context was unknown: " + context);
        } else {
            switch (context) {
                case FORGOT:
                    htmlEmailBody = propertyMGRLocal.get("FORGOT_PASSWORD_EMAIL", String.class);
                    emailSubject = propertyMGRLocal.get("FORGOT_PASSWORD_EMAIL_SUBJECT", String.class);
                    break;
                case NEW_ACCOUNT:
                    htmlEmailBody = propertyMGRLocal.get("NEW_ACCOUNT_EMAIL", String.class);
                    emailSubject = propertyMGRLocal.get("NEW_ACCOUNT_EMAIL_SUBJECT", String.class);
                    break;
                default:
                    throw new MtsException("context was unknown: " + context);
            }
        }

        Integer linkExpiration = propertyMGRLocal.get("PASSWORD_LINK_EXPIRATION_HOURS", Integer.class);
        String catLoginLink = EjbCoreConfiguration.getcatBsUri() + "/login.html";
        String catResetLink = EjbCoreConfiguration.getcatBsUri() + "/resetPassword.html?token=" + passwordToken;

        logger.info(METHODNAME, "catLoginLink=", catLoginLink);
        logger.info(METHODNAME, "catResetLink=", catResetLink);
        logger.info(METHODNAME, "linkExpiration=", linkExpiration);
        logger.info(METHODNAME, "emailSubject=", emailSubject);

        htmlEmailBody = htmlEmailBody.replace("[PASSWORD_LINK_EXPIRATION_HOURS]", linkExpiration.toString());
        htmlEmailBody = htmlEmailBody.replace("[EMAIL_CAT_BS_LOGIN_LINK]", catLoginLink);
        htmlEmailBody = htmlEmailBody.replace("[EMAIL_CAT_BS_PASSWORD_RESET_LINK]", catResetLink);
        htmlEmailBody = htmlEmailBody.replace("[USERNAME]", username);
        htmlEmailBody = htmlEmailBody.replace("[FIRST_NAME]", firstName);

        logger.info(METHODNAME, "emailBody=", htmlEmailBody);

        String plainRegEx = "<div[^>]*>|</div[^>]*>|<br[^>]*>|<a[^\"]*\"|\".*>|mailto:";

        String textEmailBody = htmlEmailBody.replaceAll(plainRegEx, "");

        logger.info(METHODNAME, "textEmailBody=", textEmailBody);

        smtpMGRLocal.sendHtmlMessage(null,
                email,
                emailSubject,
                textEmailBody,
                htmlEmailBody);
    }

    private void setPasswordToken(UserDTO userDTO) {
        final String METHODNAME = "setPasswordToken ";
        userDTO.setPasswordToken(StringUtils.getHashId(128));
        Integer linkExpiration = propertyMGRLocal.get("PASSWORD_LINK_EXPIRATION_HOURS", Integer.class);
        DateTime dateTime = new DateTime();
        dateTime = dateTime.plusHours(linkExpiration);
        userDTO.setTokenExpiration(dateTime.toDate());
        logger.info(METHODNAME, "userDTO.getPasswordToken()=", userDTO.getPasswordToken());
        logger.info(METHODNAME, "userDTO.getTokenExpiration()=", userDTO.getTokenExpiration());
    }

}
