package org.example.storemanager.entity.crm;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

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
}