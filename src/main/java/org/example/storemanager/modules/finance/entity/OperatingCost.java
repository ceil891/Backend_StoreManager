package org.example.storemanager.modules.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.Branch;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "operating_costs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"branch"})
@ToString(callSuper = true, exclude = {"branch"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "branch"})
public class OperatingCost extends BaseEntity {

    @Column(name = "cost_date", nullable = false)
    private LocalDate costDate;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 30)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    @JsonIgnore
    private Branch branch;

    @JsonIgnore
    public Branch getBranch() {
        return branch;
    }

    @Column(name = "cost_center_id")
    private Long costCenterId; // Tham chiếu đến Trung tâm chi phí ở phân hệ Kế toán chuyên sâu
}