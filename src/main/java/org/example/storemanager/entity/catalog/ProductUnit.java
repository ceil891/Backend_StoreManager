package org.example.storemanager.entity.catalog;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import java.math.BigDecimal;

@Entity
@Table(name = "product_units")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ProductUnit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Column(name = "conversion_rate", precision = 12, scale = 4, nullable = false)
    private BigDecimal conversionRate; // Tỷ lệ quy đổi so với đơn vị gốc

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal price; // Giá bán riêng cho đơn vị quy đổi này

    @Column(length = 50)
    private String barcode;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @Column(name = "is_base_unit", columnDefinition = "boolean default false")
    private Boolean isBaseUnit = false;
}