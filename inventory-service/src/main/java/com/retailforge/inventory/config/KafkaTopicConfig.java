package com.retailforge.inventory.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic stockUpdatedTopic() {
        return TopicBuilder.name("stock-updated")
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic stockLowAlertTopic() {
        return TopicBuilder.name("stock-low-alert")
            .partitions(3)
            .replicas(1)
            .build();
    }
}
