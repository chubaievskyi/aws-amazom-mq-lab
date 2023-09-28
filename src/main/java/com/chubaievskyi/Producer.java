//package com.chubaievskyi;
//
//import org.apache.activemq.jms.pool.PooledConnectionFactory;
//import javax.jms.*;
//import java.io.IOException;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static com.chubaievskyi.Main.LOGGER;
//
//public class Producer implements Runnable {
//    private static final UserGenerator USER_GENERATOR = new UserGenerator();
//    private AtomicInteger sendMessageCounter = new AtomicInteger(0);
//
//    private final PooledConnectionFactory pooledConnectionFactory;
//    private final String queueName;
//    private final int numberOfMessages;
//    private final int numberOfConsumer;
//    private final long stopTime;
//    private final AtomicInteger activeProducerCount;
//    private long startTimeProducer;
//    private long endTimeProducer;
//
//    public Producer(PooledConnectionFactory pooledConnectionFactory, InputReader inputReader, long startTimeProducer, AtomicInteger sendMessageCounter, AtomicInteger activeProducerCount) {
//        this.pooledConnectionFactory = pooledConnectionFactory;
//        this.queueName = inputReader.getQueueName();
//        this.numberOfMessages = inputReader.getNumberOfMessages();
//        this.numberOfConsumer = inputReader.getNumberOfConsumer();
//        this.stopTime = inputReader.getStopTime();
////        this.activeProducerCount = new AtomicInteger(inputReader.getNumberOfProducer());
//        this.activeProducerCount = activeProducerCount;
//        this.startTimeProducer = startTimeProducer;
//        this.sendMessageCounter = sendMessageCounter;
//    }
//
//    @Override
//    public void run() {
////        startTimeProducer = System.currentTimeMillis();
//        try {
//            sendMessage();
//        } catch (JMSException | IOException e) {
//            LOGGER.debug("Error sending a message.", e);
//        }
////        endTimeProducer = System.currentTimeMillis();
//    }
//
//    private void sendMessage() throws JMSException, IOException {
//        Connection producerConnection = pooledConnectionFactory.createConnection();
//        producerConnection.start();
//        LOGGER.info("Connection with the producer is established.");
//
//        Session producerSession = producerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//        LOGGER.info("Created a session.");
//
//        Destination producerDestination = producerSession.createQueue(queueName);
//        LOGGER.info("Created a queue.");
//
//        MessageProducer producer = producerSession.createProducer(producerDestination);
//        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
//        LOGGER.info("Created a producer from the session to the queue.");
//
//        LOGGER.info("Start sending messages to the queue.");
//        long stopTimeProducer = startTimeProducer + (stopTime * 1000);
//        while (sendMessageCounter.get() < numberOfMessages || System.currentTimeMillis() >= stopTimeProducer) {
//            if (Thread.currentThread().isInterrupted()) {
//                LOGGER.debug("Producer is interrupted.");
//                break;
//            }
//
//            String text = USER_GENERATOR.generateRandomUser();
//            TextMessage producerMessage = producerSession.createTextMessage(text);
//            producer.send(producerMessage);
//            if (sendMessageCounter.incrementAndGet() % 10000 == 0) {
//                LOGGER.info("Message sent: {}", text);
////                LOGGER.info("{} message sent.", sendMessageCounter.get());
//            }
//        }
//
//        int remainingProducers = activeProducerCount.decrementAndGet();
//        if (remainingProducers == 0) {
//            for (int i = 0; i < numberOfConsumer; i++) {
//                TextMessage poisonPill = producerSession.createTextMessage("Poison Pill");
//                producer.send(poisonPill);
//            }
//            LOGGER.info("Poison Pill sent to signal the end of production.");
//        }
//
//        producer.close();
//        producerSession.close();
//        producerConnection.close();
//    }
//}
//
