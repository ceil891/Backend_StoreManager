package org.example.storemanager.modules.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.partnerarea.entity.Supplier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrder extends BaseEntity {

    @Column(name = "po_code", nullable = false, unique = true, length = 50)
    private String poCode;

    @Column(name = "po_date", nullable = false)
    private LocalDateTime poDate;

    @Column(name = "expected_date")
    private LocalDateTime expectedDate;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 30)
    private String status; // PENDING, APPROVED, RECEIVING, COMPLETED, CANCELLED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_request_id")
    private org.example.storemanager.modules.wms.entity.PurchaseRequest purchaseRequest;
}