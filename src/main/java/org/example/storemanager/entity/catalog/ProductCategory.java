package org.example.storemanager.entity.catalog;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

@Entity
@Table(name = "product_categories", indexes = {
    @Index(name = "idx_categories_category_code", columnList = "category_code", unique = true),
    @Index(name = "idx_categories_parent_id", columnList = "parent_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ProductCategory extends BaseEntity {

    @Column(name = "category_code", nullable = false, unique = true, length = 50)
    private String categoryCode;

    @Column(name = "category_name", nullable = false, length = 150)
    private String categoryName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "manager", length = 100)
    private String manager;

    @Column(name = "inventory_gl_code", length = 50)
    private String inventoryGlCode;

    @Column(name = "cogs_gl_code", length = 50)
    private String cogsGlCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_class", length = 20)
    private org.example.storemanager.enums.catalog.TaxClass taxClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ProductCategory parent;
}