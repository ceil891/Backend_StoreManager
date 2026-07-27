package org.example.storemanager.shared.event.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.shared.event.base.DomainEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxMessageRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public <T> void saveEventToOutbox(DomainEvent<T> event) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            OutboxMessage outboxMessage = OutboxMessage.builder()
                    .eventId(event.getEventId())
                    .aggregateType(event.getAggregateType())
                    .aggregateId(event.getAggregateId())
                    .eventType(event.getEventType())
                    .payload(jsonPayload)
                    .status(OutboxMessage.OutboxStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .retryCount(0)
                    .build();

            outboxRepository.save(outboxMessage);
            log.info("Saved EventId={} to Outbox table in database transaction", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to save EventId={} to Outbox table", event.getEventId(), e);
            throw new RuntimeException("Error saving outbox message", e);
        }
    }
}
