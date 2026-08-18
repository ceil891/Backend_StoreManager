package org.example.storemanager.modules.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.partnerarea.entity.Customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quote_surveys")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class QuoteSurvey extends BaseEntity {

    @Column(name = "survey_code", nullable = false, unique = true, length = 50)
    private String surveyCode;

    @Column(name = "contact_person", length = 150)
    private String contactPerson;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "salesperson_id")
    private Long salespersonId;

    @Column(name = "salesperson_name", length = 150)
    private String salespersonName;

    @Column(name = "survey_date", nullable = false)
    private LocalDateTime surveyDate;

    @Column(name = "response_deadline")
    private LocalDateTime responseDeadline;

    // Nhu cầu khách hàng
    @Column(name = "requested_products", columnDefinition = "TEXT")
    private String requestedProducts;

    @Column(name = "expected_quantity", length = 100)
    private String expectedQuantity;

    @Column(name = "expected_budget", precision = 18, scale = 2)
    private BigDecimal expectedBudget;

    @Column(name = "technical_requirements", columnDefinition = "TEXT")
    private String technicalRequirements;

    @Column(name = "delivery_requirements", columnDefinition = "TEXT")
    private String deliveryRequirements;

    @Column(name = "payment_requirements", columnDefinition = "TEXT")
    private String paymentRequirements;

    // Kết quả khảo sát
    @Column(name = "potential_level", length = 30)
    private String potentialLevel; // THAP, TRUNG_BINH, CAO, RAT_CAO

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;

    // Trạng thái: NEW, IN_PROGRESS, INFO_COMPLETED, QUOTED, CLOSED
    @Column(nullable = false, length = 30)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "quote_id")
    private Long quoteId;
}
