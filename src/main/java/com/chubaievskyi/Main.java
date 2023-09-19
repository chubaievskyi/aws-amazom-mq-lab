package com.chubaievskyi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class Main {

    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("Program start!");

        Properties properties = new PropertiesLoader().loadProperties();
        InputReader inputReader = new InputReader(properties, args);
        String wireLevelEndpoint = inputReader.getWireLevelEndpoint();
        String username = inputReader.getUsername();
        String password = inputReader.getPassword();
        int numberOfMessages = inputReader.getNumberOfMessages();

        System.out.println(wireLevelEndpoint);
        System.out.println(username);
        System.out.println(password);
        System.out.println(numberOfMessages);

        UserGenerator userGenerator = new UserGenerator();
        for (int i = 0; i < 10; i++) {
            String user = null;
            try {
                user = userGenerator.generateRandomUser();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(user);
        }

        // TODO: власні валідатори (едрр)
        // TODO: продюсер
        // TODO: консьюмер
        // TODO: кілька продюсерів
        // TODO: кілька консюмерів
        // TODO: декілька потоків
        // TODO: мільйон меседжів за адекватний час

        // кількість продюсерів та кількість консмерів у проперті для того щоб знати скільки пойзенпілів використовувати
        // едвайзер меседж (в ідеалі один пойзен піл і всі його ловлять)
        // visualVM дивитися як себе поводять треди
    }
}