package org.example.storemanager.modules.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

@Entity
@Table(name = "units", indexes = {
    @Index(name = "idx_units_unit_code", columnList = "unit_code", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Unit extends BaseEntity {

    @Column(name = "unit_code", nullable = false, unique = true, length = 50)
    private String unitCode;

    @Column(name = "unit_name", nullable = false, length = 50)
    private String unitName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @Column(name = "unit_type", length = 50)
    private String unitType; // WEIGHT, DIMENSION, QUANTITY, VOLUME, PACKAGING

    @Column(name = "conversion_factor", precision = 12, scale = 4)
    private java.math.BigDecimal conversionFactor;

    @Column(name = "base_unit_code", length = 50)
    private String baseUnitCode;

    @Column(name = "precision_decimals", columnDefinition = "int default 0")
    private Integer precisionDecimals = 0;
}