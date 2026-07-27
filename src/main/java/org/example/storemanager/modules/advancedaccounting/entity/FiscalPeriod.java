package org.example.storemanager.modules.advancedaccounting.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.time.LocalDate;

@Entity
@Table(name = "fiscal_periods")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class FiscalPeriod extends BaseEntity {

    @Column(name = "period_code", nullable = false, unique = true, length = 50)
    private String periodCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(length = 30)
    private String status; // e.g. ACTIVE, CLOSED
}
