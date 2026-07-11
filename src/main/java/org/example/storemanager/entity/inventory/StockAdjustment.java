package org.example.storemanager.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.system.Branch;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_adjustments", indexes = {
        @Index(name = "idx_stock_adj_code", columnList = "adjustment_code", unique = true),
        @Index(name = "idx_stock_adj_branch", columnList = "branch_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class StockAdjustment extends BaseEntity {

    @Column(name = "adjustment_code", nullable = false, unique = true, length = 50)
    private String adjustmentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(columnDefinition = "TEXT")
    private String reason;

    /** DRAFT → COMPLETED / CANCELLED */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}
