package org.example.storemanager.shared.event.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.shared.event.base.DomainEvent;
import org.example.storemanager.shared.event.publisher.DomainEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxMessageRepository outboxRepository;
    private final DomainEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void processPendingOutboxMessages() {
        List<OutboxMessage> pendingMessages = outboxRepository.findByStatusOrderByCreatedAtAsc(
                OutboxMessage.OutboxStatus.PENDING, PageRequest.of(0, 50)
        );

        if (pendingMessages.isEmpty()) {
            return;
        }

        log.info("Processing {} PENDING outbox messages", pendingMessages.size());

        for (OutboxMessage message : pendingMessages) {
            try {
                DomainEvent<?> domainEvent = objectMapper.readValue(message.getPayload(), DomainEvent.class);
                eventPublisher.publish(domainEvent);

                message.setStatus(OutboxMessage.OutboxStatus.PUBLISHED);
                message.setProcessedAt(LocalDateTime.now());
                outboxRepository.save(message);
            } catch (Exception e) {
                log.error("Error publishing outbox message eventId={}", message.getEventId(), e);
                message.setRetryCount(message.getRetryCount() + 1);
                if (message.getRetryCount() >= 5) {
                    message.setStatus(OutboxMessage.OutboxStatus.FAILED);
                }
                outboxRepository.save(message);
            }
        }
    }
}
