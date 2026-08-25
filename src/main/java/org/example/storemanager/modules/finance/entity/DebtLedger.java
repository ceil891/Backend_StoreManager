package org.example.storemanager.modules.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "debt_ledgers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class DebtLedger extends BaseEntity {

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "ref_code", length = 50)
    private String refCode; // Mã chứng từ tham chiếu (Hóa đơn, Phiếu nhập...)

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal increase; // Phát sinh tăng công nợ

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal decrease; // Phát sinh giảm công nợ (Do thanh toán)

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal balance; // Dư nợ còn lại

    @Column(name = "partner_id", nullable = false)
    private Long partnerId; // Liên kết phẳng đến Customer ID hoặc Supplier ID

    @Column(name = "entity_name", length = 200)
    private String entityName; // Tên đối tác / doanh nghiệp

    @Column(name = "entity_type", length = 30)
    private String entityType; // CUSTOMER, SUPPLIER, PARTNER

    @Column(name = "due_date")
    private LocalDateTime dueDate; // Hạn thanh toán

    @Column(name = "status", length = 30)
    private String status; // NORMAL, DUE_SOON, OVERDUE, SETTLED

    @Column(name = "last_payment_date")
    private LocalDateTime lastPaymentDate; // Ngày giao dịch gần nhất

    @Column(name = "account_manager", length = 100)
    private String accountManager; // NV phụ trách
}