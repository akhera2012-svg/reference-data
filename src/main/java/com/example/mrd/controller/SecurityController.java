package com.example.mrd.controller;

import com.example.mrd.entity.SecurityData;
import com.example.mrd.repository.SecurityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/securities")
public class SecurityController {

    @Autowired
    private SecurityRepository securityRepository;

    private static final LocalDateTime ETERNITY = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    /**
     * Get all securities or securities by ISIN if provided
     * 
     * @param isin            Optional ISIN identifier to filter by
     * @param includeInactive If true, returns all records (active and inactive).
     *                        Default is false (active only)
     * @return List of securities in JSON format
     */
    @GetMapping
    public ResponseEntity<List<SecurityData>> getSecurities(
            @RequestParam(required = false) String isin,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        List<SecurityData> securities;

        if (includeInactive) {
            // Return all records
            if (isin != null && !isin.isEmpty()) {
                securities = securityRepository.findByIsin(isin);
            } else {
                securities = securityRepository.findAll();
            }
        } else {
            // Return active records only (default)
            if (isin != null && !isin.isEmpty()) {
                securities = securityRepository.findByIsinAndToDate(isin, ETERNITY);
            } else {
                securities = securityRepository.findByToDate(ETERNITY);
            }
        }

        if (securities.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(securities);
    }

    /**
     * Get a specific security by its database ID
     * 
     * @param id The security ID
     * @return The security record if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<SecurityData> getSecurityById(@PathVariable Long id) {
        Optional<SecurityData> security = securityRepository.findById(id);
        return security.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all securities with a specific ISIN
     * 
     * @param isin            The ISIN identifier
     * @param includeInactive If true, returns all records (active and inactive).
     *                        Default is false (active only)
     * @return List of securities with matching ISIN
     */
    @GetMapping("/isin/{isin}")
    public ResponseEntity<List<SecurityData>> getSecuritiesByIsin(
            @PathVariable String isin,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        List<SecurityData> securities;

        if (includeInactive) {
            securities = securityRepository.findByIsin(isin);
        } else {
            securities = securityRepository.findByIsinAndToDate(isin, ETERNITY);
        }

        if (securities.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(securities);
    }
}
