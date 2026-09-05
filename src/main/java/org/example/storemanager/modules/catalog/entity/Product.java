package org.example.storemanager.modules.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.shared.enums.catalog.VariantStrategy;
import java.math.BigDecimal;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_products_product_code", columnList = "product_code", unique = true),
    @Index(name = "idx_products_barcode", columnList = "barcode"),
    @Index(name = "idx_products_category_id", columnList = "category_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseEntity {

    @Column(name = "product_code", nullable = false, unique = true, length = 50)
    private String productCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal basePrice;

    @Column(name = "cost_price", precision = 18, scale = 2)
    private BigDecimal costPrice;

    @Column(length = 100)
    private String brand;

    @Column(name = "main_image_url", length = 2000)
    private String mainImageUrl;

    @Column(length = 50)
    private String barcode;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @Column(name = "weight", precision = 12, scale = 4)
    private BigDecimal weight;

    @Column(name = "reorder_point", precision = 12, scale = 4)
    private BigDecimal reorderPoint;

    @Column(name = "min_stock", precision = 12, scale = 4)
    private BigDecimal minStock;

    @Column(name = "max_stock", precision = 12, scale = 4)
    private BigDecimal maxStock;

    @Column(name = "gallery_images", columnDefinition = "TEXT")
    private String galleryImages;

    @Column(name = "variants", columnDefinition = "TEXT")
    private String variants;

    @Enumerated(EnumType.STRING)
    @Column(name = "variant_strategy", length = 30, columnDefinition = "varchar(30) default 'NONE'")
    @Builder.Default
    private VariantStrategy variantStrategy = VariantStrategy.NONE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_unit_id")
    private Unit baseUnit;

    @Column(name = "warranty_period_months")
    private Integer warrantyPeriodMonths;

    @Column(name = "origin_country", length = 100)
    private String originCountry;

    @Column(name = "is_serial_tracked", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isSerialTracked = false;

    @Column(name = "dimensions", length = 100)
    private String dimensions;

    @Column(name = "allow_negative_stock", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean allowNegativeStock = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_class", length = 20)
    private org.example.storemanager.shared.enums.catalog.TaxClass taxClass;

    public org.example.storemanager.shared.enums.catalog.TaxClass getEffectiveTaxClass() {
        if (this.taxClass != null) {
            return this.taxClass;
        }
        if (this.category != null && this.category.getTaxClass() != null) {
            return this.category.getTaxClass();
        }
        return org.example.storemanager.shared.enums.catalog.TaxClass.VAT_8;
    }

    public BigDecimal getEffectiveVatRate() {
        return getEffectiveTaxClass().getRate();
    }
}