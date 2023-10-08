package com.chubaievskyi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

public class CSVWriter {

    public static final Logger LOGGER = LoggerFactory.getLogger(CSVWriter.class);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final CSVPrinter validCsvPrinter;
    private final CSVPrinter invalidCsvPrinter;
    private final Validator validator;

    public CSVWriter(CSVPrinter validCsvPrinter, CSVPrinter invalidCsvPrinter, Validator validator) {
        this.validCsvPrinter = validCsvPrinter;
        this.invalidCsvPrinter = invalidCsvPrinter;
        this.validator = validator;

    }

    public void checkAndWriteMessage(String message) {
        try {
            User user = objectMapper.readValue(message, User.class);
            String name = user.getName();
            String eddr = user.getEddr();
            int count = user.getCount();
            String createdAt = String.valueOf(user.getCreatedAt());

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            if (violations.isEmpty()) {
                validCsvPrinter.printRecord(name, eddr, count, createdAt);
                validCsvPrinter.flush();
            } else {
                invalidCsvPrinter.printRecord(name, eddr, count, createdAt);
                invalidCsvPrinter.flush();
            }
        } catch (IOException e) {
            LOGGER.debug("Error writing a message.", e);
        }
    }

    public void close() {
        try {
            validCsvPrinter.close();
            invalidCsvPrinter.close();
        } catch (IOException e) {
            LOGGER.debug("Error closing printers.", e);
        }
    }
}

