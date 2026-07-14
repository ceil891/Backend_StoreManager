package org.example.storemanager.entity.hrm;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.system.Role;
import org.example.storemanager.enums.hrm.PositionRank;

import java.math.BigDecimal;

@Entity
@Table(name = "positions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Position extends BaseEntity {

    @Column(name = "position_code", nullable = false, unique = true, length = 50)
    private String positionCode;

    @Column(name = "position_name", nullable = false, length = 150)
    private String positionName;

    @Column(name = "base_salary", precision = 18, scale = 2)
    private BigDecimal baseSalary; // Mức lương cơ sở cho vị trí này

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @Column(name = "position_rank", length = 50)
    private PositionRank positionRank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "management_status_id")
    private Role managementStatus;

    @Column(columnDefinition = "TEXT")
    private String description;

}