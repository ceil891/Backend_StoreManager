package org.example.storemanager.modules.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.Branch;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_vouchers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PaymentVoucher extends BaseEntity {

    @Column(name = "voucher_code", nullable = false, unique = true, length = 50)
    private String voucherCode;

    @Column(name = "voucher_date", nullable = false)
    private LocalDateTime voucherDate;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "receiver_name", length = 150)
    private String receiverName; // Người nhận tiền

    @Column(nullable = false, length = 30)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reason_id", nullable = false)
    private TransactionReason reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
}