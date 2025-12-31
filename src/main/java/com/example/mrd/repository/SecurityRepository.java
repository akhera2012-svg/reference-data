package com.example.mrd.repository;

import com.example.mrd.entity.SecurityData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SecurityRepository extends JpaRepository<SecurityData, Long> {
    List<SecurityData> findByIsin(String isin);

    List<SecurityData> findByToDate(LocalDateTime toDate);

    List<SecurityData> findByIsinAndToDate(String isin, LocalDateTime toDate);
}
