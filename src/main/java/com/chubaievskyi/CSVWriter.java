package com.chubaievskyi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CSVWriter {

    private static final String VALID_FILE_PATH = "valid-messages.csv";
    private static final String INVALID_FILE_PATH = "invalid-messages.csv";

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public void checkAndWriteMessage(String message) {

        try {
            // Розпарсити JSON-строку у об'єкт класу User
            User user = objectMapper.readValue(message, User.class);

            // Отримати значення полів з об'єкта User
            String name = user.getName();
            String eddr = user.getEddr();
            int count = user.getCount();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        // розпарсити строку на об'єкти
        // взяти перше значення (юзер) та перевірити на валідність
        // взяти друге (єддр) та перевірии на валідність
        // взяти третє та перевірити на валідність

        // якщо всі умови виконані - повідомлення валідне, пишемо в файл з валідними значеннями
        // якщо хочаб одна з умов не валідна - повідомлення не валідне - пишемо у файл з не валідними повідомленнями
    }
    public void writeValidMessage(String message) throws IOException {
        writeMessage(VALID_FILE_PATH, message);
    }

    public void writeInvalidMessage(String message) throws IOException {
        writeMessage(INVALID_FILE_PATH, message);
    }

    private void writeMessage(String filePath, String message) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
            writer.println(message);
        }
    }
}

