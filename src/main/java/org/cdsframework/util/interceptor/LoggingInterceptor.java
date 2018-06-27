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
package org.cdsframework.util.interceptor;

import org.cdsframework.util.LogUtils;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 *
 * @author HLN Consulting, LLC
 */
@LogInterceptor
@Interceptor
public class LoggingInterceptor {

    private static LogUtils logger = LogUtils.getLogger(LoggingInterceptor.class);

    /**
     *
     * @param invocationContext
     * @return
     * @throws Exception
     */
    @AroundInvoke
    public Object log(InvocationContext invocationContext) throws Exception {
        final String METHODNAME = "log ";
        String ejbMethod = invocationContext.getTarget().getClass().getName() + "." + invocationContext.getMethod().getName();
        logger.info("*** TracingInterceptor intercepting " + ejbMethod);
        long start = System.currentTimeMillis();
        Object[] object = invocationContext.getParameters();
        if (object != null) {
            if (object.getClass().isArray()) {
                if (object.length > 0) {
                    Object param = invocationContext.getParameters()[0];
                    logger.info("*** TracingInterceptor intercepting " + ejbMethod + " param.getClass().getName(): " + param.getClass().getName());
                }
            }
        }

        // Parameters can be supplied if null
        // We can also validate parameters and supply alternate values
        //if (param == null)
        //    invocationContext.setParameters(new String[]{"default"});

        try {
            return invocationContext.proceed();
        } catch (Exception e) {
            logger.error(METHODNAME + "An Exception has occurred, Message: " + e.getMessage(), e);
            throw e;
        } finally {
            long time = System.currentTimeMillis() - start;
            logger.info("*** TracingInterceptor intercepting " + ejbMethod + " invocation of took " + time + "ms");
        }
    }
}
