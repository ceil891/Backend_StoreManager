package org.example.storemanager.modules.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sale_return_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SaleReturnRequest extends BaseEntity {

    @Column(name = "request_code", nullable = false, length = 50)
    private String requestCode;

    @Column(name = "order_code", length = 50)
    private String orderCode;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_name", length = 150)
    private String customerName;

    @Column(name = "customer_phone", length = 30)
    private String customerPhone;

    @Column(name = "requested_qty")
    private Integer requestedQty;

    @Column(name = "returned_qty")
    private Integer returnedQty;

    @Column(name = "remaining_qty")
    private Integer remainingQty;

    @Column(name = "refund_amount", precision = 18, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_method", length = 50)
    private String refundMethod;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @Column(name = "status", length = 50)
    private String status; // PENDING, APPROVED, REJECTED, COMPLETED, CANCELLED

    @Column(name = "items_json", columnDefinition = "TEXT")
    private String itemsJson;
}
