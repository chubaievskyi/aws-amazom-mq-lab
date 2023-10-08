package com.chubaievskyi;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class Consumer implements Runnable {

    public static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);
    private static final Properties PROPERTIES = new PropertiesLoader().loadProperties();
    private static final InputReader INPUT_READER = new InputReader(PROPERTIES);
    private static final String QUEUE_NAME = INPUT_READER.getQueueName();

    private final ActiveMQConnectionFactory connectionFactory;
    private final AtomicInteger receiveMessageCounter;
    private final CSVWriter csvWriter;

    public Consumer(ActiveMQConnectionFactory connectionFactory, AtomicInteger receiveMessageCounter, CSVWriter csvWriter) {
        this.connectionFactory = connectionFactory;
        this.receiveMessageCounter = receiveMessageCounter;
        this.csvWriter = csvWriter;
    }

    @Override
    public void run() {
        try {
            receiveMessage();
        } catch (JMSException e) {
            LOGGER.debug("Error receiving a message.", e);
        }
    }

    private void receiveMessage() throws JMSException {

        Connection consumerConnection = connectionFactory.createConnection();
        consumerConnection.start();
        LOGGER.info("Connection with the consumer is established.");

        Session consumerSession = consumerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        LOGGER.info("Created a session.");

        Destination consumerDestination = consumerSession.createQueue(QUEUE_NAME);
        LOGGER.info("Created a queue.");

        MessageConsumer consumer = consumerSession.createConsumer(consumerDestination);
        LOGGER.info("Created a message consumer from the session to the queue.");

        LOGGER.info("Start reading messages from the queue.");
        while (true) {
            String messageText = ((TextMessage) consumer.receive()).getText();

            if ("Poison Pill".equals(messageText)) {
                LOGGER.info("Received Poison Pill. Exiting consumer.");
                break;
            }
            receiveMessageCounter.incrementAndGet();
            csvWriter.checkAndWriteMessage(messageText);
            if (receiveMessageCounter.get() % 10000 == 0) {
                LOGGER.info("Message received: {}", messageText);
            }
        }

        consumer.close();
        consumerSession.close();
        consumerConnection.close();
    }
}
