package org.example.storemanager.modules.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.catalog.entity.ProductVariant;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.shared.enums.inventory.InventoryTransactionType;
import org.example.storemanager.shared.enums.inventory.ReferenceType;

import java.math.BigDecimal;

@Entity
@Table(name = "inventory_transactions", indexes = {
        @Index(name = "idx_inv_tx_variant", columnList = "product_variant_id"),
        @Index(name = "idx_inv_tx_created_at", columnList = "created_at"),
        @Index(name = "idx_inv_tx_transaction_type", columnList = "transaction_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class InventoryTransaction extends BaseEntity {

    @Column(name = "transaction_code", nullable = false, length = 50, unique = true)
    private String transactionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    /** Chi nhánh nguồn (xuất kho / bán hàng / chuyển kho đi) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_branch_id")
    private Branch sourceBranch;

    /** Chi nhánh đích (nhập kho / nhận chuyển kho) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_branch_id")
    private Branch destinationBranch;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private InventoryTransactionType transactionType;

    /** Luôn dương — chiều biến động do transactionType quyết định */
    @Column(name = "quantity", precision = 18, scale = 3, nullable = false)
    private BigDecimal quantity;

    @Column(name = "before_quantity", precision = 18, scale = 3, nullable = false)
    private BigDecimal beforeQuantity;

    @Column(name = "after_quantity", precision = 18, scale = 3, nullable = false)
    private BigDecimal afterQuantity;

    /** Giá vốn tại thời điểm giao dịch — phục vụ tính FIFO/LIFO/bình quân */
    @Column(name = "unit_cost", precision = 18, scale = 2)
    private BigDecimal unitCost;

    /** Giá trị tiền = quantity * unitCost (hoặc unitPrice khi bán) */
    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 30)
    private ReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;
}
