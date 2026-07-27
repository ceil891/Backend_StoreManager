package org.example.storemanager.modules.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.partnerarea.entity.Supplier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "import_receipts", indexes = {
        @Index(name = "idx_import_receipt_branch", columnList = "branch_id"),
        @Index(name = "idx_import_receipt_supplier", columnList = "supplier_id"),
        @Index(name = "idx_import_receipt_po", columnList = "purchase_order_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ImportReceipt extends BaseEntity {

    @Column(name = "receipt_code", nullable = false, unique = true, length = 50)
    private String receiptCode;

    @Column(name = "receipt_date", nullable = false)
    private LocalDateTime receiptDate;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal discount;

    @Column(precision = 18, scale = 2)
    private BigDecimal tax;

    @Column(nullable = false, length = 30)
    private String status; // PENDING, COMPLETED, CANCELLED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private org.example.storemanager.modules.sales.entity.PurchaseOrder purchaseOrder;

    @Column(name = "inspected_by", length = 100)
    private String inspectedBy;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}