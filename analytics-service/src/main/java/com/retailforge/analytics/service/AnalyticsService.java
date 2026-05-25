package com.retailforge.analytics.service;

import com.retailforge.api.response.ApiResponse;
import com.retailforge.analytics.client.BillingClient;
import com.retailforge.analytics.dto.DashboardResponse;
import com.retailforge.analytics.dto.OrderItemResponse;
import com.retailforge.analytics.dto.OrderResponse;
import com.retailforge.analytics.model.DailySalesMetric;
import com.retailforge.analytics.repository.DailySalesMetricRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final DailySalesMetricRepository metricRepository;
    private final BillingClient billingClient;

    public AnalyticsService(DailySalesMetricRepository metricRepository, BillingClient billingClient) {
        this.metricRepository = metricRepository;
        this.billingClient = billingClient;
    }

    @Transactional
    public void processPaymentCompleted(Long orderId, BigDecimal amount) {
        log.info("Processing sales telemetry for order ID: {}", orderId);
        LocalDate today = LocalDate.now();

        BigDecimal gstTotal = BigDecimal.ZERO;

        try {
            ApiResponse<OrderResponse> responseEnvelope = billingClient.getOrderDetails(orderId);
            if (responseEnvelope != null && responseEnvelope.isSuccess() && responseEnvelope.getData() != null) {
                OrderResponse order = responseEnvelope.getData();
                if (order.items() != null) {
                    for (OrderItemResponse item : order.items()) {
                        if (item.gstAmount() != null) {
                            gstTotal = gstTotal.add(item.gstAmount());
                        }
                    }
                }
            } else {
                log.warn("Could not retrieve full order details for order ID: {}. Operating with zero GST defaults.", orderId);
            }
        } catch (Exception e) {
            log.error("Failed to query order details for order ID: {}. Error: {}", orderId, e.getMessage());
        }

        // CGST and SGST are split 50/50 of the total GST
        BigDecimal gstShare = gstTotal.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

        DailySalesMetric metric = metricRepository.findByMetricDate(today)
            .orElseGet(() -> new DailySalesMetric(today, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L));

        metric.setTotalRevenue(metric.getTotalRevenue().add(amount).setScale(2, RoundingMode.HALF_UP));
        metric.setTotalCgst(metric.getTotalCgst().add(gstShare).setScale(2, RoundingMode.HALF_UP));
        metric.setTotalSgst(metric.getTotalSgst().add(gstShare).setScale(2, RoundingMode.HALF_UP));
        metric.setTransactionCount(metric.getTransactionCount() + 1);

        metricRepository.save(metric);
        log.info("Successfully updated daily sales telemetry for {}: Revenue={}, CGST={}, SGST={}, TxCount={}",
            today, metric.getTotalRevenue(), metric.getTotalCgst(), metric.getTotalSgst(), metric.getTransactionCount());
    }

    @Cacheable(value = "analytics", key = "'dashboard'")
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardMetrics() {
        log.info("Cache miss! Querying MySQL database to calculate sales metrics...");
        LocalDate today = LocalDate.now();

        return metricRepository.findByMetricDate(today)
            .map(m -> new DashboardResponse(
                m.getMetricDate(),
                m.getTotalRevenue(),
                m.getTotalCgst(),
                m.getTotalSgst(),
                m.getTransactionCount()
            ))
            .orElseGet(() -> new DashboardResponse(
                today,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0L
            ));
    }
}
