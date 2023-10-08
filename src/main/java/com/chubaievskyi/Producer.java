package com.chubaievskyi;

import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Producer implements Runnable {

    public static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);
    private static final UserGenerator USER_GENERATOR = new UserGenerator();

    private final AtomicInteger sendMessageCounter;
    private final PooledConnectionFactory pooledConnectionFactory;
    private final AtomicInteger activeProducerCount;
    private final long startTimeProducer;
    private final String queueName;
    private final long stopTime;
    private final int numberOfMessages;
    private final int numberOfConsumer;

    public Producer(PooledConnectionFactory pooledConnectionFactory, long startTimeProducer,
                    AtomicInteger sendMessageCounter, AtomicInteger activeProducerCount, String queueName,
                    long stopTime, int numberOfMessages, int numberOfConsumer) {
        this.pooledConnectionFactory = pooledConnectionFactory;
        this.activeProducerCount = activeProducerCount;
        this.startTimeProducer = startTimeProducer;
        this.sendMessageCounter = sendMessageCounter;
        this.queueName = queueName;
        this.stopTime = stopTime;
        this.numberOfMessages = numberOfMessages;
        this.numberOfConsumer = numberOfConsumer;
    }

    @Override
    public void run() {
        try {
            sendMessage();
        } catch (JMSException e) {
            LOGGER.debug("Error sending a message.", e);
        }
    }

    private void sendMessage() throws JMSException {
        Connection producerConnection = pooledConnectionFactory.createConnection();
        producerConnection.start();
        LOGGER.info("Connection with the producer is established.");

        Session producerSession = producerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        LOGGER.info("Created a session.");

        Destination producerDestination = producerSession.createQueue(queueName);
        LOGGER.info("Created a queue.");

        MessageProducer producer = producerSession.createProducer(producerDestination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        LOGGER.info("Created a producer from the session to the queue.");

        LOGGER.info("Start sending messages to the queue.");
        long stopTimeProducer = startTimeProducer + (stopTime * 1000);

        Stream.generate(USER_GENERATOR::generateRandomUser)
                .limit(numberOfMessages)
                .takeWhile(text -> System.currentTimeMillis() < stopTimeProducer)
                .filter(text -> sendMessageCounter.get() < (numberOfMessages - (activeProducerCount.get() - 1)))
                .forEach(text -> {
                    try {
                        TextMessage producerMessage = producerSession.createTextMessage(text);
                        producer.send(producerMessage);
                        if (sendMessageCounter.incrementAndGet() % 10000 == 0) {
                            LOGGER.info("Message sent: {}", text);
                        }
                    } catch (JMSException e) {
                        LOGGER.debug("Error sending a message.", e);
                    }
                });

        int remainingProducers = activeProducerCount.decrementAndGet();
        if (remainingProducers == 0) {
            for (int i = 0; i < numberOfConsumer; i++) {
                TextMessage poisonPill = producerSession.createTextMessage("Poison Pill");
                producer.send(poisonPill);
            }
            LOGGER.info("Poison Pill sent to signal the end of production.");
        }

        producer.close();
        producerSession.close();
        producerConnection.close();
    }
}

