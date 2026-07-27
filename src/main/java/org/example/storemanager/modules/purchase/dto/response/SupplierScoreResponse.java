package org.example.storemanager.modules.purchase.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplierScoreResponse {
    private Long supplierId;
    private String supplierName;
    private Double qualityScore;
    private Double deliveryScore;
    private Double priceScore;
    private Double serviceScore;
    private Double overallScore;
}
