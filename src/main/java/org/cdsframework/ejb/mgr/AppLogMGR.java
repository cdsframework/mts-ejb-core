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
package org.cdsframework.ejb.mgr;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import org.cdsframework.base.BaseDTO;
import org.cdsframework.base.BaseMGR;
import org.cdsframework.dto.AppLogDTO;
import org.cdsframework.dto.PropertyBagDTO;
import org.cdsframework.dto.SessionDTO;
import org.cdsframework.ejb.local.AppLogMGRLocal;
import org.cdsframework.ejb.remote.AppLogMGRRemote;
import org.cdsframework.enumeration.LogLevel;

/**
 *
 * @author HLN Consulting LLC
 */
@Stateless
@Remote
public class AppLogMGR extends BaseMGR<AppLogDTO> implements AppLogMGRRemote {
    @EJB 
    private AppLogMGRLocal appLogMGRLocal;
    
    @Override
    public void queueAppLog(LogLevel logLevel, String message, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
        appLogMGRLocal.queueAppLog(logLevel, message, sessionDTO, propertyBagDTO);
    }

    @Override
    public void queueAppLog(LogLevel logLevel, BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
        appLogMGRLocal.queueAppLog(logLevel, baseDTO, sessionDTO, propertyBagDTO);
    }

    @Override
    public void queueAppLog(LogLevel logLevel, Exception exception, BaseDTO baseDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
        appLogMGRLocal.queueAppLog(logLevel, exception, baseDTO, sessionDTO, propertyBagDTO);
    }

    @Override
    public void queueAppLog(LogLevel logLevel, Exception exception, String objectName, byte[] objectBytes, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
        appLogMGRLocal.queueAppLog(logLevel, exception, objectName, objectBytes, sessionDTO, propertyBagDTO);
    }

    @Override
    public void queueAppLog(LogLevel logLevel, String message, String stackTrace, String objectName, byte[] objectBytes, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
        appLogMGRLocal.queueAppLog(logLevel, message, stackTrace, objectName, objectBytes, sessionDTO, propertyBagDTO);
    }

    @Override
    public void queueAppInfoLog(String message, SessionDTO sessionDTO) {
        queueAppLog(LogLevel.INFO, message, sessionDTO, null);
    }

    @Override
    public void queueAppErrorLog(Exception exception, SessionDTO sessionDTO) {
        queueAppLog(LogLevel.ERROR, exception, null, sessionDTO, null);
    }

    @Override
    public void queueAppLog(AppLogDTO appLogDTO, SessionDTO sessionDTO, PropertyBagDTO propertyBagDTO) {
        appLogMGRLocal.queueAppLog(appLogDTO, sessionDTO, propertyBagDTO);
    }
    
}
