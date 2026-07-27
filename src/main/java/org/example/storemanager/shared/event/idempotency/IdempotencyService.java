package org.example.storemanager.shared.event.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final ProcessedEventRepository repository;

    public boolean isAlreadyProcessed(String eventId, String consumerGroup) {
        return repository.existsByEventIdAndConsumerGroup(eventId, consumerGroup);
    }

    @Transactional
    public void markAsProcessed(String eventId, String consumerGroup) {
        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .eventId(eventId)
                .consumerGroup(consumerGroup)
                .processedAt(LocalDateTime.now())
                .build();
        repository.save(processedEvent);
        log.info("Marked EventId={} as PROCESSED by consumerGroup={}", eventId, consumerGroup);
    }
}
