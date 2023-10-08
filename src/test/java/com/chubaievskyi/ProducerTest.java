package com.chubaievskyi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicInteger;

class ProducerTest {

    @Mock
    private PooledConnectionFactory factory;
    @Mock
    private Connection connection;
    @Mock
    private Session session;
    @Mock
    private MessageProducer messageProducer;
    @Mock
    private Queue queue;

    private Producer producer;
    private AtomicInteger sendMessageCounter;
    private AtomicInteger activeProducerCount;
    private long startTimeProducer;
    private String queueName;
    private int numberOfConsumer;


    @BeforeEach
    public void setUp() throws JMSException {
        MockitoAnnotations.openMocks(this);

        factory = mock(PooledConnectionFactory.class);
        sendMessageCounter = new AtomicInteger(0);
        activeProducerCount = new AtomicInteger(1);
        startTimeProducer = System.currentTimeMillis();
        queueName = "myTestQueue";
        numberOfConsumer = 1;

        when(factory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createProducer(any(Destination.class))).thenReturn(messageProducer);
        when(session.createQueue(anyString())).thenReturn(queue);
    }

    @Test
    void testMessageSendingTimeout() throws InterruptedException {
        long stopTime = 5;
        int numberOfMessages = 1_000_000;
        producer = new Producer(factory, startTimeProducer, sendMessageCounter, activeProducerCount, queueName,
                                stopTime, numberOfMessages, numberOfConsumer);

        Thread producerThread = new Thread(producer);
        producerThread.start();
        producerThread.join();

        long elapsedTime = System.currentTimeMillis() - startTimeProducer;
        assertTrue((elapsedTime - stopTime * 1000) <= 1000, "Sending messages took longer than expected");
        producerThread.interrupt();
    }

    @Test
    void testAllMessagesSent() throws InterruptedException {
        long stopTime = 1000;
        int numberOfMessages = 100;
        producer = new Producer(factory, startTimeProducer, sendMessageCounter, activeProducerCount, queueName,
                                stopTime, numberOfMessages, numberOfConsumer);

        Thread producerThread = new Thread(producer);
        producerThread.start();
        producerThread.join();
        assertEquals(numberOfMessages, sendMessageCounter.get());
    }
}
