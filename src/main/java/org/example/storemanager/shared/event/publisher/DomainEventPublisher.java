package org.example.storemanager.shared.event.publisher;

import org.example.storemanager.shared.event.base.DomainEvent;

public interface DomainEventPublisher {
    void publish(DomainEvent<?> event);
}
