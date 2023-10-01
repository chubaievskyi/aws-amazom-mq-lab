package com.chubaievskyi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {

    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("Program start!");

//        AmazonMQService amazonMQService = new AmazonMQService();
//        amazonMQService.run();

        MQFactory mqFactory = new MQFactory();
        try {
            mqFactory.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // TODO: мільйон меседжів за адекватний час
        // TODO: подивитися іншу бібліотеку для генерації імен
        // TODO: швидкість продюсера
        // TODO: швидкість консюмера
        // TODO: логи раз на 10 тис. повідомлень
        // TODO: валідатор імені
        // TODO: логери в ексепшинах

        LOGGER.info("End of program!");
    }
}