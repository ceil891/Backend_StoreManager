package org.example.storemanager.shared.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.shared.config.KafkaConfig;
import org.example.storemanager.shared.event.base.DomainEvent;
import org.example.storemanager.shared.event.idempotency.IdempotencyService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class OrderEventConsumer {

    private final IdempotencyService idempotencyService;
    private static final String CONSUMER_GROUP = "storemanager-order-group";

    @KafkaListener(topics = KafkaConfig.TOPIC_ORDER_CREATED, groupId = CONSUMER_GROUP)
    public void consumeOrderCreated(DomainEvent<?> event) {
        // Idempotency Check: Prevents duplicate execution
        if (idempotencyService.isAlreadyProcessed(event.getEventId(), CONSUMER_GROUP)) {
            log.warn("EventId={} has ALREADY been processed. Skipping to avoid duplicate action!", event.getEventId());
            return;
        }

        log.info("Received DomainEvent [Type: {}, EventId: {}] payload: {}", 
                event.getEventType(), event.getEventId(), event.getPayload());
        
        // Microservices Async Business Workflow:
        // 1. Inventory Service -> Trừ tồn kho ngầm
        // 2. Customer Service -> Tích điểm thành viên
        // 3. AI Agent Service -> Phân tích hành vi & Gửi Zalo ZNS / Push Noti

        // Mark event as processed transactionally
        idempotencyService.markAsProcessed(event.getEventId(), CONSUMER_GROUP);
    }
}
