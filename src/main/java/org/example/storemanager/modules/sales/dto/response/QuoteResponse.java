package org.example.storemanager.modules.sales.dto.response;

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
public class QuoteResponse {
    private Long id;
    private String quoteCode;
    private LocalDateTime quoteDate;
    private LocalDateTime validUntil;
    private Integer revision;
    private String currency;
    private String paymentTerms;
    private String deliveryTerms;
    private String warrantyTerms;
    private String validityTerms;
    private String shippingAddress;

    private BigDecimal subTotal;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;

    private String status;

    private Long customerId;
    private String customerName;

    private Long branchId;
    private String branchName;

    private Long warehouseId;
    private String warehouseName;

    private Long salesPersonId;
    private String salesPersonName;

    private String note;
    private String attachments;
    private String pdfUrl;

    private LocalDateTime createdAt;
    private String createdBy;

    private List<QuoteDetailResponse> details;
}
