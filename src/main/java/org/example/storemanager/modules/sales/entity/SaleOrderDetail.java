package org.example.storemanager.modules.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.catalog.entity.ProductVariant;

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
    @JoinColumn(name = "order_id", nullable = true)
    private SaleOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = true)
    private ProductVariant productVariant;

    // ---- Snapshot tại thời điểm bán ----
    @Column(name = "product_name_snapshot", nullable = true, length = 200)
    private String productNameSnapshot;

    @Column(name = "sku_snapshot", nullable = true, length = 100)
    private String skuSnapshot;

    @Column(name = "barcode_snapshot", length = 100)
    private String barcodeSnapshot;

    /** Ví dụ: "Size: M, Màu: Đen" */
    @Column(name = "variant_description_snapshot", length = 300)
    private String variantDescriptionSnapshot;
    // ---- End snapshot ----

    @Column(precision = 18, scale = 3, nullable = true)
    private BigDecimal quantity;

    @Column(name = "unit_price", precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "unit_price_snapshot", precision = 18, scale = 2, nullable = true)
    private BigDecimal unitPriceSnapshot;

    @Builder.Default
    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "sub_total", precision = 18, scale = 2)
    private BigDecimal subTotal;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate; // Thuế suất snapshot tại thời điểm bán

    @PrePersist
    public void prePersist() {
        if (this.unitPrice == null && this.unitPriceSnapshot != null) {
            this.unitPrice = this.unitPriceSnapshot;
        }
        if (this.unitPriceSnapshot == null && this.unitPrice != null) {
            this.unitPriceSnapshot = this.unitPrice;
        }
        if (this.discountAmount == null) {
            this.discountAmount = BigDecimal.ZERO;
        }
        if (this.taxAmount == null) {
            this.taxAmount = BigDecimal.ZERO;
        }
        if (this.subTotal == null && this.quantity != null && this.unitPriceSnapshot != null) {
            this.subTotal = this.quantity.multiply(this.unitPriceSnapshot);
        }
        if (this.totalAmount == null) {
            this.totalAmount = this.subTotal != null ? this.subTotal : (this.quantity != null && this.unitPriceSnapshot != null ? this.quantity.multiply(this.unitPriceSnapshot) : BigDecimal.ZERO);
        }
        if (this.subTotal == null) {
            this.subTotal = this.totalAmount;
        }
        if (this.taxRate == null) {
            this.taxRate = BigDecimal.ZERO;
        }
    }
}