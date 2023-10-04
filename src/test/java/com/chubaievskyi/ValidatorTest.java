package com.chubaievskyi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintValidatorContext;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {

    private Validator validator;
    private ConstraintValidatorContext context;
    private LocalDate localDate;

    @BeforeEach
    public void setUp() {
        validator = new Validator();
        context = Mockito.mock(ConstraintValidatorContext.class);
        localDate = LocalDate.now();
    }

    @Test
    void testIsValidWithInvalidEddr() {
        User user = new User("Alice", "invalid-eddr", 20, localDate);
        boolean result = validator.isValid(user, context);
        assertFalse(result);
    }

    @Test
    void testIsValidWithInvalidName() {
        User user = new User("1234567", "12345678-12345", 25, localDate);
        boolean result = validator.isValid(user, context);
        assertFalse(result);
    }

    @Test
    void testIsValidWithInvalidCount() {
        User user = new User("Bob", "12345678-12345", 5, localDate);
        boolean result = validator.isValid(user, context);
        assertFalse(result);
    }
}

