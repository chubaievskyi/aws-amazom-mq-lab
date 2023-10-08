package com.chubaievskyi;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserGeneratorTest {

    @Test
    void testGenerateRandomUser() {
        UserGenerator userGenerator = new UserGenerator();
        String randomUser = userGenerator.generateRandomUser();

        assertNotNull(randomUser);
        assertFalse(randomUser.isEmpty());
        assertTrue(randomUser.contains("name"));
        assertTrue(randomUser.contains("eddr"));
        assertTrue(randomUser.contains("count"));
        assertTrue(randomUser.contains("createdAt"));
    }

    @Test
    void testGenerateRandomDate() {
        UserGenerator userGenerator = new UserGenerator();
        String randomDate = userGenerator.generateRandomDate();

        assertNotNull(randomDate);
        assertEquals(8, randomDate.length());
    }

    @Test
    void testGenerateRandomEddr() {
        UserGenerator userGenerator = new UserGenerator();
        String randomEddr = userGenerator.generateRandomEddr();

        assertNotNull(randomEddr);
        assertEquals(14, randomEddr.length());
        assertTrue(randomEddr.matches("\\d{8}-\\d{5}"));
    }
}
