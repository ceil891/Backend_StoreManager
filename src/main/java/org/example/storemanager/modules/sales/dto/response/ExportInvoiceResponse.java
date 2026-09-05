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
public class ExportInvoiceResponse {
    private Long id;
    private String invoiceCode;
    private LocalDateTime invoiceDate;
    private BigDecimal subTotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingDebt;
    private String status;
    private Long customerId;
    private String customerName;
    private Long branchId;
    private String branchName;
    private Long posSessionId;
    private String taxId;
    private String companyName;
    private LocalDateTime dueDate;
    private String paymentTerms;
    private String einvoiceRef;
    private String note;
    private LocalDateTime createdAt;
    private String createdBy;
    private List<ExportInvoiceDetailResponse> details;
}
