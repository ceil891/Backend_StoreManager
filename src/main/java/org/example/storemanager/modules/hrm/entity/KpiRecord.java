package org.example.storemanager.modules.hrm.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.User;

import java.math.BigDecimal;

@Entity
@Table(name = "kpi_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"user"})
@ToString(callSuper = true, exclude = {"user"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class KpiRecord extends BaseEntity {

    @Column(name = "period_month")
    private Integer periodMonth;

    @Column(name = "period_year")
    private Integer periodYear;

    @Column(name = "target_score", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal targetScore = BigDecimal.valueOf(100);

    @Column(name = "achieved_score", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal achievedScore = BigDecimal.ZERO;

    @Column(name = "department_name", length = 100)
    private String departmentName;

    @Column(name = "rating_grade", length = 50)
    private String ratingGrade; // A_EXCELLENT, B_GOOD, C_AVERAGE, D_POOR

    @Column(name = "bonus_amount", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal bonusAmount = BigDecimal.ZERO;

    @Column(length = 30)
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    @Column(name = "employee_name", length = 100)
    private String employeeName;

    @Transient
    @JsonAlias({"kpiMonth"})
    private String kpiMonth;

    public Long getUserId() {
        try {
            if (user != null) return user.getId();
        } catch (Exception ignored) {}
        return null;
    }

    public String getEmployeeName() {
        if (employeeName != null && !employeeName.isBlank()) return employeeName;
        try {
            if (user != null && org.hibernate.Hibernate.isInitialized(user) && user.getFullName() != null) return user.getFullName();
        } catch (Exception ignored) {}
        return employeeName != null ? employeeName : "Nhân viên";
    }

    public String getKpiMonth() {
        if (periodYear != null && periodMonth != null) {
            return String.format("%04d-%02d", periodYear, periodMonth);
        }
        return kpiMonth != null ? kpiMonth : "2026-06";
    }
}