package org.example.storemanager.dto.response.sales.exportinvoice;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.example.storemanager.enums.sales.OrderStatus;

@Data
public class ExportInvoiceResponse {
    private Long id;
    private String invoiceCode;
    private LocalDateTime invoiceDate;
    private BigDecimal subTotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private Long customerId;
    private String customerName;
    private Long branchId;
    private Long posSessionId;

    private List<ExportInvoiceDetailResponse> details;

    private Boolean isActive;
}