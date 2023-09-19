package com.chubaievskyi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.chubaievskyi.Main.LOGGER;

public class PropertiesLoader {

    private final Properties properties;

    public PropertiesLoader() {
        properties = new Properties();
    }

    public Properties loadProperties() {
        try {
            InputStream externalInput = Main.class.getClassLoader().getResourceAsStream("config.properties");
            if (externalInput != null) {
                properties.load(externalInput);
                LOGGER.info("Loaded properties from external file config.properties");
            } else {
                InputStream internalInput = Main.class.getClassLoader().getResourceAsStream("config.properties");
                properties.load(internalInput);
                LOGGER.info("Loaded properties from the internal file config.properties");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read properties from file.", e);
        }

        return properties;
    }

//    public Properties loadProperties() {
//        try {
//            InputStream externalInput = getClass().getResourceAsStream("/config.properties");
//            if (externalInput != null) {
//                properties.load(externalInput);
//                LOGGER.info("Loaded properties from external file config.properties");
//            } else {
//                InputStream internalInput = getClass().getResourceAsStream("/config.properties");
//                properties.load(internalInput);
//                LOGGER.info("Loaded properties from the internal file config.properties");
//            }
//        } catch (IOException e) {
//            LOGGER.error("Failed to read properties from file.", e);
//        }
//
//        return properties;
//    }
}
