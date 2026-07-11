package org.example.storemanager.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.catalog.Color;
import org.example.storemanager.entity.catalog.Product;
import org.example.storemanager.entity.catalog.Size;
import org.example.storemanager.entity.wms.WarehouseZone;

import java.math.BigDecimal;

@Entity
@Table(name = "size_inventories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"warehouse_zone_id", "product_id", "size_id", "color_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SizeInventory extends BaseEntity {

    /** Khu vực kho (WMS) — không dùng catalog.Department (phòng ban danh mục). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_zone_id", nullable = false)
    private WarehouseZone warehouseZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "size_id")
    private Size size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id")
    private Color color;

    @Column(name = "quantity_physical", precision = 18, scale = 3, nullable = false)
    private BigDecimal quantityPhysical;

    @Column(name = "quantity_allocated", precision = 18, scale = 3, nullable = false)
    private BigDecimal quantityAllocated;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    public BigDecimal getQuantityAvailable() {
        BigDecimal physical = quantityPhysical != null ? quantityPhysical : BigDecimal.ZERO;
        BigDecimal allocated = quantityAllocated != null ? quantityAllocated : BigDecimal.ZERO;
        return physical.subtract(allocated);
    }
}
