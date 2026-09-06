package org.example.storemanager.modules.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_shipments", indexes = {
        @Index(name = "idx_ts_tracking_code", columnList = "tracking_code"),
        @Index(name = "idx_ts_transfer_id", columnList = "transfer_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class TransferShipment extends BaseEntity {

    @Column(name = "tracking_code", nullable = false, unique = true, length = 50)
    private String trackingCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false)
    private StockTransfer transfer;

    @Column(name = "carrier_name", length = 150)
    private String carrierName;

    @Column(name = "carrier_type", length = 20)
    private String carrierType; // INTERNAL, EXTERNAL

    @Column(name = "status", nullable = false, length = 30)
    private String status; // IN_TRANSIT, DELIVERED, CANCELLED

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;
}
