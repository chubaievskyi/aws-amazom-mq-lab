package com.chubaievskyi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.jms.*;

import java.util.concurrent.atomic.AtomicInteger;

class ConsumerTest {

    @Mock
    private ActiveMQConnectionFactory factory;
    @Mock
    private Connection connection;
    @Mock
    private Session session;
    @Mock
    private Queue queue;
    @Mock
    private MessageConsumer messageConsumer;
    @Mock
    private TextMessage textMessage;
    @Mock
    private CSVWriter csvWriter;

    private AtomicInteger receiveMessageCounter;
    private Consumer consumer;

    @BeforeEach
    public void setUp() throws JMSException {
        MockitoAnnotations.openMocks(this);

        when(factory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createQueue(anyString())).thenReturn(queue);
        when(session.createConsumer(queue)).thenReturn(messageConsumer);
        when(messageConsumer.receive()).thenReturn(textMessage);

        receiveMessageCounter = new AtomicInteger(0);

        consumer = new Consumer(factory, receiveMessageCounter, csvWriter);
    }

    @Test
    void testReceiveMessage() throws JMSException {
        when(textMessage.getText()).thenReturn("Test Message 1", "Test Message 2", "Test Message 3",
                                                    "Test Message 4", "Test Message 5", "Poison Pill");
        consumer.run();

        verify(connection).start();
        verify(csvWriter, times(5)).checkAndWriteMessage(anyString());
        verify(session).close();
        verify(connection).close();

        assertEquals(5, receiveMessageCounter.get());
    }

    @Test
    void testReceiveMessageWithPoisonPill() throws JMSException {
        when(textMessage.getText()).thenReturn("Poison Pill");

        consumer.run();

        verify(connection).start();
        verify(csvWriter, never()).checkAndWriteMessage(anyString());
        verify(session).close();
        verify(connection).close();
    }
}

