package org.example.storemanager.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.system.Branch;
import org.example.storemanager.entity.catalog.ProductVariant;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_balances", uniqueConstraints = {
        @UniqueConstraint(name = "uq_inv_balance_variant_branch", columnNames = {"product_variant_id", "branch_id"})
}, indexes = {
        @Index(name = "idx_inv_balance_variant", columnList = "product_variant_id"),
        @Index(name = "idx_inv_balance_branch", columnList = "branch_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class InventoryBalance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "available_quantity", precision = 18, scale = 3, nullable = false)
    private BigDecimal availableQuantity;

    @Column(name = "reserved_quantity", precision = 18, scale = 3, nullable = false)
    private BigDecimal reservedQuantity;

    @Column(name = "damaged_quantity", precision = 18, scale = 3, nullable = false)
    private BigDecimal damagedQuantity;

    @Column(name = "minimum_quantity", precision = 18, scale = 3)
    private BigDecimal minimumQuantity;

    @Column(name = "reorder_point", precision = 18, scale = 3)
    private BigDecimal reorderPoint;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
