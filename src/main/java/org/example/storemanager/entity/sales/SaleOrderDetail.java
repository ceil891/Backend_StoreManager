package org.example.storemanager.entity.sales;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.catalog.ProductVariant;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_order_details", indexes = {
        @Index(name = "idx_sale_od_order", columnList = "order_id"),
        @Index(name = "idx_sale_od_variant", columnList = "product_variant_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SaleOrderDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private SaleOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    // ---- Snapshot tại thời điểm bán ----
    @Column(name = "product_name_snapshot", nullable = false, length = 200)
    private String productNameSnapshot;

    @Column(name = "sku_snapshot", nullable = false, length = 100)
    private String skuSnapshot;

    @Column(name = "barcode_snapshot", length = 100)
    private String barcodeSnapshot;

    /** Ví dụ: "Size: M, Màu: Đen" */
    @Column(name = "variant_description_snapshot", length = 300)
    private String variantDescriptionSnapshot;
    // ---- End snapshot ----

    @Column(precision = 18, scale = 3, nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit_price_snapshot", precision = 18, scale = 2, nullable = false)
    private BigDecimal unitPriceSnapshot;

    @Column(name = "sub_total", precision = 18, scale = 2)
    private BigDecimal subTotal;
}