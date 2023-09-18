package com.chubaievskyi;

import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDate;
import java.util.Random;

public class UserGenerator {
    public static User generateRandomUser() {
        Random random = new Random();
        LocalDate currentDate = LocalDate.now();

        // Генеруємо випадкову довжину імені від 1 до 25 символів
        int nameLength = random.nextInt(15) + 1;
        // Генеруємо випадкове ім'я заданої довжини
        String randomName = RandomStringUtils.randomAlphabetic(nameLength);

        // Генеруємо випадковий erdd, складається з 13 цифр
        String randomEddr = RandomStringUtils.randomNumeric(13);

        // Генеруємо випадкову дату від 1900 року до сьогодні
        int minYear = 1900;
        int maxYear = currentDate.getYear();
        int randomYear = random.nextInt(maxYear - minYear + 1) + minYear;
        int randomDayOfYear = random.nextInt(currentDate.lengthOfYear()) + 1; // +1 для уникнення нульового значення

        LocalDate randomDate = LocalDate.ofYearDay(randomYear, randomDayOfYear);

        return new User(
                randomName,    // Генеруємо випадкове ім'я
                randomEddr,    // Генеруємо випадковий erdd
                random.nextInt(100),  // Генеруємо випадкове число від 0 до 99
                randomDate  // Генеруємо випадкову дату
        );
    }
}
