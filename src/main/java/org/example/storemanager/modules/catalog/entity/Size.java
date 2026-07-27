package org.example.storemanager.modules.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

@Entity(name = "CatalogSize")
@Table(name = "sizes", indexes = {
        @Index(name = "idx_sizes_size_code", columnList = "size_code", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Size extends BaseEntity {

    @Column(name = "size_code", nullable = false, unique = true, length = 50)
    private String sizeCode;

    @Column(name = "size_name", nullable = false, length = 150)
    private String sizeName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;
}
