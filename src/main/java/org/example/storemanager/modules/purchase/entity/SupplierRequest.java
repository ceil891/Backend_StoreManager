package org.example.storemanager.modules.purchase.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "supplier_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SupplierRequest extends BaseEntity {

    @Column(name = "rfq_code", length = 50, nullable = false)
    private String rfqCode;

    @Column(name = "supplier_name", length = 500)
    private String supplierName;

    @Column(name = "selected_suppliers", columnDefinition = "TEXT")
    private String selectedSuppliers;

    @Column(name = "destination_branch", length = 200)
    private String destinationBranch;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "sent_date")
    private LocalDate sentDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(length = 150)
    private String handler;

    @Column(length = 50)
    @Builder.Default
    private String status = "CHO_BAO_GIA"; // CHO_BAO_GIA, DA_BAO_GIA, DA_HUY

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "supplierRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SupplierRequestDetail> details = new ArrayList<>();
}
