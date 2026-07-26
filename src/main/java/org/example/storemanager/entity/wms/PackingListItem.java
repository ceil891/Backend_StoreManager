package org.example.storemanager.entity.wms;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.catalog.ProductVariant;

import java.math.BigDecimal;

@Entity
@Table(name = "packing_list_items", indexes = {
        @Index(name = "idx_packing_item_list", columnList = "packing_list_id"),
        @Index(name = "idx_packing_item_variant", columnList = "product_variant_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PackingListItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packing_list_id", nullable = false)
    private PackingList packingList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(name = "quantity", precision = 18, scale = 3, nullable = false)
    private BigDecimal quantity;

    @Column(name = "picked_quantity", precision = 18, scale = 3)
    @Builder.Default
    private BigDecimal pickedQuantity = BigDecimal.ZERO;

    @Column(name = "packed_quantity", precision = 18, scale = 3)
    @Builder.Default
    private BigDecimal packedQuantity = BigDecimal.ZERO;
}
