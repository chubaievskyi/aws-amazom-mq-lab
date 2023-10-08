package com.chubaievskyi;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testParameterizedConstructor() {
        LocalDate createdAt = LocalDate.now();
        User user = new User("User", "19900630-1234", 32, createdAt);

        assertNotNull(user);
        assertEquals("User", user.getName());
        assertEquals("19900630-1234", user.getEddr());
        assertEquals(32, user.getCount());
        assertEquals(createdAt, user.getCreatedAt());
    }

    @Test
    void testGettersAndSetters() {
        User user = new User();

        user.setName("User");
        assertEquals("User", user.getName());

        user.setEddr("19911120-1234");
        assertEquals("19911120-1234", user.getEddr());

        user.setCount(31);
        assertEquals(31, user.getCount());

        LocalDate newDate = LocalDate.of(1990, 6, 30);
        user.setCreatedAt(newDate);
        assertEquals(newDate, user.getCreatedAt());
    }
}
