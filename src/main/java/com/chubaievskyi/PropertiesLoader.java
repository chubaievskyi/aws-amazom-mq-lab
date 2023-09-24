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
//            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");

            if (input != null) {
                properties.load(input);
                LOGGER.info("Loaded properties from config.properties in classpath");
            } else {
                LOGGER.info("config.properties not found in classpath, please check the classpath and file .properties");
                System.exit(0);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read properties from file.", e);
        }

        return properties;
    }
}
