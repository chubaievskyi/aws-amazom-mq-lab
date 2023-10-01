package com.chubaievskyi;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.chubaievskyi.Main.LOGGER;

public class MQFactory {
    CSVWriter csvWriter = new CSVWriter();
    private static final Properties PROPERTIES = new PropertiesLoader().loadProperties();
    private static final InputReader INPUT_READER = new InputReader(PROPERTIES);
    private static final String WIRE_LEVEL_ENDPOINT = INPUT_READER.getWireLevelEndpoint();
    private static final String USER_NAME = INPUT_READER.getUsername();
    private static final String PASSWORD = INPUT_READER.getPassword();
    private static final int NUMBER_OF_PRODUCER = INPUT_READER.getNumberOfProducer();
    private static final int NUMBER_OF_CONSUMER = INPUT_READER.getNumberOfConsumer();
    private final AtomicInteger activeProducerCount = new AtomicInteger(NUMBER_OF_PRODUCER);
    private final AtomicInteger sendMessageCounter = new AtomicInteger(0);
    private final AtomicInteger receiveMessageCounter = new AtomicInteger(0);
    private long startTimeProducer;
    private long startTimeConsumer;
    private long endTimeProducer;
    private long endTimeConsumer;

    public void run() {

        ActiveMQConnectionFactory connectionFactory = createActiveMQConnectionFactory();
        PooledConnectionFactory pooledConnectionFactory = createPooledConnectionFactory(connectionFactory);
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_PRODUCER + NUMBER_OF_CONSUMER);
        CountDownLatch producersLatch = new CountDownLatch(NUMBER_OF_PRODUCER);

        startTimeProducer = System.currentTimeMillis();
        for (int i = 0; i < NUMBER_OF_PRODUCER; i++) {
            Producer producer = new Producer(
                    pooledConnectionFactory,
                    startTimeProducer,
                    sendMessageCounter,
                    activeProducerCount,
                    producersLatch
                    );
            executorService.submit(producer);
        }

        startTimeConsumer = System.currentTimeMillis();
        for (int i = 0; i < NUMBER_OF_CONSUMER; i++) {
            Consumer consumer = new Consumer(
                    connectionFactory,
                    receiveMessageCounter,
                    csvWriter
            );
            executorService.submit(consumer);
        }

        try {
            producersLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        endTimeProducer = System.currentTimeMillis();
        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                LOGGER.error("Not all threads have terminated.");
            }
        } catch (InterruptedException e) {
            LOGGER.debug("Executor service interrupted.", e);
            Thread.currentThread().interrupt();
        }
        endTimeConsumer = System.currentTimeMillis();

        csvWriter.close();
        pooledConnectionFactory.stop();
        printResult();
    }

    private ActiveMQConnectionFactory createActiveMQConnectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(WIRE_LEVEL_ENDPOINT);
        connectionFactory.setTrustedPackages(List.of("com.chubaievskyi"));
        LOGGER.info("Created a connection factory.");

        connectionFactory.setUserName(USER_NAME);
        connectionFactory.setPassword(PASSWORD);
        return connectionFactory;
    }

    private PooledConnectionFactory createPooledConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
        pooledConnectionFactory.setConnectionFactory(connectionFactory);
        pooledConnectionFactory.setMaxConnections(100);
        LOGGER.info("Created a pooled connection factory.");
        return pooledConnectionFactory;
    }

    private void printResult() {
        double producerTime = (double) (endTimeProducer - startTimeProducer) / 1000;
        LOGGER.info("Producer time (sec) - {}", producerTime);
        LOGGER.info("Number of sent messages - {}", sendMessageCounter);
        double consumerTime = (double) (endTimeConsumer - startTimeConsumer) / 1000;
        LOGGER.info("Consumer time (sec) - {}", consumerTime);
        LOGGER.info("Number of received messages - {}", receiveMessageCounter);

        double averageEndingSpeed = sendMessageCounter.get() / producerTime;
        double averageReceivingSpeed = receiveMessageCounter.get() / consumerTime;
        String formattedAverageEndingSpeed = String.format("%.2f", averageEndingSpeed);
        String formattedAverageReceivingSpeed = String.format("%.2f", averageReceivingSpeed);
        LOGGER.info("Average speed of sending messages (messages per second) - {}", formattedAverageEndingSpeed);
        LOGGER.info("Average speed of receiving messages (messages per second) - {}", formattedAverageReceivingSpeed);
    }
}
