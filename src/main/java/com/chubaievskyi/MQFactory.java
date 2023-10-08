package com.chubaievskyi;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MQFactory {

    public static final Logger LOGGER = LoggerFactory.getLogger(MQFactory.class);
    private static final Properties PROPERTIES = new PropertiesLoader().loadProperties();
    private static final InputReader INPUT_READER = new InputReader(PROPERTIES);
    private static final String WIRE_LEVEL_ENDPOINT = INPUT_READER.getWireLevelEndpoint();
    private static final String USER_NAME = INPUT_READER.getUsername();
    private static final String PASSWORD = INPUT_READER.getPassword();
    private static final String VALID_FILE_PATH = INPUT_READER.getValidFilePath();
    private static final String INVALID_FILE_PATH = INPUT_READER.getInvalidFilePath();
    private static final String QUEUE_NAME = INPUT_READER.getQueueName();
    private static final int NUMBER_OF_MESSAGES = INPUT_READER.getNumberOfMessages();
    private static final int NUMBER_OF_PRODUCER = INPUT_READER.getNumberOfProducer();
    private static final int NUMBER_OF_CONSUMER = INPUT_READER.getNumberOfConsumer();
    private static final long STOP_TIME = INPUT_READER.getStopTime();

    private final CSVWriter csvWriter = createCSVWriter();
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

        ExecutorService producerExecutor = Executors.newFixedThreadPool(NUMBER_OF_PRODUCER);
        ExecutorService consumerExecutor = Executors.newFixedThreadPool(NUMBER_OF_CONSUMER);

        startTimeProducer = System.currentTimeMillis();
        for (int i = 0; i < NUMBER_OF_PRODUCER; i++) {
            Producer producer = new Producer(pooledConnectionFactory, startTimeProducer, sendMessageCounter,
                        activeProducerCount, QUEUE_NAME, STOP_TIME, NUMBER_OF_MESSAGES, NUMBER_OF_CONSUMER);
            producerExecutor.submit(producer);
        }

        startTimeConsumer = System.currentTimeMillis();
        for (int i = 0; i < NUMBER_OF_CONSUMER; i++) {
            Consumer consumer = new Consumer(connectionFactory, receiveMessageCounter, csvWriter);
            consumerExecutor.submit(consumer);
        }

        shutdownAndAwaitTermination(producerExecutor, "producer");
        endTimeProducer = System.currentTimeMillis();

        shutdownAndAwaitTermination(consumerExecutor, "consumer");
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

    private CSVWriter createCSVWriter() {
        CSVPrinter validCsvPrinter;
        CSVPrinter invalidCsvPrinter;
        Validator validator;

        try {
            validCsvPrinter = new CSVPrinter(new FileWriter(VALID_FILE_PATH, true), CSVFormat.DEFAULT);
            invalidCsvPrinter = new CSVPrinter(new FileWriter(INVALID_FILE_PATH, true), CSVFormat.DEFAULT);
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            validator = factory.getValidator();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new CSVWriter(validCsvPrinter, invalidCsvPrinter, validator);
    }

    private void shutdownAndAwaitTermination(ExecutorService executor, String threadType) {
        double expectancyRatio = 1.1;
        long waitingTime;
        if (threadType.equals("producer")) {
            waitingTime = (long) (STOP_TIME * expectancyRatio);
        } else {
            waitingTime = Long.MAX_VALUE;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(waitingTime, TimeUnit.SECONDS)) {
                LOGGER.error("Not all {} threads have terminated.", threadType);
            }
        } catch (InterruptedException e) {
            LOGGER.debug("Executor service interrupted for " + threadType + " threads.", e);
            executor.shutdownNow();
        }
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
