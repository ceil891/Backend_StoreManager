package org.example.storemanager.shared.event.idempotency;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "processed_events")
public class ProcessedEvent {

    @Id
    @Column(length = 100)
    private String eventId;

    private String consumerGroup;
    private LocalDateTime processedAt;
}
