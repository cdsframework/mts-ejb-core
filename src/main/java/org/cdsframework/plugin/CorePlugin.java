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
package org.cdsframework.plugin;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.cdsframework.base.BasePlugin;
import org.cdsframework.dto.AppDTO;
import org.cdsframework.dto.AuditLogDTO;
import org.cdsframework.dto.AuditTransactionDTO;
import org.cdsframework.dto.SessionDTO;
import org.cdsframework.dto.UserDTO;
import org.cdsframework.dto.UserPreferenceDTO;
import org.cdsframework.ejb.local.SecurityMGRInternal;
import org.cdsframework.exceptions.ConstraintViolationException;
import org.cdsframework.exceptions.MtsException;
import org.cdsframework.exceptions.NotFoundException;
import org.cdsframework.util.Constants;

/**
 *
 * @author HLN Consulting, LLC
 */
@Startup
@Singleton
public class CorePlugin extends BasePlugin {

    @Resource(name = "ORG.CDSFRAMEWORK.APP_SERVER")
    private String APP_SERVER;
    
    @EJB
    private SecurityMGRInternal securityMGRInternal;

    @Override
    protected void initializeDb() throws NotFoundException, MtsException, ConstraintViolationException {

        // for CAT proxy logins
        securityMGRInternal.registerApplicationProxy(UserDTO.FindCatProxyUser.class);
    }

    @Override
    protected void preProcess() throws MtsException {
        Constants.APP_SERVER = APP_SERVER;
    }
    
    @Override
    protected void postProcess() throws MtsException {

        // pre-init these tables
        initializeDbTables(AppDTO.class);
        initializeDbTables(AuditTransactionDTO.class);
        initializeDbTables(AuditLogDTO.class);
        initializeDbTables(UserDTO.class);
        initializeDbTables(SessionDTO.class);

        // default user pref init - menu position
        UserPreferenceDTO userPreferenceDTO = new UserPreferenceDTO();
        userPreferenceDTO.setName("catMenuPosition");
        userPreferenceDTO.setValue("top");
        userPreferenceDTO.setType("java.lang.String");
        userPreferenceDTO.setSessionPersistent(true);
        userPreferenceDTO.setSessionPreference(false);
        userPreferenceDTO.setDefaultValue("top");
        userPreferenceDTO.setUserEditable(true);
        registerUserPreference(userPreferenceDTO);

        // default user pref init - show log
        userPreferenceDTO = new UserPreferenceDTO();
        userPreferenceDTO.setName("catShowLog");
        userPreferenceDTO.setValue("false");
        userPreferenceDTO.setType("java.lang.String");
        userPreferenceDTO.setSessionPersistent(true);
        userPreferenceDTO.setSessionPreference(false);
        userPreferenceDTO.setDefaultValue("false");
        userPreferenceDTO.setUserEditable(true);
        registerUserPreference(userPreferenceDTO);

        // default user pref init - theme
        userPreferenceDTO = new UserPreferenceDTO();
        userPreferenceDTO.setName("catTheme");
        userPreferenceDTO.setValue("redmond");
        userPreferenceDTO.setType("java.lang.String");
        userPreferenceDTO.setSessionPersistent(true);
        userPreferenceDTO.setSessionPreference(false);
        userPreferenceDTO.setDefaultValue("redmond");
        userPreferenceDTO.setUserEditable(true);
        registerUserPreference(userPreferenceDTO);
    }
}