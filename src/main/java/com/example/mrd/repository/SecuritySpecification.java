package com.example.mrd.repository;

import com.example.mrd.entity.SecurityData;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SecuritySpecification {

    public static Specification<SecurityData> filterByCriteria(Map<String, String> filters, boolean includeInactive) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by active/inactive status
            if (!includeInactive) {
                LocalDateTime eternity = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
                predicates.add(cb.equal(root.get("toDate"), eternity));
            }

            // Apply field-specific filters
            filters.forEach((key, value) -> {
                if (value != null && !value.trim().isEmpty()) {
                    switch (key.toLowerCase()) {
                        case "cusip":
                            predicates.add(cb.like(cb.lower(root.get("cusip")), "%" + value.toLowerCase() + "%"));
                            break;
                        case "isin":
                            predicates.add(cb.like(cb.lower(root.get("isin")), "%" + value.toLowerCase() + "%"));
                            break;
                        case "ticker":
                            predicates.add(cb.like(cb.lower(root.get("ticker")), "%" + value.toLowerCase() + "%"));
                            break;
                        case "currency":
                            predicates.add(cb.like(cb.lower(root.get("currency")), "%" + value.toLowerCase() + "%"));
                            break;
                        case "country":
                            predicates.add(cb.like(cb.lower(root.get("country")), "%" + value.toLowerCase() + "%"));
                            break;
                        case "issuercode":
                            predicates.add(cb.like(cb.lower(root.get("issuerCode")), "%" + value.toLowerCase() + "%"));
                            break;
                        case "cins":
                            predicates.add(cb.like(cb.lower(root.get("cins")), "%" + value.toLowerCase() + "%"));
                            break;
                        case "securitytype":
                            predicates
                                    .add(cb.like(cb.lower(root.get("securityType")), "%" + value.toLowerCase() + "%"));
                            break;
                        case "issuedate":
                            try {
                                LocalDate filterDate = LocalDate.parse(value);
                                predicates.add(cb.equal(root.get("issueDate"), filterDate));
                            } catch (Exception ignored) {
                                // Ignore date parsing errors
                            }
                            break;
                    }
                }
            });

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}