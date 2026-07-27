package org.example.storemanager.shared.event.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {
    List<OutboxMessage> findByStatusOrderByCreatedAtAsc(OutboxMessage.OutboxStatus status, Pageable pageable);
}
