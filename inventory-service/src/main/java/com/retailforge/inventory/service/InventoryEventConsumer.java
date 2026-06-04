package com.retailforge.inventory.service;

import com.retailforge.event.InventoryFailedEvent;
import com.retailforge.event.InventoryReservedEvent;
import com.retailforge.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final InventoryService inventoryService;
    private final InventoryEventPublisher eventPublisher;

    public InventoryEventConsumer(InventoryService inventoryService, InventoryEventPublisher eventPublisher) {
        this.inventoryService = inventoryService;
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(topics = "order-created", groupId = "inventory-service-group")
    public void consumeOrderCreated(OrderCreatedEvent event) {
        log.info("Received order-created event for order: {}", event.orderNumber());
        try {
            inventoryService.reserveStockForOrder(event);
            log.info("Successfully reserved stock for order: {}", event.orderNumber());
            
            // Publish success event
            InventoryReservedEvent successEvent = new InventoryReservedEvent(
                event.orderId(),
                event.orderNumber(),
                event.paymentMethod()
            );
            eventPublisher.publishInventoryReserved(successEvent);
        } catch (Exception e) {
            log.error("Failed to reserve stock for order: {}. Reason: {}", event.orderNumber(), e.getMessage());
            
            // Publish failure event
            InventoryFailedEvent failureEvent = new InventoryFailedEvent(
                event.orderId(),
                event.orderNumber(),
                e.getMessage()
            );
            eventPublisher.publishInventoryFailed(failureEvent);
        }
    }
}
