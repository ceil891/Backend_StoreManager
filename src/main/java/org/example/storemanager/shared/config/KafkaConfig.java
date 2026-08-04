package org.example.storemanager.shared.config;

import org.springframework.context.annotation.Configuration;

/**
 * KafkaConfig - Topic constants only.
 * Topic auto-creation disabled (spring.kafka.admin.auto-create=false)
 * to prevent AdminClient connection attempts when Kafka broker is not running locally.
 * Kafka listeners are also disabled via spring.kafka.listener.auto-startup=false.
 */
@Configuration
public class KafkaConfig {

    public static final String TOPIC_ORDER_CREATED = "storemanager.order.created";
    public static final String TOPIC_STOCK_UPDATED = "storemanager.stock.updated";
    public static final String TOPIC_MARKETING_TRIGGER = "storemanager.marketing.trigger";

    // NewTopic beans removed — topics are created manually on Kafka server when needed.
    // When Kafka is available, set SPRING_KAFKA_BOOTSTRAP_SERVERS env var
    // and SPRING_KAFKA_AUTO_STARTUP=true to enable consumers.
}
