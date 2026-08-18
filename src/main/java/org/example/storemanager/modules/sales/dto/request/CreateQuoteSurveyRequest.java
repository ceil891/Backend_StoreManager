package org.example.storemanager.modules.sales.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateQuoteSurveyRequest {
    private String surveyCode;

    @NotNull(message = "Khách hàng không được để trống")
    private Long customerId;

    @NotNull(message = "Chi nhánh không được để trống")
    private Long branchId;

    private String contactPerson;
    private String contactPhone;
    private String contactEmail;

    private Long salespersonId;
    private String salespersonName;

    @NotNull(message = "Ngày khảo sát không được để trống")
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
    private String potentialLevel; // THAP, TRUNG_BINH, CAO, RAT_CAO
    private String note;
    private String attachments;

    @NotBlank(message = "Trạng thái không được để trống")
    private String status; // NEW, IN_PROGRESS, INFO_COMPLETED, QUOTED, CLOSED
}
