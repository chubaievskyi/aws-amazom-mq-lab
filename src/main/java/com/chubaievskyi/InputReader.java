package com.chubaievskyi;

import java.util.Properties;

import static com.chubaievskyi.Main.LOGGER;

public class InputReader {

    private static final int DEFAULT_NUMBER_OF_MESSAGES = 1001;
    private String wireLevelEndpoint;
    private String username;
    private String password;
    private int numberOfMessages;
    private final Properties properties;
    private final String[] args;

    public InputReader(Properties properties, String[] args) {
        this.properties = properties;
        this.args = args;
        checkNumberOfMessages();
        readPropertiesValue();
    }

    private void checkNumberOfMessages() {
        if (args.length == 0) {
            LOGGER.error("The number of messages to be generated is not specified. " +
                    "The default number (1001) will be generated.");
            numberOfMessages = DEFAULT_NUMBER_OF_MESSAGES;
        } else {
            try {
                numberOfMessages = Integer.parseInt(args[0]);
                LOGGER.info("{} notifications will be generated.", numberOfMessages);
            } catch (NumberFormatException e) {
                LOGGER.error("The number of messages to be generated is incorrect.", e);
            }
        }
    }

    private void readPropertiesValue() {
        LOGGER.info("Read the values of properties.");
        wireLevelEndpoint = properties.getProperty("wire.level.endpoint");
        username = properties.getProperty("username");
        password = properties.getProperty("password");
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
}
