package com.example.mrd.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.mrd.entity.SecurityData;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class SecurityDataKafkaConsumer {

    private final SecurityDataImporter csvImporter;
    private final String csvDirectoryPath;

    public SecurityDataKafkaConsumer(SecurityDataImporter csvImporter,
            @Value("${app.csv.directory.path:./data}") String csvDirectoryPath) {
        this.csvImporter = csvImporter;
        this.csvDirectoryPath = csvDirectoryPath;
    }

    // @KafkaListener(topics = "${app.kafka.topic.security-data}", groupId =
    // "${spring.kafka.consumer.group-id}")
    public void consumeSecurityData(String message) {
        System.out.println("*** Received message from Kafka: " + message);
        try {
            // Create a temporary CSV file from the received data
            String csvFilePath = createCsvFileFromMessage(message);
            System.out.println("*** Successfully created security data file from Kafka message, path = "
                    + csvFilePath);
        } catch (Exception e) {
            System.err.println("*** Failed to process Kafka message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "${app.kafka.topic.security-data}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(SecurityData securityData) {
        System.out.println("Received security data from Kafka: " + securityData.getIsin());
        try {
            csvImporter.importSecurityData(securityData);
            System.out.println("Successfully processed security: " + securityData.getIsin());
        } catch (Exception e) {
            System.err.println("Error processing security data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a temporary CSV file from the Kafka message
     * The message is expected to be in CSV format (can be single row or multiple
     * rows)
     */
    private String createCsvFileFromMessage(String message) throws IOException {
        // Ensure the directory exists
        Path dirPath = Paths.get(csvDirectoryPath).toAbsolutePath();
        Files.createDirectories(dirPath);

        // Generate a unique filename based on timestamp and UUID
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(dateFormatter);
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String filename = "kafka-import-" + timestamp + "-" + uuid + ".csv";

        Path filePath = dirPath.resolve(filename);

        // Write the message content to the file
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.write(message);
            writer.flush();
        }

        System.out.println("*** Created temporary CSV file: " + filePath.toString());
        return filePath.toAbsolutePath().toString();
    }
}
