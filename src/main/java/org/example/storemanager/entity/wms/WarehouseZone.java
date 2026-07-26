package org.example.storemanager.entity.wms;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.system.Branch;

import java.math.BigDecimal;

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

    /** Sức chứa tổng của Zone (đơn vị tùy quy ước: m², pallet...) */
    @Column(precision = 18, scale = 2)
    private BigDecimal capacity;

    /**
     * Trạng thái: ACTIVE (hoạt động) / INACTIVE (tạm ngưng)
     * Khi INACTIVE, không thể chọn làm đích trong Putaway/Import Receipt.
     */
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch; // Khu vực này thuộc Kho/Chi nhánh nào
}