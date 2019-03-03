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
package org.cdsframework.util;

import java.util.Properties;

/**
 *
 * @author HLN Consulting, LLC
 */
public class EjbCoreConfiguration {

    private static final LogUtils logger = LogUtils.getLogger(EjbCoreConfiguration.class);
    private static final Properties INSTANCE_PROPERTIES = new Properties();
    private static final boolean MTS_MASTER;    // Defaults to true
    private static final String CAT_BS_URI;

    static {
        final String METHODNAME = "EjbCoreConfiguration static contructor ";

        // Instance attributes
        String instanceName = "appServer.properties";
        String instancePropertyUriName = "ejbCorePropertiesUri";
        String instanceDirSystemName = "instanceDirSystemPropertyName";

        // Log attributes
        logger.info(METHODNAME, "instanceName=", instanceName, " instancePropertyUriName=", instancePropertyUriName, " instanceDirSystemName=", instanceDirSystemName);

        // Get the properties, failOnNotFound = false (optional)
        ConfigurationProperties.getProperties(instanceName, instancePropertyUriName, instanceDirSystemName, INSTANCE_PROPERTIES, false);
        if (INSTANCE_PROPERTIES != null) {
            //
            // Make sure only 1 domain is the master, 
            // Used by timer jobs that can SHOULD NOT run in all domains
            // Make sure to set all other NON master domains to false
            //
            String catBsUri = INSTANCE_PROPERTIES.getProperty("CAT_BS_URI");
            if (StringUtils.isEmpty(catBsUri)) {
                logger.warn(METHODNAME, "CAT_BS_URI is null - password token emails will not be generated correctly");
                CAT_BS_URI = "NOT_CONFIGURED - property missing from ejb-core.properties";
            } else {
                CAT_BS_URI = catBsUri;
            }
            MTS_MASTER = Boolean.parseBoolean(INSTANCE_PROPERTIES.getProperty("MTS_MASTER", "true"));
        } else {
            // Default to True
            MTS_MASTER = true;
            CAT_BS_URI = "NOT_CONFIGURED - ejb-core.properties missing";
        }
        logger.info(METHODNAME, "MTS_MASTER=", MTS_MASTER);
        logger.info(METHODNAME, "CAT_BS_URI=", CAT_BS_URI);
    }

    public static Properties getInstanceProperties() {
        return INSTANCE_PROPERTIES;
    }

    public static boolean isMtsMaster() {
        return MTS_MASTER;
    }

    public static String getcatBsUri() {
        return CAT_BS_URI;
    }

}
