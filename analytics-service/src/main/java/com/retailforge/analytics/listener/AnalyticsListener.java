package com.retailforge.analytics.listener;

import com.retailforge.analytics.event.PaymentCompletedEvent;
import com.retailforge.analytics.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsListener {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsListener.class);

    private final AnalyticsService analyticsService;

    public AnalyticsListener(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @KafkaListener(topics = "payment-completed", groupId = "analytics-service-group")
    @CacheEvict(value = "analytics", allEntries = true)
    public void consumePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Received payment-completed event for order: {}. Evicting dashboard cache.", event.orderNumber());
        try {
            analyticsService.processPaymentCompleted(event.orderId(), event.totalAmount(), event.totalGstAmount());
        } catch (Exception e) {
            log.error("Failed to process payment telemetry for order ID: {}", event.orderId(), e);
        }
    }
}
