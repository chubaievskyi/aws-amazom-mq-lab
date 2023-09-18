package com.chubaievskyi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        for (int i = 0; i < 10; i++) {
            User user = UserGenerator.generateRandomUser();
            System.out.println(user.getName() + " " + user.getEddr() + " " + user.getCount() + " " + user.getCreatedAt());
        }

        // TODO: власні валідатори (едрр)
        // TODO: продюсер
        // TODO: консьюмер
        // TODO: кілька продюсерів
        // TODO: кілька консюмерів
        // TODO: декілька потоків
        // TODO: мільйон меседжів за адекватний час
        // TODO: поджо месседж в форматі джейсон (бібліотека джексон)


        // кількість продюсерів та кількість консмерів у проперті для того щоб знати скільки пойзенпілів використовувати
        // едвайзер меседж (в ідеалі один пойзен піл і всі його ловлять)
        // visualVM дивитися як себе поводять треди
    }
}