package org.example.storemanager.entity.wms;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

@Entity(name = "WmsArea")
@Table(name = "wms_areas", indexes = {
        @Index(name = "idx_area_zone_id", columnList = "zone_id"),
        @Index(name = "idx_area_code", columnList = "area_code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Area extends BaseEntity {

    @Column(name = "area_code", nullable = false, unique = true, length = 50)
    private String areaCode; // Ví dụ: A1, A2, B1...

    @Column(name = "area_name", nullable = false, length = 150)
    private String areaName; // Ví dụ: Bãi A1, Khu B

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private WarehouseZone zone; // Bãi này thuộc Khu vực (Zone) nào
}
