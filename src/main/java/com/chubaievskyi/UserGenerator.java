package com.chubaievskyi;

import java.time.LocalDate;
import java.util.Random;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.javafaker.Faker;

public class UserGenerator {

    private final Faker faker;
    private final ObjectMapper objectMapper;
    private final Random random;

    private static final int MIN_BIRTH_YEAR = 1920;
    private static final int MAX_BIRTH_YEAR = 2020;
    private static final int MAX_MONTH = 12;
    private static final int MAX_DAY = 31;
    private static final int MIN_COUNT = 0;
    private static final int MAX_COUNT = 1000;

    public UserGenerator() {
        this.faker = new Faker();
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.random = new Random();
    }

    public String generateRandomUser() throws IOException {

        String name = faker.name().firstName();
        String eddr = generateRandomEddr();
        int count = faker.number().numberBetween(MIN_COUNT, MAX_COUNT);
        LocalDate createdAt = LocalDate.now();

        User user = new User(name, eddr, count, createdAt);

        return objectMapper.writeValueAsString(user);
    }

    private String generateRandomEddr() {

        int year = random.nextInt(MAX_BIRTH_YEAR - MIN_BIRTH_YEAR + 1) + MIN_BIRTH_YEAR;
        int month = random.nextInt(MAX_MONTH) + 1;
        int day = random.nextInt(MAX_DAY) + 1;

        String restOfEddr = faker.numerify("#####");

        return String.format("%04d%02d%02d-%s", year, month, day, restOfEddr);
    }
}
