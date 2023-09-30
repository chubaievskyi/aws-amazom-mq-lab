package com.chubaievskyi;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static com.chubaievskyi.Main.LOGGER;

public class Consumer implements Runnable {

    private final CSVWriter csvWriter;
    private static final Properties PROPERTIES = new PropertiesLoader().loadProperties();
    private static final InputReader INPUT_READER = new InputReader(PROPERTIES);
    private static final String QUEUE_NAME = INPUT_READER.getQueueName();

    private final ActiveMQConnectionFactory connectionFactory;
//    private final String queueName;
    private AtomicInteger receiveMessageCounter;

    //    private long startTimeConsumer;
    private long endTimeConsumer;

    public Consumer(ActiveMQConnectionFactory connectionFactory, AtomicInteger receiveMessageCounter, CSVWriter csvWriter) {
        this.connectionFactory = connectionFactory;
//        this.queueName = inputReader.getQueueName();
        this.receiveMessageCounter = receiveMessageCounter;
        this.csvWriter = csvWriter;
    }

    @Override
    public void run() {
//        startTimeConsumer = System.currentTimeMillis();
        try {
            receiveMessage();
        } catch (JMSException e) {
            LOGGER.debug("Error receiving a message.", e);
        }
//        endTimeConsumer = System.currentTimeMillis();
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
//                if (poisonPillCount.incrementAndGet() >= 2) { // Змініть на кількість консюмерів
                LOGGER.info("Received Poison Pill. Exiting consumer.");
//                    break;
//                }
                break;
            }
            receiveMessageCounter.incrementAndGet();
            csvWriter.checkAndWriteMessage(messageText);
            if (receiveMessageCounter.get() % 1 == 0) {
                LOGGER.info("Message received: {}", messageText);
//                LOGGER.info("{} message received.", receiveMessageCounter.get());
            }
        }

        consumer.close();
        consumerSession.close();
        consumerConnection.close();
    }
}
