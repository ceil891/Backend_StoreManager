package org.example.storemanager.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.system.Branch;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transfers", indexes = {
        @Index(name = "idx_stock_transfer_from_branch", columnList = "from_branch_id"),
        @Index(name = "idx_stock_transfer_to_branch", columnList = "to_branch_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class StockTransfer extends BaseEntity {

    @Column(name = "transfer_code", nullable = false, unique = true, length = 50)
    private String transferCode;

    @Column(name = "transfer_date", nullable = false)
    private LocalDateTime transferDate;

    @Column(nullable = false, length = 30)
    private String status; // DRAFT, IN_TRANSIT, COMPLETED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_branch_id", nullable = false)
    private Branch fromBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_branch_id", nullable = false)
    private Branch toBranch;

    @Column(name = "logistics_partner", length = 100)
    private String logisticsPartner;

    @Column(name = "tracking_ref", length = 100)
    private String trackingRef;

    @Column(name = "requested_by", length = 100)
    private String requestedBy;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "est_arrival_date")
    private LocalDateTime estArrivalDate;
}