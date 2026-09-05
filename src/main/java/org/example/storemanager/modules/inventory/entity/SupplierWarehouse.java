package org.example.storemanager.modules.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "supplier_warehouses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SupplierWarehouse extends BaseEntity {

    @Column(name = "warehouse_code", length = 50, nullable = false)
    private String warehouseCode;

    @Column(name = "warehouse_name", length = 200, nullable = false)
    private String warehouseName;

    @Column(name = "supplier_name", length = 200)
    private String supplierName;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "warehouse_type", length = 50)
    private String warehouseType;

    @Column(precision = 14, scale = 2)
    private BigDecimal capacity;

    @Column(name = "capacity_unit", length = 30)
    private String capacityUnit;

    @Column(name = "manager_name", length = 150)
    private String managerName;

    @Column(name = "manager_phone", length = 50)
    private String managerPhone;

    @Column(name = "manager_email", length = 150)
    private String managerEmail;

    @Column(name = "contact_person", length = 150)
    private String contactPerson;

    @Column(length = 50)
    private String phone;

    @Column(name = "loading_contact_phone", length = 50)
    private String loadingContactPhone;

    @Column(name = "operating_hours", length = 100)
    private String operatingHours;

    @Column(name = "operating_days", length = 100)
    private String operatingDays;

    @Column(name = "storage_conditions", length = 255)
    private String storageConditions;

    @Column(length = 30)
    @Builder.Default
    private String status = "HOAT_DONG";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;
}
