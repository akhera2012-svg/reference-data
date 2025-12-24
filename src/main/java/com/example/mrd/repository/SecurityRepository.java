package com.example.mrd.repository;

import com.example.mrd.entity.SecurityData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SecurityRepository extends JpaRepository<SecurityData, Long> {
    java.util.List<SecurityData> findByIsin(String isin);

    @Query("SELECT s FROM SecurityData s WHERE s.isin = ?1 AND s.toDate = TO_DATE('9999-12-31 23:59:59', 'YYYY-MM-DD HH24:MI:SS')")
    java.util.List<SecurityData> findActiveByIsin(String isin);
}
