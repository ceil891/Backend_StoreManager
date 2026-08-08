package org.example.storemanager.modules.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.catalog.entity.ProductVariant;
import org.example.storemanager.modules.wms.entity.WarehouseZone;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_ledgers", indexes = {
        @Index(name = "idx_stock_ledger_product", columnList = "product_id"),
        @Index(name = "idx_stock_ledger_variant", columnList = "product_variant_id"),
        @Index(name = "idx_stock_ledger_branch", columnList = "branch_id"),
        @Index(name = "idx_stock_ledger_zone", columnList = "warehouse_zone_id"),
        @Index(name = "idx_stock_ledger_ref", columnList = "reference_id"),
        @Index(name = "idx_stock_ledger_batch", columnList = "batch_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class StockLedger extends BaseEntity {

    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType; // IMPORT, EXPORT, TRANSFER, CANCEL, ADJUSTMENT

    @Column(name = "reference_id")
    private Long referenceId; // ID của phiếu tương ứng (VD: import_receipt_id)

    @Column(name = "change_qty", precision = 18, scale = 3, nullable = false)
    private BigDecimal changeQty; // Số lượng thay đổi (âm hoặc dương)

    @Column(name = "balance_after", precision = 18, scale = 3, nullable = false)
    private BigDecimal balanceAfter; // Tồn kho sau giao dịch

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_zone_id")
    private WarehouseZone warehouseZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductBatch batch; // Tùy chọn lô hàng
}