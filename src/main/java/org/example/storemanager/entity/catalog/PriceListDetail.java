package org.example.storemanager.entity.catalog;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import java.math.BigDecimal;

@Entity
@Table(name = "price_list_details", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pricelist_product_unit", columnNames = {"price_list_id", "product_id", "product_unit_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PriceListDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_list_id", nullable = false)
    private PriceList priceList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Giá theo đơn vị quy đổi (lon, thùng...). Tạm nullable=true cho migration; sau backfill đổi NOT NULL qua migration. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_unit_id", nullable = true)
    private ProductUnit productUnit;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal price;
}