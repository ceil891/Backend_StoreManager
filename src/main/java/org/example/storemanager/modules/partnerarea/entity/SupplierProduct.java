package org.example.storemanager.modules.partnerarea.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.catalog.entity.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "supplier_products",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_supplier_product", columnNames = {"supplier_id", "product_id"})
        },
        indexes = {
                @Index(name = "idx_supplier_product_supplier", columnList = "supplier_id"),
                @Index(name = "idx_supplier_product_product", columnList = "product_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SupplierProduct extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Mã SKU của NCC (có thể khác SKU nội bộ) */
    @Column(name = "supplier_sku", length = 100)
    private String supplierSku;

    /** Giá mua từ NCC này */
    @Column(name = "unit_price", precision = 18, scale = 2)
    private BigDecimal unitPrice;

    /** Đơn vị tiền tệ: VND, USD... */
    @Column(length = 10)
    private String currency = "VND";

    /** Minimum Order Quantity — số lượng đặt hàng tối thiểu */
    @Column(precision = 18, scale = 3)
    private BigDecimal moq;

    /** Thời gian giao hàng (ngày) từ NCC này */
    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    /**
     * NCC ưu tiên (preferred supplier) cho sản phẩm này.
     * Hệ thống sẽ tự gợi ý NCC này khi tạo Purchase Order.
     */
    @Column(name = "is_preferred", columnDefinition = "boolean default false")
    private Boolean isPreferred = false;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;
}
