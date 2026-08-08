package org.example.storemanager.modules.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_out_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class StockOutDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_out_id", nullable = false)
    private StockOut stockOut;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "variant", length = 150)
    private String variant;

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount;
}
