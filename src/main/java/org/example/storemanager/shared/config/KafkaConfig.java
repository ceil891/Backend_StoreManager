package org.example.storemanager.shared.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaConfig {

    public static final String TOPIC_ORDER_CREATED = "storemanager.order.created";
    public static final String TOPIC_STOCK_UPDATED = "storemanager.stock.updated";
    public static final String TOPIC_MARKETING_TRIGGER = "storemanager.marketing.trigger";

    // NewTopic beans removed — topics are created manually on Kafka server when needed.
    // When Kafka is available, set SPRING_KAFKA_BOOTSTRAP_SERVERS env var
    // and SPRING_KAFKA_AUTO_STARTUP=true to enable consumers.
}
