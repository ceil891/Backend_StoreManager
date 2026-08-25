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
@Table(name = "fund_balances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"branch"})
@ToString(callSuper = true, exclude = {"branch"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "branch"})
public class FundBalance extends BaseEntity {

    @Column(name = "balance_date", nullable = false)
    private LocalDate balanceDate;

    @Column(name = "cash_balance", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal cashBalance = BigDecimal.ZERO;

    @Column(name = "bank_balance", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal bankBalance = BigDecimal.ZERO;

    @Column(name = "branch_name", length = 150)
    private String branchName;

    @Column(name = "manager_name", length = 100)
    private String managerName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = true)
    @JsonIgnore
    private Branch branch;

    @JsonIgnore
    public Branch getBranch() {
        return branch;
    }
}