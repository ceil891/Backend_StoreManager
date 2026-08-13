package org.example.storemanager.modules.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.partnerarea.entity.Customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quotes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Quote extends BaseEntity {

    @Column(name = "quote_code", nullable = false, unique = true, length = 50)
    private String quoteCode;

    @Column(name = "quote_date", nullable = false)
    private LocalDateTime quoteDate;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Builder.Default
    @Column(name = "revision", nullable = false)
    private Integer revision = 1;

    @Builder.Default
    @Column(name = "currency", length = 10)
    private String currency = "VND";

    @Column(name = "payment_terms", length = 255)
    private String paymentTerms;

    @Column(name = "delivery_terms", length = 255)
    private String deliveryTerms;

    @Column(name = "warranty_terms", length = 255)
    private String warrantyTerms;

    @Column(name = "validity_terms", length = 255)
    private String validityTerms;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "sub_total", precision = 18, scale = 2)
    private BigDecimal subTotal;

    @Column(name = "discount_type", length = 20)
    private String discountType; // PERCENT, AMOUNT

    @Column(name = "discount_value", precision = 18, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "shipping_fee", precision = 18, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 30)
    private String status; // DRAFT, SENT, ACCEPTED, REJECTED, EXPIRED, CANCELLED

    @Column(name = "sales_person_id")
    private Long salesPersonId;

    @Column(name = "sales_person_name", length = 150)
    private String salesPersonName;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "warehouse_name", length = 150)
    private String warehouseName;

    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
}