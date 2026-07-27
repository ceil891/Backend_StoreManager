package org.example.storemanager.modules.hrm.entity;

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
@EqualsAndHashCode(callSuper = true)
public class KpiRecord extends BaseEntity {

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    @Column(name = "target_score", precision = 18, scale = 2, nullable = false)
    private BigDecimal targetScore; // Chỉ tiêu đề ra (VD: Doanh thu, số lượng đơn)

    @Column(name = "achieved_score", precision = 18, scale = 2, nullable = false)
    private BigDecimal achievedScore; // Thực tế đạt được

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}