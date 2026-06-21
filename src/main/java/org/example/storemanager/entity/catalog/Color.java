package org.example.storemanager.entity.catalog;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

@Entity(name = "CatalogColor")
@Table(name = "colors", indexes = {
        @Index(name = "idx_colors_color_code", columnList = "color_code", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Color extends BaseEntity {

    @Column(name = "color_code", nullable = false, unique = true, length = 50)
    private String colorCode;

    @Column(name = "color_name", nullable = false, length = 150)
    private String colorName;

    @Column(name = "hex_value", length = 20)
    private String hexValue;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;
}
