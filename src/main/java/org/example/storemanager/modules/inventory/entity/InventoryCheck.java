package org.example.storemanager.modules.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.wms.entity.WarehouseZone;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_checks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class InventoryCheck extends BaseEntity {

    @Column(name = "check_code", nullable = false, unique = true, length = 50)
    private String checkCode;

    @Column(name = "check_date", nullable = false)
    private LocalDateTime checkDate;

    @Column(nullable = false, length = 30)
    private String status; // DRAFT, BALANCED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_zone_id")
    private WarehouseZone warehouseZone;
}