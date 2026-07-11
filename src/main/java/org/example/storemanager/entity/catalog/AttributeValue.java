package org.example.storemanager.entity.catalog;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

@Entity
@Table(name = "attribute_values", indexes = {
        @Index(name = "idx_attr_values_attr_id", columnList = "attribute_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class AttributeValue extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private ProductAttribute productAttribute;

    @Column(name = "value_text", nullable = false, length = 150)
    private String value;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;
}
