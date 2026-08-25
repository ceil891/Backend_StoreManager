package org.example.storemanager.modules.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    @Column(name = "base_salary", precision = 18, scale = 2, nullable = false)
    private BigDecimal baseSalary;

    @Column(precision = 18, scale = 2)
    private BigDecimal allowance; // Phụ cấp

    @Column(precision = 18, scale = 2)
    private BigDecimal deduction; // Khấu trừ (Bảo hiểm, Phạt...)

    @Column(name = "net_salary", precision = 18, scale = 2, nullable = false)
    private BigDecimal netSalary; // Lương thực nhận

    @Column(nullable = false, length = 30)
    private String status; // DRAFT, APPROVED, PAID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @JsonIgnore
    public User getUser() {
        return user;
    }
}