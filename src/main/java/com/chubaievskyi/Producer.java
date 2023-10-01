package com.chubaievskyi;

import org.apache.activemq.jms.pool.PooledConnectionFactory;
import javax.jms.*;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static com.chubaievskyi.Main.LOGGER;

public class Producer implements Runnable {

    private static final UserGenerator USER_GENERATOR = new UserGenerator();
    private static final Properties PROPERTIES = new PropertiesLoader().loadProperties();
    private static final InputReader INPUT_READER = new InputReader(PROPERTIES);
    private static final String QUEUE_NAME = INPUT_READER.getQueueName();
    private static final long STOP_TIME = INPUT_READER.getStopTime();
    private static final int NUMBER_OF_MESSAGES = INPUT_READER.getNumberOfMessages();
    private static final int NUMBER_OF_CONSUMER = INPUT_READER.getNumberOfConsumer();

    private final AtomicInteger sendMessageCounter;
    private final PooledConnectionFactory pooledConnectionFactory;
    private final AtomicInteger activeProducerCount;
    private final long startTimeProducer;
    private final CountDownLatch producersLatch;

    public Producer(PooledConnectionFactory pooledConnectionFactory, long startTimeProducer,
                    AtomicInteger sendMessageCounter, AtomicInteger activeProducerCount, CountDownLatch producersLatch) {
        this.pooledConnectionFactory = pooledConnectionFactory;
        this.activeProducerCount = activeProducerCount;
        this.startTimeProducer = startTimeProducer;
        this.sendMessageCounter = sendMessageCounter;
        this.producersLatch = producersLatch;
    }

    @Override
    public void run() {
        try {
            sendMessage();
        } catch (JMSException | IOException e) {
            LOGGER.debug("Error sending a message.", e);
        } finally {
            producersLatch.countDown();
        }
    }

    private void sendMessage() throws JMSException, IOException {
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

        LOGGER.info("Start sending messages to the queue.");
        long stopTimeProducer = startTimeProducer + (STOP_TIME * 1000);
        while (sendMessageCounter.get() < NUMBER_OF_MESSAGES || System.currentTimeMillis() >= stopTimeProducer) {
            if (Thread.currentThread().isInterrupted()) {
                LOGGER.debug("Producer is interrupted.");
                break;
            }
            if (sendMessageCounter.get() >= (NUMBER_OF_MESSAGES - (activeProducerCount.get() - 1))) {
                break;
            }

            String text = USER_GENERATOR.generateRandomUser();
            TextMessage producerMessage = producerSession.createTextMessage(text);
            producer.send(producerMessage);
            if (sendMessageCounter.incrementAndGet() % 10000 == 0) {
                LOGGER.info("Message sent: {}", text);
            }
        }

        int remainingProducers = activeProducerCount.decrementAndGet();
        if (remainingProducers == 0) {
            for (int i = 0; i < NUMBER_OF_CONSUMER; i++) {
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

