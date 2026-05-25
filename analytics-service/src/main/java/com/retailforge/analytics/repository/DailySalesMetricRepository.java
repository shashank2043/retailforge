package com.retailforge.analytics.repository;

import com.retailforge.analytics.model.DailySalesMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailySalesMetricRepository extends JpaRepository<DailySalesMetric, Long> {
    Optional<DailySalesMetric> findByMetricDate(LocalDate metricDate);
}
