package org.example.storemanager.modules.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

@Entity
@Table(name = "payment_method_branches",
        uniqueConstraints = @UniqueConstraint(columnNames = {"payment_method_id", "branch_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PaymentMethodBranch extends BaseEntity {

    @Column(name = "payment_method_id", nullable = false)
    private Long paymentMethodId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;
}
