package com.chubaievskyi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

public class CSVWriter {

    public static final Logger LOGGER = LoggerFactory.getLogger(CSVWriter.class);
    private static final Properties PROPERTIES = new PropertiesLoader().loadProperties();
    private static final InputReader INPUT_READER = new InputReader(PROPERTIES);
    private static final String VALID_FILE_PATH = INPUT_READER.getValidFilePath();
    private static final String INVALID_FILE_PATH = INPUT_READER.getInvalidFilePath();

    private final CSVPrinter validCsvPrinter;
    private final CSVPrinter invalidCsvPrinter;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Validator validator;

    //    private final Lock lock = new ReentrantLock();

    public CSVWriter() {
        try {
            validCsvPrinter = new CSVPrinter(new FileWriter(VALID_FILE_PATH, true), CSVFormat.DEFAULT);
            invalidCsvPrinter = new CSVPrinter(new FileWriter(INVALID_FILE_PATH, true), CSVFormat.DEFAULT);
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            validator = factory.getValidator();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void checkAndWriteMessage(String message) {
//        lock.lock();
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
            throw new RuntimeException(e);
        } finally {
//            lock.unlock();
        }
    }

    public void close() {
//        lock.lock();
        try {
            validCsvPrinter.close();
            invalidCsvPrinter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
//            lock.unlock();
        }
    }
}

