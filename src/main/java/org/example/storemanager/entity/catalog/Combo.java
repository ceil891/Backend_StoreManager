package org.example.storemanager.entity.catalog;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import java.math.BigDecimal;

@Entity
@Table(name = "combos", indexes = {
    @Index(name = "idx_combos_combo_code", columnList = "combo_code", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Combo extends BaseEntity {

    @Column(name = "combo_code", nullable = false, unique = true, length = 50)
    private String comboCode;

    @Column(name = "combo_name", nullable = false, length = 150)
    private String comboName;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal price; // Giá trọn gói của cả combo

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;
}