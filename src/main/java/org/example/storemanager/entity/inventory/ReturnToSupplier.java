package org.example.storemanager.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.system.Branch;
import org.example.storemanager.entity.partnerarea.Supplier;
import org.example.storemanager.enums.inventory.ReturnToSupplierStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "return_to_suppliers", indexes = {
        @Index(name = "idx_return_supplier_branch", columnList = "branch_id"),
        @Index(name = "idx_return_supplier_supplier", columnList = "supplier_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ReturnToSupplier extends BaseEntity {

    @Column(name = "return_code", nullable = false, unique = true, length = 50)
    private String returnCode;

    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReturnToSupplierStatus status;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "grn_ref_number", length = 50)
    private String grnRefNumber;
}