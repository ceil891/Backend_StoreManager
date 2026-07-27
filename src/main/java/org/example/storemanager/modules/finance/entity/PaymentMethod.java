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
}