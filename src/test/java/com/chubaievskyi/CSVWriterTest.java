package com.chubaievskyi;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

class CSVWriterTest {

    @Mock
    private CSVPrinter validCsvPrinter;
    @Mock
    private CSVPrinter invalidCsvPrinter;
    @Mock
    private Validator validator;

    private CSVWriter csvWriter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        csvWriter = new CSVWriter(validCsvPrinter, invalidCsvPrinter, validator);
    }

    @Test
    void testCheckAndWriteMessage_ValidUser() throws IOException {

        User validUser = new User("Johnathan", "12345678-12345", 123, LocalDate.ofYearDay(2023, 1));
        String validUserJson = "{ \"name\":\"Johnathan\", \"eddr\":\"12345678-12345\", \"count\":123, \"createdAt\":\"2023-01-01\" }";

        when(validator.validate(validUser)).thenReturn(Collections.emptySet());
        csvWriter.checkAndWriteMessage(validUserJson);

        verify(validCsvPrinter).printRecord("Johnathan", "12345678-12345", 123, "2023-01-01");
        verify(validCsvPrinter).flush();
    }

    @Test
    void testCheckAndWriteMessage_InvalidUser() throws IOException {

        User invalidUser = new User("John", "00000000-00009", 0, LocalDate.ofYearDay(2023, 1));
        String invalidUserJson = "{ \"name\":\"John\", \"eddr\":\"00000000-00009\", \"count\":0, \"createdAt\":\"2023-01-01\" }";

        ConstraintViolation<User> nameViolation = Mockito.mock(ConstraintViolation.class);
        Set<ConstraintViolation<User>> violations = new HashSet<>();
        violations.add(nameViolation);

        when(validator.validate(any(User.class))).thenReturn(violations);
        csvWriter.checkAndWriteMessage(invalidUserJson);

        verify(invalidCsvPrinter).printRecord("John", "00000000-00009", 0, "2023-01-01");
        verify(invalidCsvPrinter).flush();
    }
}