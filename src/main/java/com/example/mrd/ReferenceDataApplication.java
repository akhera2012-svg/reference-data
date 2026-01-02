package com.example.mrd;

import com.example.mrd.service.CsvImporter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ReferenceDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReferenceDataApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(CsvImporter importer) {
        return args -> {
            // If a file path is provided via command-line, import that single file.
            if (args != null && args.length > 0) {
                importer.importCsv(args[0]);
            }
            // Otherwise, do nothing here: `CsvDirectoryWatcher` handles directory imports.
        };
    }
}
