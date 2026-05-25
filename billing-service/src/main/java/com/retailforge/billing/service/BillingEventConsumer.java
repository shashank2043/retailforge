package com.retailforge.billing.service;

import com.retailforge.billing.event.InventoryFailedEvent;
import com.retailforge.billing.event.InventoryReservedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BillingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BillingEventConsumer.class);

    private final BillingService billingService;

    public BillingEventConsumer(BillingService billingService) {
        this.billingService = billingService;
    }

    @KafkaListener(topics = "inventory-reserved", groupId = "billing-group")
    public void consumeInventoryReserved(InventoryReservedEvent event) {
        log.info("Consumed inventory-reserved event for order: {}", event.orderNumber());
        try {
            billingService.completeOrder(event.orderId(), event.paymentMethod());
        } catch (Exception e) {
            log.error("Failed to complete order ID: {}", event.orderId(), e);
        }
    }

    @KafkaListener(topics = "inventory-failed", groupId = "billing-group")
    public void consumeInventoryFailed(InventoryFailedEvent event) {
        log.info("Consumed inventory-failed event for order: {}. Reason: {}", event.orderNumber(), event.reason());
        try {
            billingService.cancelOrder(event.orderId(), event.reason());
        } catch (Exception e) {
            log.error("Failed to cancel order ID: {}", event.orderId(), e);
        }
    }
}
