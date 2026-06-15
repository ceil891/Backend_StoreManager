package org.example.storemanager.entity.wms;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.sales.SaleOrder;

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
    private String status; // PACKING, PACKED, HANDED_OVER

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private SaleOrder order; // Phiếu đóng gói này thuộc về đơn đặt hàng nào
}