package com.retailforge.analytics.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardResponse(
    LocalDate metricDate,
    BigDecimal totalRevenue,
    BigDecimal totalCgst,
    BigDecimal totalSgst,
    Long transactionCount
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
