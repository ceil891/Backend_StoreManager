package org.example.storemanager.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.system.Branch;
import org.example.storemanager.entity.catalog.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_ledgers")
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
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductBatch batch; // Tùy chọn lô hàng
}