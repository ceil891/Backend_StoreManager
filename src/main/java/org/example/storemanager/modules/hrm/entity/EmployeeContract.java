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
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Position position;

    @Transient
    private String contractCode;

    @Column(name = "employee_name", length = 100)
    private String employeeName;

    @Column(name = "employee_phone", length = 30)
    private String employeePhone;

    public String getContractCode() {
        return contractNumber != null ? contractNumber : (contractCode != null ? contractCode : (getId() != null ? "HD-" + getId() : null));
    }

    public Long getUserId() {
        try {
            if (user != null) return user.getId();
        } catch (Exception ignored) {}
        return null;
    }

    public Long getPositionId() {
        try {
            if (position != null) return position.getId();
        } catch (Exception ignored) {}
        return null;
    }

    public String getPositionName() {
        try {
            if (position != null && org.hibernate.Hibernate.isInitialized(position)) {
                return position.getPositionName();
            }
        } catch (Exception ignored) {}
        return "Nhân viên chính thức";
    }

    public String getEmployeeName() {
        if (employeeName != null && !employeeName.isBlank()) return employeeName;
        try {
            if (user != null && org.hibernate.Hibernate.isInitialized(user) && user.getFullName() != null) return user.getFullName();
        } catch (Exception ignored) {}
        return employeeName != null ? employeeName : "Nhân viên";
    }

    public String getEmployeePhone() {
        if (employeePhone != null && !employeePhone.isBlank()) return employeePhone;
        try {
            if (user != null && org.hibernate.Hibernate.isInitialized(user) && user.getPhone() != null) return user.getPhone();
        } catch (Exception ignored) {}
        return employeePhone != null ? employeePhone : "";
    }
}