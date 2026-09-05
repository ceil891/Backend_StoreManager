package org.example.storemanager.modules.finance.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.User;

import java.math.BigDecimal;

@Entity
@Table(name = "payrolls")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"user"})
@ToString(callSuper = true, exclude = {"user"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user"})
public class Payroll extends BaseEntity {

    @Column(name = "payroll_code", length = 50)
    @JsonAlias({"payrollCode"})
    private String payrollCode;

    @Column(name = "period_month")
    private Integer periodMonth;

    @Column(name = "period_year")
    private Integer periodYear;

    @Column(name = "base_salary", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal baseSalary = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    @Builder.Default
    @JsonAlias({"allowance", "allowances"})
    private BigDecimal allowance = BigDecimal.ZERO; // Phụ cấp

    @Column(name = "kpi_bonus", precision = 18, scale = 2)
    @Builder.Default
    @JsonAlias({"kpiBonus"})
    private BigDecimal kpiBonus = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    @Builder.Default
    @JsonAlias({"deduction", "deductions"})
    private BigDecimal deduction = BigDecimal.ZERO; // Khấu trừ

    @Column(name = "net_salary", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal netSalary = BigDecimal.ZERO; // Lương thực nhận

    @Column(length = 30)
    @Builder.Default
    private String status = "DRAFT"; // DRAFT, APPROVED, PAID

    @Column(name = "payment_date", length = 30)
    private String paymentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User user;

    @Transient
    private Long userId;

    @Transient
    private String employeeName;

    @Transient
    @JsonAlias({"payrollMonth"})
    private String payrollMonth;

    public Long getUserId() {
        return user != null ? user.getId() : userId;
    }

    public String getEmployeeName() {
        return user != null ? user.getFullName() : (employeeName != null ? employeeName : "Nhân viên");
    }

    public String getPayrollCode() {
        return payrollCode != null ? payrollCode : (getId() != null ? "PR-" + getId() : "PR-NEW");
    }

    public String getPayrollMonth() {
        if (periodYear != null && periodMonth != null) {
            return String.format("%04d-%02d", periodYear, periodMonth);
        }
        return payrollMonth != null ? payrollMonth : "";
    }

    public BigDecimal getAllowances() {
        return allowance != null ? allowance : BigDecimal.ZERO;
    }

    public BigDecimal getDeductions() {
        return deduction != null ? deduction : BigDecimal.ZERO;
    }

    public BigDecimal getKpiBonus() {
        return kpiBonus != null ? kpiBonus : BigDecimal.ZERO;
    }
}