package com.chubaievskyi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class InputReader {

    public static final Logger LOGGER = LoggerFactory.getLogger(InputReader.class);
    private static final int DEFAULT_NUMBER_OF_MESSAGES = 10001;
    private String wireLevelEndpoint;
    private String username;
    private String password;
    private String queueName;
    private long stopTime;
    private int numberOfProducer;
    private int numberOfConsumer;
    private String validFilePath;
    private String invalidFilePath;

    private int numberOfMessages;

    private final Properties properties;
    public InputReader(Properties properties) {
        this.properties = properties;
        checkNumberOfMessages();
        readPropertiesValue();
    }

    private void checkNumberOfMessages() {
        String numberOfMessagesProperty = System.getProperty("nm");
        if (numberOfMessagesProperty == null) {
            LOGGER.error("The number of messages to be generated is not specified. " +
                    "The default number (10001) will be generated.");
            numberOfMessages = DEFAULT_NUMBER_OF_MESSAGES;
        } else {
            try {
                numberOfMessages = Integer.parseInt(numberOfMessagesProperty);
                LOGGER.info("{} notifications will be generated.", numberOfMessages);
            } catch (NumberFormatException e) {
                LOGGER.error("The number of messages to be generated is incorrect.", e);
            }
        }
    }

    private void readPropertiesValue() {
        LOGGER.info("Read the values of properties.");
        wireLevelEndpoint = properties.getProperty("wire.level.endpoint");
        username = properties.getProperty("user.name");
        password = properties.getProperty("password");
        queueName = properties.getProperty("queue.name");
        stopTime = Long.parseLong(properties.getProperty("stop.time"));
        numberOfProducer = Integer.parseInt(properties.getProperty("number.of.producer"));
        numberOfConsumer = Integer.parseInt(properties.getProperty("number.of.consumer"));
        validFilePath = properties.getProperty("valid.file.path");
        invalidFilePath = properties.getProperty("invalid.file.path");
    }

    public String getWireLevelEndpoint() {
        return wireLevelEndpoint;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getNumberOfMessages() {
        return numberOfMessages;
    }

    public String getQueueName() {
        return queueName;
    }

    public long getStopTime() {
        return stopTime;
    }

    public int getNumberOfProducer() {
        return numberOfProducer;
    }

    public int getNumberOfConsumer() {
        return numberOfConsumer;
    }

    public String getValidFilePath() {
        return validFilePath;
    }

    public String getInvalidFilePath() {
        return invalidFilePath;
    }
}
