package com.retailforge.inventory.service;

import com.retailforge.inventory.event.LowStockAlertEvent;
import com.retailforge.inventory.event.StockUpdatedEvent;
import com.retailforge.inventory.event.InventoryReservedEvent;
import com.retailforge.inventory.event.InventoryFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventoryEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishStockUpdated(StockUpdatedEvent event) {
        log.info("Publishing stock-updated event: {}", event);
        kafkaTemplate.send("stock-updated", event.productId().toString(), event);
    }

    public void publishLowStockAlert(LowStockAlertEvent event) {
        log.info("Publishing stock-low-alert event: {}", event);
        kafkaTemplate.send("stock-low-alert", event.productId().toString(), event);
    }

    public void publishInventoryReserved(InventoryReservedEvent event) {
        log.info("Publishing inventory-reserved event: {}", event);
        kafkaTemplate.send("inventory-reserved", event.orderId().toString(), event);
    }

    public void publishInventoryFailed(InventoryFailedEvent event) {
        log.info("Publishing inventory-failed event: {}", event);
        kafkaTemplate.send("inventory-failed", event.orderId().toString(), event);
    }
}
