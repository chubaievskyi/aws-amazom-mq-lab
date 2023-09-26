//package com.chubaievskyi;
//
//import org.apache.activemq.ActiveMQConnectionFactory;
//import org.apache.activemq.jms.pool.PooledConnectionFactory;
//
//import javax.jms.*;
//import java.io.IOException;
//import java.util.List;
//import java.util.Properties;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static com.chubaievskyi.Main.LOGGER;
//
//public class AmazonMQServicev1 {
//
//    private static final UserGenerator USER_GENERATOR = new UserGenerator();
//    private static final Properties PROPERTIES = new PropertiesLoader().loadProperties();
//    private static final InputReader INPUT_READER = new InputReader(PROPERTIES);
//    private static final String WIRE_LEVEL_ENDPOINT = INPUT_READER.getWireLevelEndpoint();
//    private static final String USER_NAME = INPUT_READER.getUsername();
//    private static final String PASSWORD = INPUT_READER.getPassword();
//    private static final String QUEUE_NAME = INPUT_READER.getQueueName();
//    private static final String STOP_TIME = INPUT_READER.getStopTime();
//    private static final int NUMBER_OF_MESSAGES = INPUT_READER.getNumberOfMessages();
//    private static final int NUMBER_OF_PRODUCER = 4;
//    private static final int NUMBER_OF_CONSUMER = 2;
//
//    private final AtomicInteger sendMessageCounter = new AtomicInteger(0);
//    private final AtomicInteger receiveMessageCounter = new AtomicInteger(0);
//    private final AtomicInteger activeProducerCount = new AtomicInteger(NUMBER_OF_PRODUCER);
//    private long startTimeProducer;
//    private long startTimeConsumer;
//    private long endTimeProducer;
//    private long endTimeConsumer;
//
//    public void run() {
//
//        ActiveMQConnectionFactory connectionFactory = createActiveMQConnectionFactory();
//        PooledConnectionFactory pooledConnectionFactory = createPooledConnectionFactory(connectionFactory);
//
//        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_PRODUCER + NUMBER_OF_CONSUMER);
//
//        startTimeProducer = System.currentTimeMillis();
//        for (int i = 0; i < NUMBER_OF_PRODUCER; i++) {
//            executorService.submit(() -> {
//                try {
//                    sendMessage(pooledConnectionFactory);
//                } catch (JMSException | IOException e) {
//                    LOGGER.debug("Error sending a message.", e);
//                }
//            });
//        }
//
//        startTimeConsumer = System.currentTimeMillis();
//        for (int i = 0; i < NUMBER_OF_CONSUMER; i++) {
//            executorService.submit(() -> {
//                try {
//                    receiveMessage(connectionFactory);
//                } catch (JMSException e) {
//                    LOGGER.debug("Error receiving a message.", e);
//                }
//            });
//        }
//
//        executorService.shutdown();
//        try {
//            if (!executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
//                LOGGER.error("Not all threads have terminated.");
//            }
//        } catch (InterruptedException e) {
//            LOGGER.debug("Executor service interrupted.", e);
//            Thread.currentThread().interrupt();
//        }
//
//        pooledConnectionFactory.stop();
//
//        double producerTime = (double) (endTimeProducer - startTimeProducer) / 1000;
//        LOGGER.info("Producer time (sec) - {}", producerTime);
//        LOGGER.info("Number of sent messages - {}", sendMessageCounter);
//        double consumerTime = (double) (endTimeConsumer - startTimeConsumer) / 1000;
//        LOGGER.info("Consumer time (sec) - {}", consumerTime);
//        LOGGER.info("Number of received messages - {}", receiveMessageCounter);
//
//        double averageEndingSpeed = sendMessageCounter.get() / producerTime;
//        double averageReceivingSpeed = receiveMessageCounter.get() / consumerTime;
//        String formattedAverageEndingSpeed = String.format("%.2f", averageEndingSpeed);
//        String formattedAverageReceivingSpeed = String.format("%.2f", averageReceivingSpeed);
//        LOGGER.info("Average speed of sending messages (messages per second) - {}", formattedAverageEndingSpeed);
//        LOGGER.info("Average speed of receiving messages (messages per second) - {}", formattedAverageReceivingSpeed);
//    }
//
//    private void sendMessage(PooledConnectionFactory pooledConnectionFactory) throws JMSException, IOException {
//        Connection producerConnection = pooledConnectionFactory.createConnection();
//        producerConnection.start();
//        LOGGER.info("Connection with the producer is established.");
//
//        Session producerSession = producerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//        LOGGER.info("Created a session.");
//
//        Destination producerDestination = producerSession.createQueue(QUEUE_NAME);
//        LOGGER.info("Created a queue.");
//
//        MessageProducer producer = producerSession.createProducer(producerDestination);
//        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
//        LOGGER.info("Created a producer from the session to the queue.");
//
//        long stopTimeProducer = startTimeProducer + (Long.parseLong(STOP_TIME) * 1000);
//        LOGGER.info("Start sending messages to the queue.");
//        while (sendMessageCounter.get() < NUMBER_OF_MESSAGES || System.currentTimeMillis() >= stopTimeProducer) {
//            if (Thread.currentThread().isInterrupted()) {
//                LOGGER.debug("Producer is interrupted.");
//                break;
//            }
//
//            String text = USER_GENERATOR.generateRandomUser();
//            TextMessage producerMessage = producerSession.createTextMessage(text);
//            producer.send(producerMessage);
//
//            if (sendMessageCounter.incrementAndGet() % 10000 == 0) {
//                LOGGER.info("Message sent: {}", text);
////                LOGGER.info("{} message sent.", sendMessageCounter.get());
//            }
//        }
//
//        int remainingProducers = activeProducerCount.decrementAndGet();
//        if (remainingProducers == 0) {
//            for (int i = 0; i < NUMBER_OF_CONSUMER; i++) {
//                TextMessage poisonPill = producerSession.createTextMessage("Poison Pill");
//                producer.send(poisonPill);
//            }
//            LOGGER.info("Poison Pill sent to signal the end of production.");
//        }
//
//        endTimeProducer = System.currentTimeMillis();
//        producer.close();
//        producerSession.close();
//        producerConnection.close();
//    }
//
//
//    private void receiveMessage(ActiveMQConnectionFactory connectionFactory) throws JMSException {
//        Connection consumerConnection = connectionFactory.createConnection();
//        consumerConnection.start();
//        LOGGER.info("Connection with the consumer is established.");
//
//        Session consumerSession = consumerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//        LOGGER.info("Created a session.");
//
//        Destination consumerDestination = consumerSession.createQueue(QUEUE_NAME);
//        LOGGER.info("Created a queue.");
//
//        MessageConsumer consumer = consumerSession.createConsumer(consumerDestination);
//        LOGGER.info("Created a message consumer from the session to the queue.");
//
//        LOGGER.info("Start reading messages from the queue.");
//        while (true) {
//            String messageText = ((TextMessage) consumer.receive()).getText();
//
//            if ("Poison Pill".equals(messageText)) {
//                LOGGER.info("Received Poison Pill. Exiting consumer.");
//                break;
//            }
//
//            if (receiveMessageCounter.incrementAndGet() % 10000 == 0) {
//                LOGGER.info("Message received: {}", messageText);
////                LOGGER.info("{} message received.", receiveMessageCounter.get());
//            }
//        }
//
//        endTimeConsumer = System.currentTimeMillis();
//        consumer.close();
//        consumerSession.close();
//        consumerConnection.close();
//    }
//
//    private ActiveMQConnectionFactory createActiveMQConnectionFactory() {
//        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(WIRE_LEVEL_ENDPOINT);
//        connectionFactory.setTrustedPackages(List.of("com.chubaievskyi"));
//        LOGGER.info("Created a connection factory.");
//
//        connectionFactory.setUserName(USER_NAME);
//        connectionFactory.setPassword(PASSWORD);
//        return connectionFactory;
//    }
//
//    private PooledConnectionFactory createPooledConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
//        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
//        pooledConnectionFactory.setConnectionFactory(connectionFactory);
//        pooledConnectionFactory.setMaxConnections(10);
//        LOGGER.info("Created a pooled connection factory.");
//        return pooledConnectionFactory;
//    }
//}