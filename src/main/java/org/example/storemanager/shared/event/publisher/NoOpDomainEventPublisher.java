package org.example.storemanager.shared.event.publisher;

import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.shared.event.base.DomainEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Fallback DomainEventPublisher when Kafka is disabled or unavailable on cloud containers (Render 512MB).
 * Prevents continuous reconnection loops and OutOfMemoryError.
 */
@Slf4j
@Component
@ConditionalOnMissingBean(KafkaDomainEventPublisher.class)
public class NoOpDomainEventPublisher implements DomainEventPublisher {

    @Override
    public void publish(DomainEvent<?> event) {
        log.debug("NoOpDomainEventPublisher: event {} of type {} skipped (Kafka disabled)", 
                event.getEventId(), event.getEventType());
    }
}
