package org.example.storemanager.modules.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

@Entity
@Table(name = "product_attributes", indexes = {
        @Index(name = "idx_attributes_code", columnList = "attribute_code", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ProductAttribute extends BaseEntity {

    @Column(name = "attribute_name", nullable = false, length = 100)
    private String attributeName;

    @Column(name = "attribute_code", nullable = false, unique = true, length = 50)
    private String attributeCode;

    @Column(name = "attribute_type", length = 50)
    private String attributeType;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;
}
