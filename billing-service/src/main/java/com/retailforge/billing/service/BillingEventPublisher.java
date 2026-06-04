package com.retailforge.billing.service;

import com.retailforge.event.OrderCreatedEvent;
import com.retailforge.event.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BillingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BillingEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BillingEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("Publishing order-created event: {}", event);
        kafkaTemplate.send("order-created", event.orderId().toString(), event);
    }

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Publishing payment-completed event: {}", event);
        kafkaTemplate.send("payment-completed", event.orderId().toString(), event);
    }
}
