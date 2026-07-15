package org.example.storemanager.entity.sales;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_order_details", indexes = {
        @Index(name = "idx_order_detail_order", columnList = "sale_order_id"),
        @Index(name = "idx_order_detail_variant", columnList = "product_variant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_order_id", referencedColumnName = "id", nullable = false)
    private SaleOrder saleOrder;

    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;
}