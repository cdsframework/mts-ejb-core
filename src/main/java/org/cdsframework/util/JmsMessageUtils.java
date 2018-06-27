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
package org.cdsframework.util;


import java.io.Serializable;
import java.util.Enumeration;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.cdsframework.util.LogUtils;

public class JmsMessageUtils {

    private static final LogUtils logger = LogUtils.getLogger(JmsMessageUtils.class);

    private InitialContext ctx;
    private ConnectionFactory queueFactory;
    private Connection connection;
    private Session session;
    private javax.jms.Queue queue;
    private Destination destination;
    private MessageProducer producer;
    private MessageConsumer consumer;
    private QueueBrowser browser;

    public JmsMessageUtils(String queueName, String queueFactoryName) throws NamingException, JMSException {
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing context for: " + queueFactoryName + "/" + queueName);
        }
        ctx = new InitialContext();
        queueFactory = (ConnectionFactory) ctx.lookup(queueFactoryName);
        queue = (javax.jms.Queue) ctx.lookup(queueName);
        connection = queueFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public ObjectMessage getNewObjectMessage() throws JMSException {
        return session.createObjectMessage();
    }
    public ObjectMessage getNewObjectMessage(Serializable serializable) throws JMSException {
        return session.createObjectMessage(serializable);
    }    
    public void sendMessage(ObjectMessage message) throws JMSException {
        if (logger.isDebugEnabled())
            logMessage(message);
        destination = (Destination) queue;
        producer = session.createProducer(destination);
        producer.send(message);
        producer.close();
    }

    public ObjectMessage getNextMessageFromQueue() throws JMSException {
        destination = (Destination) queue;
        consumer  = session.createConsumer(destination);
        ObjectMessage message = (ObjectMessage) consumer.receiveNoWait();
        consumer.close();
        if (logger.isDebugEnabled())
            logMessage(message);
        return message;
    }
    public Enumeration getQueueMessages() throws JMSException {
        browser = session.createBrowser(queue);
        Enumeration messages = browser.getEnumeration();
        return messages;
    }

    public ObjectMessage getMessageByJmsType(String jmsType) throws JMSException {
        ObjectMessage message = null;
        Enumeration messages = getQueueMessages();

        if (!messages.hasMoreElements()) {
            if (logger.isDebugEnabled()) {
                logger.debug("No messages in queue");
            }
        } else {
            while (messages.hasMoreElements()) {
                ObjectMessage tempMsg = (ObjectMessage) messages.nextElement();
                if (logger.isDebugEnabled()) {
                    logger.debug("Message: JMSCorrelationID - " + tempMsg.getJMSCorrelationID() + "; JMSMessageID - " + tempMsg.getJMSMessageID() + "; JMSType - " + tempMsg.getJMSType());
                }
                if (tempMsg.getJMSType().equals(jmsType)) {
                    message = tempMsg;
                }
            }
        }
        browser.close();
        if (logger.isDebugEnabled())
            logMessage(message);
        return message;
    }

    public void close() {
        try {
            session.close();
            if (logger.isDebugEnabled()) {
                logger.debug("JMS session closing...");
            }
        } catch (JMSException e) {
            logger.error("JMS Exception Message on session close: " + e.getMessage(), e);
        }
        try {
            connection.close();
            if (logger.isDebugEnabled()) {
                logger.debug("JMS connection closing...");
            }
        } catch (JMSException e) {
            logger.error("JMS Exception Message on connection close: " + e.getMessage(), e);
        }
    }
    private void logMessage(ObjectMessage message) throws JMSException {
        if (message != null) {
            for (Enumeration propertyNames = message.getPropertyNames(); propertyNames.hasMoreElements();) {
                String propertyName = (String) propertyNames.nextElement();
                logger.debug("Object property: " + propertyName + " - " + message.getObjectProperty(propertyName));
            }
        } else {
            logger.debug("null message");
        }
    }
}
