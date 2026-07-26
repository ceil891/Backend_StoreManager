package org.example.storemanager.entity.sales;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.catalog.Product;
import org.example.storemanager.entity.inventory.ProductBatch;

import java.math.BigDecimal;

@Entity
@Table(name = "export_invoice_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ExportInvoiceDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private ExportInvoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductBatch batch; // Lô hàng xuất đi

    @Column(precision = 18, scale = 3, nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(precision = 18, scale = 2)
    private BigDecimal discount;

    @Column(name = "sub_total", precision = 18, scale = 2)
    private BigDecimal subTotal;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate; // Thuế suất snapshot tại thời điểm bán
}