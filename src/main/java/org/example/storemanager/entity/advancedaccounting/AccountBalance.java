package org.example.storemanager.entity.advancedaccounting;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "account_balances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class AccountBalance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private ChartOfAccount account;

    @Column(nullable = false, length = 50)
    private String period; // e.g. "2026-07" or "2026"

    @Builder.Default
    @Column(name = "opening_balance", precision = 18, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Builder.Default
    @Column(precision = 18, scale = 2)
    private BigDecimal debit = BigDecimal.ZERO;

    @Builder.Default
    @Column(precision = 18, scale = 2)
    private BigDecimal credit = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "closing_balance", precision = 18, scale = 2)
    private BigDecimal closingBalance = BigDecimal.ZERO;
}
