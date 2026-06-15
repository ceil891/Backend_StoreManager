package org.example.storemanager.entity.finance;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.system.Branch;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "operating_costs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class OperatingCost extends BaseEntity {

    @Column(name = "cost_date", nullable = false)
    private LocalDate costDate;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 30)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "cost_center_id")
    private Long costCenterId; // Tham chiếu đến Trung tâm chi phí ở phân hệ Kế toán chuyên sâu
}