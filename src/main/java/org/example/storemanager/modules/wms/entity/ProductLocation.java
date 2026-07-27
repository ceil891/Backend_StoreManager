package org.example.storemanager.modules.wms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.catalog.entity.Product;
import java.math.BigDecimal;

@Entity
@Table(name = "product_locations", indexes = {
    @Index(name = "idx_prod_locations_product_id", columnList = "product_id"),
    @Index(name = "idx_prod_locations_bin_id", columnList = "bin_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ProductLocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bin_id", nullable = false)
    private WarehouseBin bin;

    @Column(precision = 18, scale = 4, nullable = false)
    private BigDecimal quantity;
}
