package org.example.storemanager.entity.warranty;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.partnerarea.Supplier;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "supplier_contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SupplierContract extends BaseEntity {

    @Column(name = "contract_code", nullable = false, unique = true, length = 50)
    private String contractCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "max_debt_limit", precision = 18, scale = 2)
    private BigDecimal maxDebtLimit; // Hạn mức công nợ tối đa cho phép

    @Column(nullable = false, length = 30)
    private String status; // ACTIVE, EXPIRED, TERMINATED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;
}