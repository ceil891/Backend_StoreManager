package org.example.storemanager.entity.catalog;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
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

    @Column(name = "main_image_url", length = 500)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_unit_id")
    private Unit baseUnit;
}