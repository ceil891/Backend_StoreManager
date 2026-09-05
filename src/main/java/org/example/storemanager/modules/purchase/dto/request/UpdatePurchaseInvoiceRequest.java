package org.example.storemanager.modules.purchase.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdatePurchaseInvoiceRequest {
    private String poCode;
    private Long poId;
    private Long supplierId;
    private Long branchId;
    private LocalDateTime invoiceDate;
    private LocalDateTime dueDate;
    private BigDecimal subTotal;
    private BigDecimal vatAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String status;
    private String paymentTerms;
    private String note;
    private java.util.List<org.example.storemanager.modules.purchase.dto.PurchaseInvoiceItemDTO> items;
}
