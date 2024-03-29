package com.chubaievskyi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        LOGGER.info("Program start!");

        MQFactory mqFactory = new MQFactory();
        mqFactory.run();

        LOGGER.info("End of program!");
    }
}