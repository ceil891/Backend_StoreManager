package org.example.storemanager.modules.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

@Entity
@Table(name = "transaction_reasons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class TransactionReason extends BaseEntity {

    @Column(name = "reason_code", nullable = false, unique = true, length = 50)
    private String reasonCode;

    @Column(name = "reason_name", nullable = false, length = 150)
    private String reasonName;

    @Column(length = 30)
    private String type; // RECEIPT (Thu) hoặc PAYMENT (Chi)

    @Column(name = "accounting_code", length = 30)
    private String accountingCode;

    @Column(length = 255)
    private String description;

    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }

    public String getReasonName() { return reasonName; }
    public void setReasonName(String reasonName) { this.reasonName = reasonName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAccountingCode() { return accountingCode; }
    public void setAccountingCode(String accountingCode) { this.accountingCode = accountingCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}