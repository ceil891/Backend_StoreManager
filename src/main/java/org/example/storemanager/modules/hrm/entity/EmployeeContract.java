package org.example.storemanager.modules.hrm.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employee_contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"user", "position"})
@ToString(callSuper = true, exclude = {"user", "position"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EmployeeContract extends BaseEntity {

    @Column(name = "contract_number", nullable = false, unique = true, length = 50)
    @JsonAlias({"contractCode", "contractNumber"})
    private String contractNumber;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "contract_type", length = 50)
    private String contractType; // Thử việc, Có thời hạn, Vô thời hạn...

    @Column(length = 30)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, EXPIRED, TERMINATED

    @Column(name = "base_salary", precision = 18, scale = 2)
    private BigDecimal baseSalary;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Position position;

    @Transient
    private String contractCode;

    @Transient
    private String employeeName;

    @Transient
    private String employeePhone;

    public String getContractCode() {
        return contractNumber != null ? contractNumber : (contractCode != null ? contractCode : (getId() != null ? "HD-" + getId() : null));
    }

    public String getEmployeeName() {
        if (user != null && user.getFullName() != null) return user.getFullName();
        return employeeName != null ? employeeName : "Nhân viên";
    }

    public String getEmployeePhone() {
        if (user != null && user.getPhone() != null) return user.getPhone();
        return employeePhone != null ? employeePhone : "";
    }
}