package org.example.storemanager.entity.wms;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "warehouse_bins")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class WarehouseBin extends BaseEntity {

    @Column(name = "bin_code", nullable = false, unique = true, length = 50)
    private String binCode;

    @Column(length = 50)
    private String barcode; // Mã vạch dán trên kệ để dùng máy quét

    @Column(name = "max_capacity", precision = 18, scale = 2)
    private BigDecimal maxCapacity; // Sức chứa tối đa (theo Kg hoặc Thể tích)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private WarehouseZone zone; // Ngăn chứa này nằm ở Khu vực nào
}