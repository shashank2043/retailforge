package com.retailforge.notification.listener;

import com.retailforge.notification.event.LowStockAlertEvent;
import com.retailforge.notification.event.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    @Value("${retailforge.business.name:RetailForge}")
    private String businessName;

    @Value("${retailforge.business.address:1-32, Gachibowli, Hyderabad, 500032}")
    private String businessAddress;

    @KafkaListener(topics = "stock-low-alert", groupId = "notification-service-group")
    public void consumeLowStockAlert(LowStockAlertEvent event) {
        log.warn("=== LOW STOCK WARNING ===");
        log.warn("Business: {}", businessName);
        log.warn("Location: {}", businessAddress);
        log.warn("Product ID {} has crossed the threshold!", event.productId());
        log.warn("Current Warehouse ID: {}", event.warehouseId());
        log.warn("Current Quantity: {} (Threshold: {})", event.currentQuantity(), event.threshold());
        log.warn("Alert Timestamp: {}", event.alertedAt());
        log.warn("=========================");
    }

    @KafkaListener(topics = "payment-completed", groupId = "notification-service-group")
    public void consumePaymentCompleted(PaymentCompletedEvent event) {
        log.info("=== DIGITAL CASH RECEIPT ===");
        log.info("Store: {}", businessName);
        log.info("Address: {}", businessAddress);
        log.info("Order Number: {}", event.orderNumber());
        log.info("Transaction ID: {}", event.transactionId());
        log.info("Amount Paid: INR {}", event.totalAmount());
        log.info("Completed At: {}", event.completedAt());
        log.info("Simulated digital receipt sent to customer successfully.");
        log.info("============================");
    }
}
