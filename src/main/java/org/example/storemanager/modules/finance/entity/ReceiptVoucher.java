package org.example.storemanager.modules.finance.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.Branch;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "receipt_vouchers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReceiptVoucher extends BaseEntity {

    @Column(name = "voucher_code", nullable = false, unique = true, length = 50)
    private String voucherCode;

    @Column(name = "voucher_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd[['T'][ ]HH:mm:ss][.SSS][X]")
    private LocalDateTime voucherDate;

    @com.fasterxml.jackson.annotation.JsonProperty("voucherDate")
    public void setVoucherDate(Object dateObj) {
        if (dateObj == null) {
            this.voucherDate = LocalDateTime.now();
            return;
        }
        if (dateObj instanceof LocalDateTime) {
            this.voucherDate = (LocalDateTime) dateObj;
            return;
        }
        String str = dateObj.toString().trim();
        if (str.isEmpty()) {
            this.voucherDate = LocalDateTime.now();
            return;
        }
        try {
            if (str.length() == 10) {
                this.voucherDate = java.time.LocalDate.parse(str).atStartOfDay();
            } else if (str.contains("T")) {
                this.voucherDate = LocalDateTime.parse(str.split("\\+")[0].split("Z")[0]);
            } else if (str.contains(" ")) {
                this.voucherDate = LocalDateTime.parse(str.replace(" ", "T"));
            } else {
                this.voucherDate = LocalDateTime.parse(str);
            }
        } catch (Exception e) {
            this.voucherDate = LocalDateTime.now();
        }
    }

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "payer_name", length = 150)
    private String payerName; // Người nộp tiền

    @Column(nullable = false, length = 30)
    private String status; // DRAFT, COMPLETED, CANCELLED

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "fund_account_name", length = 150)
    private String fundAccountName;

    @Column(name = "invoice_code", length = 50)
    private String invoiceCode;

    @Column(name = "handler", length = 100)
    private String handler;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reason_id", nullable = true)
    private TransactionReason reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = true)
    private Branch branch;
}