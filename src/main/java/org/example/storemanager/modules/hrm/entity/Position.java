package org.example.storemanager.modules.hrm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "positions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
}