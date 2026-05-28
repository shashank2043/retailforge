package com.retailforge.notification.listener;

import com.retailforge.notification.event.LowStockAlertEvent;
import com.retailforge.notification.event.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final JavaMailSender mailSender;

    @Value("${retailforge.business.name:RetailForge}")
    private String businessName;

    @Value("${retailforge.business.address:1-32, Gachibowli, Hyderabad, 500032}")
    private String businessAddress;

    public NotificationListener(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

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
        log.info("Customer Email: {}", event.customerEmail());
        log.info("============================");

        try {
            sendEmailReceipt(event);
            log.info("Digital receipt email sent to {} successfully.", event.customerEmail());
        } catch (Exception e) {
            log.error("Failed to send digital receipt email to {}", event.customerEmail(), e);
        }
    }

    private void sendEmailReceipt(PaymentCompletedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.customerEmail());
        message.setSubject("Your Digital Receipt from " + businessName + " - Order #" + event.orderNumber());
        
        String body = String.format(
            "Dear Customer,\n\n" +
            "Thank you for shopping at %s!\n\n" +
            "Here is your digital cash receipt:\n" +
            "------------------------------------\n" +
            "Order Number: %s\n" +
            "Transaction ID: %s\n" +
            "Amount Paid: INR %s\n" +
            "Completed At: %s\n" +
            "Store Location: %s\n" +
            "------------------------------------\n\n" +
            "We hope to see you again soon!\n\n" +
            "Best Regards,\n" +
            "The %s Team",
            businessName,
            event.orderNumber(),
            event.transactionId(),
            event.totalAmount(),
            event.completedAt(),
            businessAddress,
            businessName
        );
        message.setText(body);
        mailSender.send(message);
    }
}
