package org.example.storemanager.modules.logistics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_assignment_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class DeliveryAssignmentHistory extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_code", length = 50)
    private String orderCode;

    @Column(name = "carrier_id")
    private Long carrierId;

    @Column(name = "carrier_name", length = 150)
    private String carrierName;

    @Column(name = "shipper_id")
    private Long shipperId;

    @Column(name = "shipper_name", length = 150)
    private String shipperName;

    @Column(name = "shipper_phone", length = 30)
    private String shipperPhone;

    @Column(name = "tracking_code", length = 100)
    private String trackingCode;

    @Column(name = "tracking_url", length = 500)
    private String trackingUrl;

    @Column(name = "delivery_status", length = 50)
    private String deliveryStatus; // ASSIGNED, REASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, FAILED, CANCELLED

    @Column(name = "action_type", length = 50)
    private String actionType; // ASSIGNED, REASSIGNED, CANCELLED

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "assigned_by", length = 100)
    private String assignedBy;

    @Column(name = "note", length = 500)
    private String note;
}
