package org.example.storemanager.modules.purchase.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.partnerarea.entity.Supplier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PurchaseInvoice extends BaseEntity {

    @Column(name = "invoice_code", nullable = false, unique = true, length = 50)
    private String invoiceCode;

    @Column(name = "po_code", length = 50)
    private String poCode;

    @Column(name = "po_id")
    private Long poId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "invoice_date", nullable = false)
    private LocalDateTime invoiceDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "sub_total", precision = 18, scale = 2)
    private BigDecimal subTotal;

    @Column(name = "vat_amount", precision = 18, scale = 2)
    private BigDecimal vatAmount;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 50)
    private String status; // CHO_THANH_TOAN, DA_THANH_TOAN, DA_HUY

    @Column(name = "payment_terms", length = 50)
    private String paymentTerms;

    @OneToMany(mappedBy = "purchaseInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<PurchaseInvoiceItem> items = new java.util.ArrayList<>();
}
