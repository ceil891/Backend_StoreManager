package org.example.storemanager.modules.purchase.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_invoice_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PurchaseInvoiceItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_invoice_id", nullable = false)
    private PurchaseInvoice purchaseInvoice;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_variant_id")
    private Long productVariantId;

    @Column(name = "product_name", length = 255)
    private String productName;

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(name = "unit_name", length = 50)
    private String unitName;

    @Column(name = "quantity", precision = 18, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_price", precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "vat_rate", precision = 5, scale = 2)
    private BigDecimal vatRate;

    @Column(name = "vat_amount", precision = 18, scale = 2)
    private BigDecimal vatAmount;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;
}
