package org.example.storemanager.shared.event.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEvent<T> {
    private String eventId;
    private String eventType;
    private int version;
    private LocalDateTime occurredAt;
    private String aggregateType;
    private String aggregateId;
    private T payload;

    public static <T> DomainEvent<T> create(String eventType, String aggregateType, String aggregateId, T payload) {
        return DomainEvent.<T>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .version(1)
                .occurredAt(LocalDateTime.now())
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .payload(payload)
                .build();
    }
}
