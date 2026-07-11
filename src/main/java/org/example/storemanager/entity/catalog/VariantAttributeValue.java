package org.example.storemanager.entity.catalog;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

@Entity
@Table(name = "variant_attribute_values", uniqueConstraints = {
        @UniqueConstraint(name = "uq_variant_attr", columnNames = {"product_variant_id", "product_attribute_id"}),
        @UniqueConstraint(name = "uq_variant_val", columnNames = {"product_variant_id", "attribute_value_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class VariantAttributeValue extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_attribute_id", nullable = false)
    private ProductAttribute productAttribute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_value_id", nullable = false)
    private AttributeValue attributeValue;
}
