package org.example.storemanager.shared.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.shared.config.KafkaConfig;
import org.example.storemanager.shared.event.base.DomainEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(DomainEvent<?> event) {
        String topic = resolveTopic(event.getEventType());
        log.info("Publishing DomainEvent to Kafka [Topic: {}, EventId: {}, Type: {}]", 
                topic, event.getEventId(), event.getEventType());
        
        kafkaTemplate.send(topic, event.getAggregateId(), event);
    }

    private String resolveTopic(String eventType) {
        return switch (eventType) {
            case "ORDER_CREATED" -> KafkaConfig.TOPIC_ORDER_CREATED;
            case "STOCK_LOW" -> KafkaConfig.TOPIC_STOCK_UPDATED;
            default -> KafkaConfig.TOPIC_MARKETING_TRIGGER;
        };
    }
}
