package org.example.storemanager.entity.crm;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.catalog.Product;

@Entity
@Table(name = "promotion_products", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"promotion_id", "product_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PromotionProduct extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
