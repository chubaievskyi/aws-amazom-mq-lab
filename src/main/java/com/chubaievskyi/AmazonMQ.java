package com.chubaievskyi;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;

import javax.jms.*;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static com.chubaievskyi.Main.LOGGER;

public class AmazonMQ {

    private static final UserGenerator USER_GENERATOR = new UserGenerator();
    private static final Properties PROPERTIES = new PropertiesLoader().loadProperties();
    private static final InputReader INPUT_READER = new InputReader(PROPERTIES);
    private static final String WIRE_LEVEL_ENDPOINT = INPUT_READER.getWireLevelEndpoint();
    private static final String USER_NAME = INPUT_READER.getUsername();
    private static final String PASSWORD = INPUT_READER.getPassword();
    private static final String QUEUE_NAME = INPUT_READER.getQueueName();
    private static final String STOP_TIME = INPUT_READER.getStopTime();
    private static final int NUMBER_OF_MESSAGES = INPUT_READER.getNumberOfMessages();

    public static void main(String[] args) {
        ActiveMQConnectionFactory connectionFactory = createActiveMQConnectionFactory();
        PooledConnectionFactory pooledConnectionFactory = createPooledConnectionFactory(connectionFactory);

        Thread producerThread = new Thread(() -> {
            try {
                sendMessage(pooledConnectionFactory);
            } catch (JMSException | IOException e) {
                LOGGER.debug("Error sending a message.", e);
            }
        });

        Thread consumerThread = new Thread(() -> {
            try {
                receiveMessage(connectionFactory);
            } catch (JMSException e) {
                LOGGER.debug("Error receiving a message.", e);
            }
        });

        producerThread.start();
        consumerThread.start();

        try {
            producerThread.join();
            consumerThread.join();
        } catch (InterruptedException e) {
            LOGGER.debug("The current thread has been interrupted.", e);
            Thread.currentThread().interrupt();
        }
        pooledConnectionFactory.stop();
    }

    private static void sendMessage(PooledConnectionFactory pooledConnectionFactory) throws JMSException, IOException {
        Connection producerConnection = pooledConnectionFactory.createConnection();
        producerConnection.start();
        LOGGER.info("Connection with the producer is established.");

        Session producerSession = producerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        LOGGER.info("Created a session.");

        Destination producerDestination = producerSession.createQueue(QUEUE_NAME);
        LOGGER.info("Created a queue.");

        MessageProducer producer = producerSession.createProducer(producerDestination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        LOGGER.info("Created a producer from the session to the queue.");

        int count = 0;
        long time = System.currentTimeMillis() + (Long.parseLong(STOP_TIME) * 1000);
        for (int i = 0; i < NUMBER_OF_MESSAGES; i++) {
            if (Thread.currentThread().isInterrupted()) {
                LOGGER.debug("Producer is interrupted.");
                break;
            }

            String text = USER_GENERATOR.generateRandomUser();
            TextMessage producerMessage = producerSession.createTextMessage(text);
            LOGGER.info("Message created: {}", text);

            producer.send(producerMessage);
            LOGGER.info("Message sent: {}", text);
            count++;

            if (System.currentTimeMillis() >= time || count == NUMBER_OF_MESSAGES) {
                TextMessage poisonPill = producerSession.createTextMessage("Poison Pill");
                producer.send(poisonPill);
                LOGGER.info("Poison Pill sent to signal the end of production.");
            }

        }

        producer.close();
        producerSession.close();
        producerConnection.close();
    }

    private static void receiveMessage(ActiveMQConnectionFactory connectionFactory) throws JMSException {
        Connection consumerConnection = connectionFactory.createConnection();
        consumerConnection.start();
        LOGGER.info("Connection with the consumer is established.");

        Session consumerSession = consumerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        LOGGER.info("Created a session.");

        Destination consumerDestination = consumerSession.createQueue(QUEUE_NAME);
        LOGGER.info("Created a queue.");

        MessageConsumer consumer = consumerSession.createConsumer(consumerDestination);
        LOGGER.info("Created a message consumer from the session to the queue.");

        while (true) {
            Message consumerMessage = consumer.receive(1000); // Wait for a message
            if (consumerMessage instanceof TextMessage) {
                TextMessage consumerTextMessage = (TextMessage) consumerMessage;
                String messageText = consumerTextMessage.getText();
                LOGGER.info("Message received: {}", messageText);

                if ("Poison Pill".equals(messageText)) {
                    LOGGER.info("Received Poison Pill. Exiting consumer.");
                    break;
                }
            }
        }

        consumer.close();
        consumerSession.close();
        consumerConnection.close();
    }

    private static ActiveMQConnectionFactory createActiveMQConnectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(WIRE_LEVEL_ENDPOINT);
        connectionFactory.setTrustedPackages(List.of("com.chubaievskyi"));
        LOGGER.info("Created a connection factory.");

        connectionFactory.setUserName(USER_NAME);
        connectionFactory.setPassword(PASSWORD);
        return connectionFactory;
    }

    private static PooledConnectionFactory createPooledConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
        pooledConnectionFactory.setConnectionFactory(connectionFactory);
        pooledConnectionFactory.setMaxConnections(10);
        LOGGER.info("Created a pooled connection factory.");
        return pooledConnectionFactory;
    }
}