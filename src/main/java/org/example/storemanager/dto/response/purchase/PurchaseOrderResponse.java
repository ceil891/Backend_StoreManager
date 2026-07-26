package org.example.storemanager.dto.response.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {
    private Long id;
    private String poCode;
    private LocalDateTime poDate;
    private LocalDateTime expectedDate;
    private BigDecimal totalAmount;
    private String status;
    private Long supplierId;
    private String supplierName;
    private Long branchId;
    private String branchName;
    private String note;
    private LocalDateTime createdAt;
    private String createdBy;
    private List<PurchaseOrderDetailResponse> details;
}
