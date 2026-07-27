package org.example.storemanager.modules.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.shared.enums.catalog.ComboType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Column(length = 50)
    private String barcode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "combo_type", nullable = false, length = 30)
    @Builder.Default
    private ComboType comboType = ComboType.DYNAMIC_VIRTUAL;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;
}