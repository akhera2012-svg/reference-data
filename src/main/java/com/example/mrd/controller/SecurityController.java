package com.example.mrd.controller;

import com.example.mrd.entity.SecurityData;
import com.example.mrd.repository.SecurityRepository;
import com.example.mrd.repository.SecuritySpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/securities")
@CrossOrigin(origins = "*")
public class SecurityController {

    @Autowired
    private SecurityRepository securityRepository;

    private static final LocalDateTime ETERNITY = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    /**
     * Get securities with flexible filtering by multiple criteria
     * 
     * @param isin            Optional ISIN identifier to filter by
     * @param cusip           Optional CUSIP to filter by
     * @param ticker          Optional ticker to filter by
     * @param currency        Optional currency to filter by
     * @param country         Optional country to filter by
     * @param issuerCode      Optional issuer code to filter by
     * @param cins            Optional CINS to filter by
     * @param securityType    Optional security type to filter by
     * @param issueDate       Optional issue date to filter by (YYYY-MM-DD format)
     * @param includeInactive If true, returns all records (active and inactive).
     *                        Default is false (active only)
     * @return List of securities in JSON format
     */
    @GetMapping
    public ResponseEntity<List<SecurityData>> getSecurities(
            @RequestParam(required = false) String isin,
            @RequestParam(required = false) String cusip,
            @RequestParam(required = false) String ticker,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String issuerCode,
            @RequestParam(required = false) String cins,
            @RequestParam(required = false) String securityType,
            @RequestParam(required = false) String issueDate,
            @RequestParam(defaultValue = "false") boolean includeInactive) {

        // Build filter map
        Map<String, String> filters = new HashMap<>();
        if (isin != null && !isin.isEmpty())
            filters.put("isin", isin);
        if (cusip != null && !cusip.isEmpty())
            filters.put("cusip", cusip);
        if (ticker != null && !ticker.isEmpty())
            filters.put("ticker", ticker);
        if (currency != null && !currency.isEmpty())
            filters.put("currency", currency);
        if (country != null && !country.isEmpty())
            filters.put("country", country);
        if (issuerCode != null && !issuerCode.isEmpty())
            filters.put("issuercode", issuerCode);
        if (cins != null && !cins.isEmpty())
            filters.put("cins", cins);
        if (securityType != null && !securityType.isEmpty())
            filters.put("securitytype", securityType);
        if (issueDate != null && !issueDate.isEmpty())
            filters.put("issuedate", issueDate);

        // Fetch using specification
        List<SecurityData> securities = securityRepository.findAll(
                SecuritySpecification.filterByCriteria(filters, includeInactive));

        // securities.forEach(System.out::println);

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
}
