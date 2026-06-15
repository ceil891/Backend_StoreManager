package org.example.storemanager.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.catalog.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "inventory_check_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class InventoryCheckDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_id", nullable = false)
    private InventoryCheck check;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "system_qty", precision = 18, scale = 3, nullable = false)
    private BigDecimal systemQty; // SL tồn trên phần mềm

    @Column(name = "actual_qty", precision = 18, scale = 3, nullable = false)
    private BigDecimal actualQty; // SL thực tế kiểm đếm

    @Column(name = "diff_qty", precision = 18, scale = 3)
    private BigDecimal diffQty; // Độ lệch

    @Column(columnDefinition = "TEXT")
    private String reason; // Lý do lệch (Thất thoát, hỏng hóc...)
}