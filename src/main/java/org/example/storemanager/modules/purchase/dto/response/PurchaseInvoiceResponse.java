package org.example.storemanager.modules.purchase.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseInvoiceResponse {
    private Long id;
    private String invoiceCode;
    private String poCode;
    private Long poId;
    private Long supplierId;
    private String supplierName;
    private Long branchId;
    private String branchName;
    private LocalDateTime invoiceDate;
    private LocalDateTime dueDate;
    private BigDecimal subTotal;
    private BigDecimal vatAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingDebt;
    private String status;
    private String paymentTerms;
    private String note;
    private LocalDateTime createdAt;
    private String createdBy;
    private java.util.List<org.example.storemanager.modules.purchase.dto.PurchaseInvoiceItemDTO> items;
}
