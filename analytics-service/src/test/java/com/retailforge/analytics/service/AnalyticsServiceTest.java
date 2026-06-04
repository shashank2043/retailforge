package com.retailforge.analytics.service;

import com.retailforge.api.response.ApiResponse;
import com.retailforge.analytics.client.BillingClient;
import com.retailforge.analytics.dto.DashboardResponse;
import com.retailforge.dto.OrderItemResponse;
import com.retailforge.dto.OrderResponse;
import com.retailforge.analytics.model.DailySalesMetric;
import com.retailforge.analytics.repository.DailySalesMetricRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceTest {

    @Mock
    private DailySalesMetricRepository metricRepository;

    @Mock
    private BillingClient billingClient;

    @InjectMocks
    private AnalyticsService analyticsService;

    private DailySalesMetric metric;
    private OrderResponse orderResponse;

    @BeforeEach
    public void setup() {
        metric = new DailySalesMetric(LocalDate.now(), BigDecimal.valueOf(100.00), BigDecimal.valueOf(9.00), BigDecimal.valueOf(9.00), 5L);
        orderResponse = new OrderResponse(1L, "ORD-12345", BigDecimal.valueOf(59.00), "COMPLETED", LocalDateTime.now(), List.of(
            new OrderItemResponse(10L, 100L, "Mango Juice", 2, BigDecimal.valueOf(25.00), BigDecimal.valueOf(9.00))
        ));
    }

    @Test
    public void testProcessPaymentCompleted_ExistingMetric() {
        when(metricRepository.findByMetricDate(LocalDate.now())).thenReturn(Optional.of(metric));
        when(metricRepository.save(any(DailySalesMetric.class))).thenAnswer(invocation -> invocation.getArgument(0));

        analyticsService.processPaymentCompleted(1L, BigDecimal.valueOf(59.00), BigDecimal.valueOf(9.00));

        assertEquals(new BigDecimal("159.00"), metric.getTotalRevenue());
        assertEquals(new BigDecimal("13.50"), metric.getTotalCgst()); // 9.00 + 4.50
        assertEquals(new BigDecimal("13.50"), metric.getTotalSgst()); // 9.00 + 4.50
        assertEquals(6L, metric.getTransactionCount());
        verify(metricRepository, times(1)).save(metric);
    }

    @Test
    public void testProcessPaymentCompleted_NewMetric() {
        when(metricRepository.findByMetricDate(LocalDate.now())).thenReturn(Optional.empty());
        when(metricRepository.save(any(DailySalesMetric.class))).thenAnswer(invocation -> invocation.getArgument(0));

        analyticsService.processPaymentCompleted(1L, BigDecimal.valueOf(59.00), BigDecimal.valueOf(9.00));

        verify(metricRepository, times(1)).save(any(DailySalesMetric.class));
    }

    @Test
    public void testProcessPaymentCompleted_WithZeroGst() {
        when(metricRepository.findByMetricDate(LocalDate.now())).thenReturn(Optional.of(metric));
        when(metricRepository.save(any(DailySalesMetric.class))).thenAnswer(invocation -> invocation.getArgument(0));

        analyticsService.processPaymentCompleted(1L, BigDecimal.valueOf(50.00), BigDecimal.ZERO);

        // Revenue should still increase by 50, but GST remains unchanged
        assertEquals(new BigDecimal("150.00"), metric.getTotalRevenue());
        assertEquals(new BigDecimal("9.00"), metric.getTotalCgst());
        assertEquals(6L, metric.getTransactionCount());
        verify(metricRepository, times(1)).save(metric);
    }

    @Test
    public void testGetDashboardMetrics_Success() {
        when(metricRepository.findByMetricDate(LocalDate.now())).thenReturn(Optional.of(metric));

        DashboardResponse response = analyticsService.getDashboardMetrics();

        assertNotNull(response);
        assertEquals(metric.getTotalRevenue(), response.totalRevenue());
        assertEquals(metric.getTransactionCount(), response.transactionCount());
    }

    @Test
    public void testGetDashboardMetrics_EmptyDefaults() {
        when(metricRepository.findByMetricDate(LocalDate.now())).thenReturn(Optional.empty());

        DashboardResponse response = analyticsService.getDashboardMetrics();

        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.totalRevenue());
        assertEquals(0L, response.transactionCount());
    }
}
