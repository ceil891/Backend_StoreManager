package org.example.storemanager.modules.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.system.entity.PosSession;
import org.example.storemanager.modules.partnerarea.entity.Customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "export_invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ExportInvoice extends BaseEntity {

    @Column(name = "invoice_code", nullable = false, unique = true, length = 50)
    private String invoiceCode;

    @Column(name = "invoice_date", nullable = false)
    private LocalDateTime invoiceDate;

    @Column(name = "sub_total", precision = 18, scale = 2)
    private BigDecimal subTotal;

    @Column(precision = 18, scale = 2)
    private BigDecimal discount;

    @Column(precision = 18, scale = 2)
    private BigDecimal tax;

    @Column(name = "total_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 30)
    private String status; // DRAFT, PAID, CANCELLED, RETURNED

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "payment_terms", length = 50)
    private String paymentTerms;

    @Column(name = "einvoice_ref", length = 100)
    private String einvoiceRef;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pos_session_id")
    private PosSession posSession; // Gắn liền với ca làm việc nếu tạo từ POS
}