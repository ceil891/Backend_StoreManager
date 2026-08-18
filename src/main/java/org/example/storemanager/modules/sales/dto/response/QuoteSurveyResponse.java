package org.example.storemanager.modules.sales.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteSurveyResponse {
    private Long id;
    private String surveyCode;

    private Long customerId;
    private String customerName;

    private Long branchId;
    private String branchName;

    private String contactPerson;
    private String contactPhone;
    private String contactEmail;

    private Long salespersonId;
    private String salespersonName;

    private LocalDateTime surveyDate;
    private LocalDateTime responseDeadline;

    // Nhu cầu khách hàng
    private String requestedProducts;
    private String expectedQuantity;
    private BigDecimal expectedBudget;
    private String technicalRequirements;
    private String deliveryRequirements;
    private String paymentRequirements;

    // Kết quả khảo sát
    private String potentialLevel;
    private String note;
    private String attachments;

    private String status;
    private Long quoteId;

    private LocalDateTime createdAt;
    private String createdBy;
}
