package org.example.storemanager.entity.hrm;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.system.User;

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

    @Column(name = "salary")
    private Double salary;

    @Column(name = "allowance")
    private Double allowance; // phụ cấp

    @Column(name = "social_insurance_salary")
    private Double socialInsuranceSalary; // lương tính bảo hiểm xã hội

    @Column(name = "contract_url")
    private String contractUrl; // URL hợp đồng

    @Column(name = "signing_date")
    private LocalDate signingDate; // ngày kí hợp đồng

    @Column(name = "working_hours")
    private Double workingHours; // giờ làm việc

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @Column(name = "renewal_date")
    private LocalDate renewalDate; // ngày gia hạn

    @Column(name = "termination_date")
    private LocalDate terminationDate; // ngày chấm dứt

    @Column(name = "termination_reason", length = 500)
    private String terminationReason; // lý do chấm dứt
}