package com.example.mrd.controller;

import com.example.mrd.entity.SecurityData;
import com.example.mrd.repository.SecurityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/securities")
public class SecurityController {

    @Autowired
    private SecurityRepository securityRepository;

    /**
     * Get all securities or securities by ISIN if provided
     * 
     * @param isin Optional ISIN identifier to filter by
     * @return List of securities in JSON format
     */
    @GetMapping
    public ResponseEntity<List<SecurityData>> getSecurities(@RequestParam(required = false) String isin) {
        List<SecurityData> securities;

        if (isin != null && !isin.isEmpty()) {
            securities = securityRepository.findByIsin(isin);
        } else {
            securities = securityRepository.findAll();
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
     * @param isin The ISIN identifier
     * @return List of securities with matching ISIN
     */
    @GetMapping("/isin/{isin}")
    public ResponseEntity<List<SecurityData>> getSecuritiesByIsin(@PathVariable String isin) {
        List<SecurityData> securities = securityRepository.findByIsin(isin);

        if (securities.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(securities);
    }
}
