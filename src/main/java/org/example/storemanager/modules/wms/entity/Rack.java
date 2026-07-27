package org.example.storemanager.modules.wms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "racks", indexes = {
        @Index(name = "idx_rack_area_id", columnList = "area_id"),
        @Index(name = "idx_rack_code", columnList = "rack_code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Rack extends BaseEntity {

    @Column(name = "rack_code", nullable = false, unique = true, length = 50)
    private String rackCode; // Ví dụ: R01, R02...

    @Column(name = "rack_name", nullable = false, length = 150)
    private String rackName; // Ví dụ: Kệ 01

    /** Tải trọng tối đa (kg) — phục vụ Putaway Strategy */
    @Column(name = "max_weight_kg", precision = 10, scale = 2)
    private BigDecimal maxWeightKg;

    /** Thể tích tối đa (m³) */
    @Column(name = "max_volume_m3", precision = 10, scale = 3)
    private BigDecimal maxVolumeM3;

    /** Số pallet tối đa */
    @Column(name = "max_pallet")
    private Integer maxPallet;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area; // Kệ này nằm trong Bãi (Area) nào
}
