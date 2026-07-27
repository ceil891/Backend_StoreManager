package org.example.storemanager.shared.event.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
    boolean existsByEventIdAndConsumerGroup(String eventId, String consumerGroup);
}
