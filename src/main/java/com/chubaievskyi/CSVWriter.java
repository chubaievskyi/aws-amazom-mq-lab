package com.chubaievskyi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class CSVWriter {
//    private final Lock lock = new ReentrantLock();

    private static final String VALID_FILE_PATH = "./valid-messages.csv";
    private static final String INVALID_FILE_PATH = "./invalid-messages.csv";

    private final CSVPrinter validCsvPrinter;
    private final CSVPrinter invalidCsvPrinter;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public CSVWriter() {
        try {
            validCsvPrinter = new CSVPrinter(new FileWriter(VALID_FILE_PATH, true), CSVFormat.DEFAULT);
            invalidCsvPrinter = new CSVPrinter(new FileWriter(INVALID_FILE_PATH, true), CSVFormat.DEFAULT);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void checkAndWriteMessage(String message) {
//        lock.lock();
        try {
            Map<String, Object> messageMap = objectMapper.readValue(message, Map.class);
            String name = (String) messageMap.get("name");
            String eddr = (String) messageMap.get("eddr");
            int count = (int) messageMap.get("count");
            String createdAt = String.valueOf(messageMap.get("createdAt"));
            if (Validator.validateEDDRNumber(eddr) && Validator.validateName(name) && Validator.validateCount(count)) {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
//            lock.unlock();
        }
    }
}

