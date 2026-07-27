package org.example.storemanager.modules.purchase.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierEvaluationResponse {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private LocalDate evalDate;
    private Integer score;
    private String remarks;
    private Long evaluatedById;
    private String evaluatedByName;
    private String evaluationType;
    private Integer qualityScore;
    private Integer deliveryScore;
    private Integer serviceScore;
    private Integer priceScore;
    private Integer overallScore;
    private String result;
    private String improvement;
    private String note;
    private LocalDateTime createdAt;
    private String createdBy;
}
