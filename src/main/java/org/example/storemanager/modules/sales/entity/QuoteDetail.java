package org.example.storemanager.modules.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.catalog.entity.ProductVariant;
import org.example.storemanager.modules.catalog.entity.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "quote_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class QuoteDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(precision = 18, scale = 3, nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "discount_type", length = 20)
    private String discountType; // PERCENT, AMOUNT

    @Column(name = "discount_value", precision = 18, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "sub_total", precision = 18, scale = 2)
    private BigDecimal subTotal;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;
}