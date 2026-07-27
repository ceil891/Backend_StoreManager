package org.example.storemanager.modules.wms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.sales.entity.SaleOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "packing_lists")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PackingList extends BaseEntity {

    @Column(name = "pack_code", nullable = false, unique = true, length = 50)
    private String packCode;

    @Column(name = "pack_date", nullable = false)
    private LocalDateTime packDate;

    @Column(precision = 18, scale = 2)
    private BigDecimal weight; // Khối lượng kiện hàng

    @Column(length = 100)
    private String dimensions; // Kích thước (VD: 30x20x15 cm)

    @Column(nullable = false, length = 30)
    private String status; // DRAFT, PICKING, PICKED, PACKING, PACKED, CANCELLED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private SaleOrder order;

    @Column(name = "picking_started_by", length = 100)
    private String pickingStartedBy;

    @Column(name = "picking_started_at")
    private LocalDateTime pickingStartedAt;

    @Column(name = "picked_by", length = 100)
    private String pickedBy;

    @Column(name = "picked_at")
    private LocalDateTime pickedAt;

    @Column(name = "packing_started_by", length = 100)
    private String packingStartedBy;

    @Column(name = "packing_started_at")
    private LocalDateTime packingStartedAt;

    @Column(name = "packed_by", length = 100)
    private String packedBy;

    @Column(name = "packed_at")
    private LocalDateTime packedAt;
}