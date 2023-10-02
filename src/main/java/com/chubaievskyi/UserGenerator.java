package com.chubaievskyi;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserGenerator {

    public static final Logger LOGGER = LoggerFactory.getLogger(UserGenerator.class);
    private final Faker faker;
    private final ObjectMapper objectMapper;

    private static final int MIN_BIRTH_YEAR = 1920;
    private static final int MAX_BIRTH_YEAR = 2020;
    private static final int MIN_COUNT = 0;
    private static final int MAX_COUNT = 1000;

    public UserGenerator() {
        this.faker = new Faker();
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public String generateRandomUser() {

        String name = faker.name().firstName();
        String eddr = generateRandomEddr();
        int count = faker.number().numberBetween(MIN_COUNT, MAX_COUNT);
        LocalDate createdAt = LocalDate.now();

        User user = new User(name, eddr, count, createdAt);

        try {
            return objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            LOGGER.debug("Error trying to convert an object to a JSON string", e);
        }
        return "";
    }

    private String generateRandomEddr() {
        String date = generateRandomDate();
        String restOfEddr = faker.numerify("#####");
        return String.format("%s-%s", date, restOfEddr);
    }

    public String generateRandomDate() {
        int dayOfYear = ThreadLocalRandom.current().nextInt(1, 366);
        int year = ThreadLocalRandom.current().nextInt(MIN_BIRTH_YEAR, MAX_BIRTH_YEAR + 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return LocalDate.ofYearDay(year, dayOfYear).format(formatter);
    }
}
