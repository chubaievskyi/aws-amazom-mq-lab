package com.chubaievskyi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class InputReaderTest {

    private Properties testProperties;
    @BeforeEach
    void setUp() throws IOException {
        testProperties = new Properties();
        try (InputStream inputStream = InputReaderTest.class.getResourceAsStream("/test.properties")) {
            testProperties.load(inputStream);
        }
    }

    @Test
    void testInputReaderWithProperties() {
        InputReader inputReader = new InputReader(testProperties);

        assertNotNull(inputReader);
        assertEquals("ssl://b-265a939d-40dc-4ee3-9abb-229e14589feb-1.mq.eu-central-1.amazonaws.com:61617",
                                                                                inputReader.getWireLevelEndpoint());
        assertEquals("user2", inputReader.getUsername());
        assertEquals("user1234567890", inputReader.getPassword());
        assertEquals("userQueue", inputReader.getQueueName());
        assertEquals(200, inputReader.getStopTime());
        assertEquals(4, inputReader.getNumberOfProducer());
        assertEquals(2, inputReader.getNumberOfConsumer());
        assertEquals("./valid-messages.csv", inputReader.getValidFilePath());
        assertEquals("./invalid-messages.csv", inputReader.getInvalidFilePath());
    }
}