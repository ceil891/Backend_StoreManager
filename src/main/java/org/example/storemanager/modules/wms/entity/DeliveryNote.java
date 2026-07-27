package org.example.storemanager.modules.wms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class DeliveryNote extends BaseEntity {

    @Column(name = "note_code", nullable = false, unique = true, length = 50)
    private String noteCode;

    @Column(name = "delivery_date", nullable = false)
    private LocalDateTime deliveryDate;

    @Column(name = "recipient_name", length = 150)
    private String recipientName; // Tên người ký nhận hàng thực tế

    @Column(nullable = false, length = 30)
    private String status; // DRAFT, DISPATCHED, IN_TRANSIT, DELIVERED, FAILED, CANCELLED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packing_list_id", nullable = false)
    private PackingList packingList;

    @Column(name = "carrier_name", length = 150)
    private String carrierName;

    @Column(name = "tracking_number", length = 150)
    private String trackingNumber;

    @Column(name = "dispatched_by", length = 100)
    private String dispatchedBy;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "in_transit_by", length = 100)
    private String inTransitBy;

    @Column(name = "in_transit_at")
    private LocalDateTime inTransitAt;

    @Column(name = "delivered_by", length = 100)
    private String deliveredBy;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "failed_by", length = 100)
    private String failedBy;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason", length = 250)
    private String failureReason;

    @Column(name = "cancelled_by", length = 100)
    private String cancelledBy;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 250)
    private String cancelReason;
}