package org.example.storemanager.modules.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "supplier_storages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SupplierStorage extends BaseEntity {

    @Column(name = "storage_code", length = 50, nullable = false)
    private String storageCode;

    @Column(name = "storage_name", length = 200, nullable = false)
    private String storageName;

    @Column(name = "warehouse_name", length = 200)
    private String warehouseName;

    @Column(name = "storage_type", length = 50)
    private String storageType;

    @Column(name = "area_type", length = 50)
    private String areaType;

    @Column(name = "zone_type", length = 50)
    private String zoneType;

    @Column(name = "putaway_rule", length = 50)
    private String putawayRule;

    @Column(precision = 14, scale = 2)
    private BigDecimal capacity;

    @Column(name = "capacity_pallets")
    private Integer capacityPallets;

    @Column(name = "used_pallets")
    private Integer usedPallets;

    @Column(name = "current_usage", precision = 14, scale = 2)
    private BigDecimal currentUsage;

    @Column(name = "capacity_unit", length = 30)
    private String capacityUnit;

    @Column(name = "operating_hours", length = 100)
    private String operatingHours;

    @Column(name = "allow_import")
    @Builder.Default
    private Boolean allowImport = true;

    @Column(name = "allow_export")
    @Builder.Default
    private Boolean allowExport = true;

    @Column(name = "allow_transfer")
    @Builder.Default
    private Boolean allowTransfer = true;

    @Column(length = 30)
    @Builder.Default
    private String status = "TRONG";

    @Column(columnDefinition = "TEXT")
    private String notes;
}
