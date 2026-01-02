package com.example.mrd;

import com.example.mrd.repository.SecurityRepository;
import com.example.mrd.service.CsvImporter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ImporterIntegrationTest {

    @Autowired
    CsvImporter importer;

    @Autowired
    SecurityRepository repo;

    @Test
    void importCsv_and_deduplicateByIsin() {
        repo.deleteAll();
        importer.importCsv("classpath:test-securities.csv");

        List<?> isin123 = repo.findByIsin("ISIN123");
        assertThat(isin123.size()).isEqualTo(2);

        List<?> isin456 = repo.findByIsin("ISIN456");
        assertThat(isin456.size()).isEqualTo(1);

        long total = repo.count();
        assertThat(total).isEqualTo(3);
    }

    @Test
    void deleteRepo() {
        repo.deleteAll();
    }
}
