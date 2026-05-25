package com.retailforge.analytics.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_sales_metrics")
public class DailySalesMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metric_date", nullable = false, unique = true)
    private LocalDate metricDate;

    @Column(name = "total_revenue", nullable = false)
    private BigDecimal totalRevenue;

    @Column(name = "total_cgst", nullable = false)
    private BigDecimal totalCgst;

    @Column(name = "total_sgst", nullable = false)
    private BigDecimal totalSgst;

    @Column(name = "transaction_count", nullable = false)
    private Long transactionCount;

    public DailySalesMetric() {}

    public DailySalesMetric(LocalDate metricDate, BigDecimal totalRevenue, BigDecimal totalCgst, BigDecimal totalSgst, Long transactionCount) {
        this.metricDate = metricDate;
        this.totalRevenue = totalRevenue;
        this.totalCgst = totalCgst;
        this.totalSgst = totalSgst;
        this.transactionCount = transactionCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getMetricDate() {
        return metricDate;
    }

    public void setMetricDate(LocalDate metricDate) {
        this.metricDate = metricDate;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalCgst() {
        return totalCgst;
    }

    public void setTotalCgst(BigDecimal totalCgst) {
        this.totalCgst = totalCgst;
    }

    public BigDecimal getTotalSgst() {
        return totalSgst;
    }

    public void setTotalSgst(BigDecimal totalSgst) {
        this.totalSgst = totalSgst;
    }

    public Long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Long transactionCount) {
        this.transactionCount = transactionCount;
    }
}
