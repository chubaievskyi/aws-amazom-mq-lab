package com.chubaievskyi;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.chubaievskyi.Main.LOGGER;

public class Consumer implements Runnable {

    private final ActiveMQConnectionFactory connectionFactory;
    private final String queueName;
    private final AtomicInteger receiveMessageCounter = new AtomicInteger(0);

//    private long startTimeConsumer;
//    private long endTimeConsumer;

    public Consumer(ActiveMQConnectionFactory connectionFactory, InputReader inputReader) {
        this.connectionFactory = connectionFactory;
        this.queueName = inputReader.getQueueName();
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
//        double consumerTime = (double) (endTimeConsumer - startTimeConsumer) / 1000;
//        LOGGER.info("Consumer time (sec) - {}", consumerTime);
//        LOGGER.info("Number of received messages - {}", receiveMessageCounter);
//
//        double averageReceivingSpeed = receiveMessageCounter.get() / consumerTime;
//        String formattedAverageReceivingSpeed = String.format("%.2f", averageReceivingSpeed);
//        LOGGER.info("Average speed of receiving messages (messages per second) - {}", formattedAverageReceivingSpeed);
    }

    private void receiveMessage() throws JMSException {
        Connection consumerConnection = connectionFactory.createConnection();
        consumerConnection.start();
        LOGGER.info("Connection with the consumer is established.");

        Session consumerSession = consumerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        LOGGER.info("Created a session.");

        Destination consumerDestination = consumerSession.createQueue(queueName);
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
            if (receiveMessageCounter.incrementAndGet() % 2 == 0) {
                LOGGER.info("Message received: {}", messageText);
//                LOGGER.info("{} message received.", receiveMessageCounter.get());
            }

//            if (receiveMessageCounter.incrementAndGet() % 10000 == 0) {
//                LOGGER.info("Message received: {}", messageText);
////                LOGGER.info("{} message received.", receiveMessageCounter.get());
//            }
        }

        consumer.close();
        consumerSession.close();
        consumerConnection.close();
    }
}
