package org.example.storemanager.entity.wms;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.system.Branch;

@Entity
@Table(name = "warehouse_zones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class WarehouseZone extends BaseEntity {

    @Column(name = "zone_code", nullable = false, unique = true, length = 50)
    private String zoneCode;

    @Column(name = "zone_name", nullable = false, length = 150)
    private String zoneName;

    @Column(length = 255)
    private String conditions; // Điều kiện lưu trữ: Nhiệt độ, Độ ẩm...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch; // Khu vực này thuộc Kho/Chi nhánh nào
}