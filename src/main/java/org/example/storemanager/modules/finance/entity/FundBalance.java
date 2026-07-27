package org.example.storemanager.modules.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.Branch;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fund_balances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class FundBalance extends BaseEntity {

    @Column(name = "balance_date", nullable = false)
    private LocalDate balanceDate;

    @Column(name = "cash_balance", precision = 18, scale = 2, nullable = false)
    private BigDecimal cashBalance;

    @Column(name = "bank_balance", precision = 18, scale = 2, nullable = false)
    private BigDecimal bankBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
}