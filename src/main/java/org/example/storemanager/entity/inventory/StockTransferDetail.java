package org.example.storemanager.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.catalog.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_transfer_details")
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