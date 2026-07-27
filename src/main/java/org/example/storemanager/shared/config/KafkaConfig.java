package org.example.storemanager.shared.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
public class KafkaConfig {

    public static final String TOPIC_ORDER_CREATED = "storemanager.order.created";
    public static final String TOPIC_STOCK_UPDATED = "storemanager.stock.updated";
    public static final String TOPIC_MARKETING_TRIGGER = "storemanager.marketing.trigger";

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(TOPIC_ORDER_CREATED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic stockUpdatedTopic() {
        return TopicBuilder.name(TOPIC_STOCK_UPDATED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic marketingTriggerTopic() {
        return TopicBuilder.name(TOPIC_MARKETING_TRIGGER)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
