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
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class DebtLedger extends BaseEntity {

    @Column(name = "transaction_date", nullable = false)
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd[['T'][ ]HH:mm:ss][.SSS][X]")
    private LocalDateTime transactionDate;

    @com.fasterxml.jackson.annotation.JsonProperty("transactionDate")
    public void setTransactionDate(Object dateObj) {
        this.transactionDate = parseDateTime(dateObj, LocalDateTime.now());
    }

    @Column(name = "ref_code", length = 50)
    @com.fasterxml.jackson.annotation.JsonAlias({"debtCode", "refCode"})
    private String refCode; // Mã chứng từ tham chiếu (Hóa đơn, Phiếu nhập...)

    @com.fasterxml.jackson.annotation.JsonProperty("debtCode")
    public String getDebtCode() {
        return refCode;
    }

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal increase; // Phát sinh tăng công nợ

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal decrease; // Phát sinh giảm công nợ (Do thanh toán)

    @Column(precision = 18, scale = 2, nullable = false)
    @com.fasterxml.jackson.annotation.JsonAlias({"totalDebt", "balance", "dueAmount"})
    private BigDecimal balance; // Dư nợ còn lại

    @com.fasterxml.jackson.annotation.JsonProperty("totalDebt")
    public BigDecimal getTotalDebt() {
        return balance;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("dueAmount")
    public BigDecimal getDueAmount() {
        return balance;
    }

    @Column(name = "partner_id", nullable = false)
    private Long partnerId; // Liên kết phẳng đến Customer ID hoặc Supplier ID

    @Column(name = "entity_name", length = 200)
    private String entityName; // Tên đối tác / doanh nghiệp

    @Column(name = "entity_type", length = 30)
    private String entityType; // CUSTOMER, SUPPLIER, PARTNER

    @Column(name = "due_date")
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd[['T'][ ]HH:mm:ss][.SSS][X]")
    private LocalDateTime dueDate; // Hạn thanh toán

    @com.fasterxml.jackson.annotation.JsonProperty("dueDate")
    public void setDueDate(Object dateObj) {
        this.dueDate = parseDateTime(dateObj, null);
    }

    @Column(name = "status", length = 30)
    private String status; // NORMAL, DUE_SOON, OVERDUE, SETTLED

    @Column(name = "last_payment_date")
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd[['T'][ ]HH:mm:ss][.SSS][X]")
    private LocalDateTime lastPaymentDate; // Ngày giao dịch gần nhất

    @com.fasterxml.jackson.annotation.JsonProperty("lastPaymentDate")
    public void setLastPaymentDate(Object dateObj) {
        this.lastPaymentDate = parseDateTime(dateObj, null);
    }

    @Column(name = "account_manager", length = 100)
    private String accountManager; // NV phụ trách

    @Column(name = "notes", length = 500)
    @com.fasterxml.jackson.annotation.JsonAlias({"notes", "note", "description"})
    private String notes;

    private static LocalDateTime parseDateTime(Object dateObj, LocalDateTime fallback) {
        if (dateObj == null) return fallback;
        if (dateObj instanceof LocalDateTime) return (LocalDateTime) dateObj;
        String str = dateObj.toString().trim();
        if (str.isEmpty()) return fallback;
        try {
            if (str.length() == 10) {
                return java.time.LocalDate.parse(str).atStartOfDay();
            } else if (str.contains("T")) {
                return LocalDateTime.parse(str.split("\\+")[0].split("Z")[0]);
            } else if (str.contains(" ")) {
                return LocalDateTime.parse(str.replace(" ", "T"));
            } else {
                return LocalDateTime.parse(str);
            }
        } catch (Exception e) {
            return fallback;
        }
    }
}