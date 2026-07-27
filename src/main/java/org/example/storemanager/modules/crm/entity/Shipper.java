package org.example.storemanager.modules.crm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.catalog.entity.Department;

@Entity
@Table(name = "shippers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Shipper extends BaseEntity {

    @Column(name = "shipper_code", nullable = false, unique = true, length = 50)
    private String shipperCode;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Column(name = "license_plate", length = 20)
    private String licensePlate; // Biển số xe

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @Column(length = 100)
    private String email; // Email

    @Column(length = 255)
    private String address; // Địa chỉ

    @Column(name = "vehicle_type", length = 50)
    private String vehicleType; // Xe máy, ô tô

    @Column(name = "vehicle_number", length = 20)
    private String vehicleNumber; // Số xe

    @Column(name = "identity_number", length = 20)
    private String identityNumber; // CCCD

    @Column(length = 500)
    private String avatar; // Ảnh đại diện

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department; // Kho/CN

    @Column(name = "current_latitude")
    private Double currentLatitude; // GPS

    @Column(name = "current_longitude")
    private Double currentLongitude; // GPS

    @Column(length = 30)
    private String status; // ONLINE, OFFLINE, BUSY
}