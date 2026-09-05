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
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User user;

    @Transient
    private String employeeName;

    @Transient
    @JsonAlias({"kpiMonth"})
    private String kpiMonth;

    public String getEmployeeName() {
        if (user != null && user.getFullName() != null) return user.getFullName();
        return employeeName != null ? employeeName : "Nhân viên";
    }

    public String getKpiMonth() {
        if (periodYear != null && periodMonth != null) {
            return String.format("%04d-%02d", periodYear, periodMonth);
        }
        return kpiMonth != null ? kpiMonth : "2026-06";
    }
}