package org.example.storemanager.entity.finance;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

@Entity
@Table(name = "transaction_reasons")
@Data
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
}