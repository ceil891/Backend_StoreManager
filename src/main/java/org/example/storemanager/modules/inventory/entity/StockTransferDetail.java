package org.example.storemanager.modules.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.catalog.entity.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_transfer_details", indexes = {
        @Index(name = "idx_stock_td_transfer", columnList = "transfer_id"),
        @Index(name = "idx_stock_td_product", columnList = "product_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class StockTransferDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false)
    private StockTransfer transfer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity_shipped", precision = 18, scale = 3, nullable = false)
    private BigDecimal quantityShipped; // SL xuất đi

    @Column(name = "quantity_received", precision = 18, scale = 3)
    private BigDecimal quantityReceived; // SL thực nhận
}