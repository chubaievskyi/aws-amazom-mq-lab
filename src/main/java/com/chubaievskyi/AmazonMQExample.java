//package com.chubaievskyi;
//
//import org.apache.activemq.ActiveMQConnectionFactory;
//import org.apache.activemq.jms.pool.PooledConnectionFactory;
//
//import javax.jms.*;
//
//public class AmazonMQExample {
//
//    // Specify the connection parameters.
//    private final static String WIRE_LEVEL_ENDPOINT
//            = "ssl://b-265a939d-40dc-4ee3-9abb-229e14589feb-1.mq.eu-central-1.amazonaws.com:61617";
//    private final static String ACTIVE_MQ_USERNAME = "user2";
//    private final static String ACTIVE_MQ_PASSWORD = "user1234567890";
//
//    public static void main(String[] args) throws JMSException {
//        final ActiveMQConnectionFactory connectionFactory =
//                createActiveMQConnectionFactory();
//        final PooledConnectionFactory pooledConnectionFactory =
//                createPooledConnectionFactory(connectionFactory);
//
//        sendMessage(pooledConnectionFactory);
//        receiveMessage(connectionFactory);
//
//        pooledConnectionFactory.stop();
//    }
//
//    private static void
//    sendMessage(PooledConnectionFactory pooledConnectionFactory) throws JMSException {
//        // Establish a connection for the producer.
//        final Connection producerConnection = pooledConnectionFactory
//                .createConnection();
//        producerConnection.start();
//
//        // Create a session.
//        final Session producerSession = producerConnection
//                .createSession(false, Session.AUTO_ACKNOWLEDGE);
//
//        // Create a queue named "MyQueue".
//        final Destination producerDestination = producerSession
//                .createQueue("MyQueue");
//
//        // Create a producer from the session to the queue.
//        final MessageProducer producer = producerSession
//                .createProducer(producerDestination);
//        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
//
//        // Create a message.
//        final String text = "Hello from Amazon MQ!";
////        final String text = String.valueOf(UserGenerator.generateRandomUser());
//        final TextMessage producerMessage = producerSession
//                .createTextMessage(text);
//
//        // Send the message.
//        producer.send(producerMessage);
//        System.out.println("Message sent.");
//
//        // Clean up the producer.
//        producer.close();
//        producerSession.close();
//        producerConnection.close();
//    }
//
//    private static void
//    receiveMessage(ActiveMQConnectionFactory connectionFactory) throws JMSException {
//        // Establish a connection for the consumer.
//        // Note: Consumers should not use PooledConnectionFactory.
//        final Connection consumerConnection = connectionFactory.createConnection();
//        consumerConnection.start();
//
//        // Create a session.
//        final Session consumerSession = consumerConnection
//                .createSession(false, Session.AUTO_ACKNOWLEDGE);
//
//        // Create a queue named "MyQueue".
//        final Destination consumerDestination = consumerSession
//                .createQueue("MyQueue");
//
//        // Create a message consumer from the session to the queue.
//        final MessageConsumer consumer = consumerSession
//                .createConsumer(consumerDestination);
//
//        // Begin to wait for messages.
//        final Message consumerMessage = consumer.receive(1000);
//
//        // Receive the message when it arrives.
//        final TextMessage consumerTextMessage = (TextMessage) consumerMessage;
//        System.out.println("Message received: " + consumerTextMessage.getText());
//
//        // Clean up the consumer.
//        consumer.close();
//        consumerSession.close();
//        consumerConnection.close();
//    }
//
//    private static PooledConnectionFactory
//    createPooledConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
//        // Create a pooled connection factory.
//        final PooledConnectionFactory pooledConnectionFactory =
//                new PooledConnectionFactory();
//        pooledConnectionFactory.setConnectionFactory(connectionFactory);
//        pooledConnectionFactory.setMaxConnections(10);
//        return pooledConnectionFactory;
//    }
//
//    private static ActiveMQConnectionFactory createActiveMQConnectionFactory() {
//        // Create a connection factory.
//        final ActiveMQConnectionFactory connectionFactory =
//                new ActiveMQConnectionFactory(WIRE_LEVEL_ENDPOINT);
//
//        // Pass the sign-in credentials.
//        connectionFactory.setUserName(ACTIVE_MQ_USERNAME);
//        connectionFactory.setPassword(ACTIVE_MQ_PASSWORD);
//        return connectionFactory;
//    }
//}