package org.example.storemanager.entity.wms;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.catalog.ProductVariant;
import org.example.storemanager.entity.system.Branch;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "location_transfers", indexes = {
        @Index(name = "idx_loc_transfer_branch", columnList = "branch_id"),
        @Index(name = "idx_loc_transfer_from_bin", columnList = "from_bin_id"),
        @Index(name = "idx_loc_transfer_to_bin", columnList = "to_bin_id"),
        @Index(name = "idx_loc_transfer_variant", columnList = "product_variant_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class LocationTransfer extends BaseEntity {

    @Column(name = "transfer_code", nullable = false, unique = true, length = 50)
    private String transferCode; // Ví dụ: LT-20260712-001

    @Column(name = "transfer_date", nullable = false)
    private LocalDateTime transferDate;

    /**
     * Trạng thái: PENDING (chờ thực hiện) / COMPLETED (đã chuyển) / CANCELLED (hủy)
     * Không có trạng thái IN_TRANSIT vì chỉ là công nhân bốc hàng trong kho.
     */
    @Column(nullable = false, length = 20)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String reason; // Lý do chuyển: Tối ưu vị trí, Hết hạn ô kệ cũ...

    @Column(precision = 18, scale = 3, nullable = false)
    private BigDecimal quantity;

    /** Người thực hiện chuyển (snapshot tên) */
    @Column(name = "executed_by", length = 150)
    private String executedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_bin_id", nullable = false)
    private WarehouseBin fromBin; // Ô kệ nguồn

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_bin_id", nullable = false)
    private WarehouseBin toBin; // Ô kệ đích

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch; // Kho thực hiện chuyển (fromBin và toBin phải cùng branch)
}
