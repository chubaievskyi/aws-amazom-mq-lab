package com.chubaievskyi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("Program start!");

        MQFactory mqFactory = new MQFactory();
        mqFactory.run();

        // TODO: мільйон меседжів за адекватний час
        // TODO: подивитися іншу бібліотеку для генерації імен
        // TODO: логери в ексепшинах

        LOGGER.info("End of program!");
    }
}