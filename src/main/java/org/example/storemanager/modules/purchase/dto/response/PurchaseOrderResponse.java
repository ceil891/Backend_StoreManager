package org.example.storemanager.modules.purchase.dto.response;

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
    private String paymentStatus;
    private BigDecimal advanceAmount;
    private String paymentTerms;
    private BigDecimal shippingFee;
    private BigDecimal vatRate;
    private BigDecimal vatAmount;
    private BigDecimal discountAmount;
    private LocalDateTime createdAt;
    private String createdBy;
    private List<PurchaseOrderDetailResponse> details;
}
