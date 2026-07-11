package org.example.storemanager.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.catalog.ProductVariant;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_adjustment_details", indexes = {
        @Index(name = "idx_stock_adj_detail_adj", columnList = "adjustment_id"),
        @Index(name = "idx_stock_adj_detail_variant", columnList = "product_variant_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class StockAdjustmentDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adjustment_id", nullable = false)
    private StockAdjustment stockAdjustment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    /** Tồn kho hệ thống (lý thuyết) */
    @Column(name = "expected_qty", precision = 18, scale = 3, nullable = false)
    private BigDecimal expectedQty;

    /** Tồn kho thực tế đếm được */
    @Column(name = "actual_qty", precision = 18, scale = 3, nullable = false)
    private BigDecimal actualQty;

    /** Chênh lệch = actualQty - expectedQty (âm = thiếu, dương = thừa) */
    @Column(name = "difference_qty", precision = 18, scale = 3)
    private BigDecimal differenceQty;
}
