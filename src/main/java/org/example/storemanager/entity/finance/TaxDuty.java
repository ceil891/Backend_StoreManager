package org.example.storemanager.entity.finance;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.system.Branch;

import java.math.BigDecimal;

@Entity
@Table(name = "tax_duties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class TaxDuty extends BaseEntity {

    @Column(name = "tax_type", nullable = false, length = 50)
    private String taxType; // VAT, PIT, CIT...

    @Column(length = 50)
    private String period; // Kỳ tính thuế: VD "04-2026", "Q1-2026"

    @Column(name = "amount_due", precision = 18, scale = 2, nullable = false)
    private BigDecimal amountDue; // Số tiền phải nộp

    @Column(name = "amount_paid", precision = 18, scale = 2, nullable = false)
    private BigDecimal amountPaid; // Số tiền đã nộp

    @Column(nullable = false, length = 30)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
}