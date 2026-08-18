package org.example.storemanager.modules.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

@Entity
@Table(name = "payment_methods")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PaymentMethod extends BaseEntity {

    @Column(name = "method_code", nullable = false, unique = true, length = 50)
    private String methodCode;

    @Column(name = "method_name", nullable = false, length = 100)
    private String methodName; // VD: Tiền mặt, Chuyển khoản, Thẻ tín dụng, Ví điện tử...

    @Column(length = 30)
    private String type; // CASH, BANK_TRANSFER, CARD, E_WALLET

    @Column(name = "fee_type", length = 20)
    private String feeType; // PERCENT, FIXED

    @Column(name = "fee_value", precision = 18, scale = 2)
    private java.math.BigDecimal feeValue;

    @Column(length = 20)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, TESTING_MODE, MAINTENANCE, DISABLED

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // Backward compatibility fields for the existing frontend admin page
    @Column(name = "provider_type", length = 50)
    private String providerType; // Mapped to type

    @Column(name = "processing_fee_pct", precision = 5, scale = 2)
    private java.math.BigDecimal processingFeePct = java.math.BigDecimal.ZERO;

    @Column(name = "fixed_fee_usd", precision = 18, scale = 2)
    private java.math.BigDecimal fixedFeeUsd = java.math.BigDecimal.ZERO;

    @Column(name = "settlement_time", length = 50)
    private String settlementTime = "INSTANT";

    @Column(name = "total_volume_usd", precision = 18, scale = 2)
    private java.math.BigDecimal totalVolumeUsd = java.math.BigDecimal.ZERO;

    @Column(name = "configured_gateways", length = 250)
    private String configuredGateways;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "bank_name", length = 150)
    private String bankName;

    @Column(name = "bank_account", length = 50)
    private String bankAccount;

    @Column(name = "bank_account_name", length = 150)
    private String bankAccountName;

    @Column(name = "transfer_syntax", length = 150)
    private String transferSyntax = "POS {order_code}";

    @Column(name = "merchant_id", length = 100)
    private String merchantId;

    @Column(name = "api_key", length = 250)
    private String apiKey;

    @Column(name = "secret_key", length = 250)
    private String secretKey;

    @Column(name = "checksum_key", length = 250)
    private String checksumKey;

    @Builder.Default
    @Column(name = "allow_pos")
    private Boolean allowPos = true;

    @Builder.Default
    @Column(name = "allow_online")
    private Boolean allowOnline = false;

    @Builder.Default
    @Column(name = "apply_to_all_branches", columnDefinition = "boolean default true")
    private Boolean applyToAllBranches = true;

    @Builder.Default
    @Column(name = "currency", length = 10)
    private String currency = "VND";

    @PrePersist
    @PreUpdate
    public void syncFields() {
        if (this.providerType != null) {
            this.type = this.providerType;
        } else if (this.type != null) {
            this.providerType = this.type;
        }

        if (this.feeType == null) {
            this.feeType = "PERCENT";
        }

        if (this.feeValue != null) {
            if ("PERCENT".equalsIgnoreCase(this.feeType)) {
                this.processingFeePct = this.feeValue;
            } else {
                this.fixedFeeUsd = this.feeValue;
            }
        } else {
            if ("PERCENT".equalsIgnoreCase(this.feeType)) {
                this.feeValue = this.processingFeePct;
            } else {
                this.feeValue = this.fixedFeeUsd;
            }
        }

        if (this.status == null) {
            this.status = "ACTIVE";
        }
        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }
    }
}