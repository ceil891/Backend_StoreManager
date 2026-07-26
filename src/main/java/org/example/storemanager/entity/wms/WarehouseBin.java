package org.example.storemanager.entity.wms;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "warehouse_bins", indexes = {
        @Index(name = "idx_bin_rack_id", columnList = "rack_id"),
        @Index(name = "idx_bin_code", columnList = "bin_code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class WarehouseBin extends BaseEntity {

    @Column(name = "bin_code", nullable = false, unique = true, length = 50)
    private String binCode; // Ví dụ: B01, B02... Toạ độ đầy đủ: HN-A-A2-R05-B12

    @Column(length = 50)
    private String barcode; // Mã vạch dán trên ô kệ để dùng máy quét handheld

    /** Tải trọng tối đa (kg) — dùng cho Putaway Strategy tự động */
    @Column(name = "max_weight_kg", precision = 10, scale = 2)
    private BigDecimal maxWeightKg;

    /** Thể tích tối đa (m³) */
    @Column(name = "max_volume_m3", precision = 10, scale = 3)
    private BigDecimal maxVolumeM3;

    /** Số pallet tối đa */
    @Column(name = "max_pallet")
    private Integer maxPallet;

    /**
     * Trạng thái vật lý của ô kệ:
     * EMPTY (trống) / OCCUPIED (đang có hàng) / FULL (đầy)
     * Được cập nhật tự động khi ProductLocation thay đổi.
     */
    @Column(nullable = false, length = 20)
    private String status = "EMPTY";

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rack_id", nullable = false)
    private Rack rack; // Ô kệ này nằm trong Kệ (Rack) nào
}