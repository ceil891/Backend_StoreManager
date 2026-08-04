package org.example.storemanager.modules.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.partnerarea.entity.Customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sale_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SaleOrder extends BaseEntity {

    @Column(name = "order_code", nullable = false, unique = true, length = 50)
    private String orderCode;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "expected_delivery")
    private LocalDateTime expectedDelivery;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "final_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal finalAmount;

    @Column(nullable = false, length = 30)
    private String status; // PENDING, CONFIRMED, DELIVERING, COMPLETED, CANCELLED

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "customer_phone", length = 30)
    private String customerPhone;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Builder.Default
    @Column(name = "payment_status", length = 50)
    private String paymentStatus = "UNPAID";

    @Builder.Default
    @Column(name = "order_origin", length = 50)
    private String orderOrigin = "ONLINE";

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "carrier_id")
    private Long carrierId;

    @Column(name = "carrier", length = 100)
    private String carrier;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "tracking_code", length = 100)
    private String trackingCode;

    @Column(name = "tracking_url", length = 500)
    private String trackingUrl;

    @Column(name = "shipper_name", length = 150)
    private String shipperName;

    @Column(name = "shipper_phone", length = 30)
    private String shipperPhone;

    @Builder.Default
    @Column(name = "delivery_status", length = 50)
    private String deliveryStatus = "UNASSIGNED";

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "assigned_by", length = 100)
    private String assignedBy;

    @PrePersist
    public void prePersist() {
        if (this.paymentStatus == null || this.paymentStatus.trim().isEmpty()) {
            this.paymentStatus = "UNPAID";
        }
        if (this.deliveryStatus == null || this.deliveryStatus.trim().isEmpty()) {
            this.deliveryStatus = "UNASSIGNED";
        }
        if (this.orderOrigin == null || this.orderOrigin.trim().isEmpty() || "ONLINE_STORE".equalsIgnoreCase(this.orderOrigin)) {
            this.orderOrigin = "ONLINE";
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.getIsDeleted() == null) {
            this.setIsDeleted(false);
        }
        if (this.getIsLocked() == null) {
            this.setIsLocked(false);
        }
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = true)
    private Customer customer;

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = true)
    private Branch branch;
}