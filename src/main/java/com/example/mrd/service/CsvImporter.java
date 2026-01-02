package com.example.mrd.service;

import com.example.mrd.entity.SecurityData;
import com.example.mrd.repository.SecurityRepository;
import com.opencsv.CSVReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CsvImporter {

    private final SecurityRepository repository;
    private final ResourceLoader resourceLoader;

    private final DateTimeFormatter[] formatters = new DateTimeFormatter[] {
            DateTimeFormatter.ISO_DATE,
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    public CsvImporter(SecurityRepository repository, ResourceLoader resourceLoader) {
        this.repository = repository;
        this.resourceLoader = resourceLoader;
    }

    @Transactional
    public void importCsv(String path) {
        System.out.println("!!! Importing CSV from path: " + path);
        try {
            Resource resource;
            System.out.println("!!!!! Index of back slash - " + path.indexOf("\\"));
            // Use String.replace to avoid regex interpretation of backslash
            System.out.println("!!! Importing CSV from modified path: " + path);
            if (path.startsWith("classpath:")) {
                resource = resourceLoader.getResource(path);
            } else {
                resource = resourceLoader.getResource("file:" + path);
            }
            Reader reader = new InputStreamReader(resource.getInputStream());
            try (CSVReader csvReader = new CSVReader(reader)) {
                String[] header = csvReader.readNext();
                if (header == null) {
                    System.out.println("Empty CSV");
                    return;
                }
                List<SecurityData> batch = new ArrayList<>();
                List<SecurityData> toUpdate = new ArrayList<>();
                String[] row;
                while ((row = csvReader.readNext()) != null) {
                    SecurityData s = mapRow(header, row);
                    if (s == null)
                        continue;

                    String isin = s.getIsin();
                    if (isin == null || isin.isBlank()) {
                        // no identifier, always insert with timestamps
                        s.setFromDate(LocalDateTime.now());
                        s.setToDate(LocalDateTime.of(9999, 12, 31, 23, 59, 59));
                        batch.add(s);
                        continue;
                    }

                    List<SecurityData> existing = repository.findByIsin(isin);
                    boolean hasExact = false;
                    for (SecurityData ex : existing) {
                        if (nonIdentifierFieldsEqual(ex, s)) {
                            hasExact = true;
                            break;
                        }
                    }
                    System.out.println("!!!!! " + hasExact);
                    if (!hasExact) {
                        // New record with different non-identifier fields for same ISIN
                        // Close off the old active record
                        for (SecurityData ex : existing) {
                            if (ex.getToDate() != null &&
                                    ex.getToDate().equals(LocalDateTime.of(9999, 12, 31, 23, 59, 59))) {
                                // This is the active record, close it
                                ex.setToDate(LocalDateTime.now());
                                toUpdate.add(ex);
                            }
                        }
                        // Insert new row with same ISIN
                        s.setFromDate(LocalDateTime.now());
                        s.setToDate(LocalDateTime.of(9999, 12, 31, 23, 59, 59));
                        batch.add(s);
                    } else {
                        // identical record already exists -> skip
                    }
                }
                // Update the old records with new toDate
                if (!toUpdate.isEmpty()) {
                    repository.saveAll(toUpdate);
                }
                // Insert new records
                repository.saveAll(batch);
                System.out.println("Imported " + batch.size() + " new rows into DB.");
            }
            // Rename the file to .done after successful import
            // Move this outside try-with-resources to ensure file handle is released on
            // Windows
            renameFileToProcessed(path);
        } catch (Exception e) {
            System.err.println("Failed to import CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean nonIdentifierFieldsEqual(SecurityData a, SecurityData b) {
        if (a == null || b == null)
            return false;
        if (!Objects.equals(a.getIssueDate(), b.getIssueDate()))
            return false;
        if (!Objects.equals(norm(a.getTicker()), norm(b.getTicker())))
            return false;
        if (!Objects.equals(norm(a.getCurrency()), norm(b.getCurrency())))
            return false;
        if (!Objects.equals(norm(a.getCountry()), norm(b.getCountry())))
            return false;
        if (!Objects.equals(norm(a.getSecurityDesc()), norm(b.getSecurityDesc())))
            return false;
        if (!Objects.equals(norm(a.getSecurityType()), norm(b.getSecurityType())))
            return false;
        return true;
    }

    private String norm(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private SecurityData mapRow(String[] header, String[] row) {
        SecurityData s = new SecurityData();
        for (int i = 0; i < header.length && i < row.length; i++) {
            String col = header[i].trim().toLowerCase();
            String val = row[i].trim();
            switch (col) {
                case "cusip":
                    s.setCusip(val);
                    break;
                case "isin":
                    s.setIsin(val);
                    break;
                case "cins":
                    s.setCins(val);
                    break;
                case "issuer_code":
                case "issuercode":
                    s.setIssuerCode(val);
                    break;
                case "issue_date":
                case "issuedate":
                    s.setIssueDate(parseDate(val));
                    break;
                case "ticker":
                    s.setTicker(val);
                    break;
                case "currency":
                    s.setCurrency(val);
                    break;
                case "country":
                    s.setCountry(val);
                    break;
                case "security_desc":
                case "securitydesc":
                    s.setSecurityDesc(val);
                    break;
                case "security_type":
                case "securitytype":
                    s.setSecurityType(val);
                    break;
                default:
                    // ignore unknown
            }
        }
        return s;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isEmpty())
            return null;
        for (DateTimeFormatter f : formatters) {
            try {
                return LocalDate.parse(value, f);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private void renameFileToProcessed(String filePath) {
        try {
            // Extract actual file path from "file:" prefix if present
            String actualPath = filePath;
            if (filePath.startsWith("file:")) {
                actualPath = filePath.substring(5);
            }

            Path path = Paths.get(actualPath);
            Path newPath = Paths.get(actualPath + ".done");

            // Retry logic for Windows file locking issues
            int retries = 3;
            int delayMs = 500;
            Exception lastException = null;

            for (int i = 0; i < retries; i++) {
                try {
                    Files.move(path, newPath);
                    System.out.println("Renamed file from " + path.getFileName() + " to " + newPath.getFileName());
                    return; // Success
                } catch (Exception e) {
                    lastException = e;
                    if (i < retries - 1) {
                        System.out.println("Rename attempt " + (i + 1) + " failed, retrying in " + delayMs + "ms...");
                        Thread.sleep(delayMs);
                        delayMs *= 2; // Exponential backoff
                    }
                }
            }

            // All retries failed
            if (lastException != null) {
                System.err.println(
                        "Failed to rename file to .done after " + retries + " attempts: " + lastException.getMessage());
                lastException.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Failed to rename file to .done: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
