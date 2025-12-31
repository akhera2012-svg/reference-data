package com.example.mrd;

import com.example.mrd.service.CsvImporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ReferenceDataApplication {

    @Value("${app.csv.path:classpath:sample-securities.csv}")
    private String csvPath;

    public static void main(String[] args) {
        SpringApplication.run(ReferenceDataApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(CsvImporter importer) {
        return args -> {
            String path = csvPath;
            if (args != null && args.length > 0) {
                path = args[0];
            }
            importer.importCsv(path);
        };
    }
}
