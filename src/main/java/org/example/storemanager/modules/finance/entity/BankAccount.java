package org.example.storemanager.modules.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.Branch;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@Entity
@Table(name = "bank_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"branch"})
@ToString(callSuper = true, exclude = {"branch"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "branch"})
public class BankAccount extends BaseEntity {

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @Column(name = "account_holder", nullable = false, length = 100)
    private String accountHolder;

    @Column(name = "branch_name", length = 150)
    private String branchName; // Chi nhánh của ngân hàng

    @Column(name = "swift_bic", length = 50)
    private String swiftBic;

    @Column(name = "currency", length = 10)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "current_balance", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "available_working_capital", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal availableWorkingCapital = BigDecimal.ZERO;

    @Column(name = "account_type", length = 50)
    @Builder.Default
    private String accountType = "PRIMARY_OPERATING";

    @Column(name = "opened_date", length = 20)
    private String openedDate;

    @Column(name = "status", length = 30)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "is_active", columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    @JsonIgnore
    private Branch branch; // Tài khoản này do chi nhánh cửa hàng nào quản lý

    @JsonIgnore
    public Branch getBranch() {
        return branch;
    }
}