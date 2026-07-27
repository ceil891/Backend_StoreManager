package org.example.storemanager.modules.advancedaccounting.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "accounting_periods")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class AccountingPeriod extends BaseEntity {

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Builder.Default
    @Column(name = "is_closed", columnDefinition = "boolean default false")
    private Boolean isClosed = false;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;
}
