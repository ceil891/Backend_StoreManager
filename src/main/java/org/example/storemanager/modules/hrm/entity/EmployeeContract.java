package org.example.storemanager.modules.hrm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.User;

import java.time.LocalDate;

@Entity
@Table(name = "employee_contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class EmployeeContract extends BaseEntity {

    @Column(name = "contract_number", nullable = false, unique = true, length = 50)
    private String contractNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "contract_type", length = 50)
    private String contractType; // Thử việc, Có thời hạn, Vô thời hạn...

    @Column(nullable = false, length = 30)
    private String status; // ACTIVE, EXPIRED, TERMINATED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;
}