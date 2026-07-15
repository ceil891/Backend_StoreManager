package org.example.storemanager.dto.request.sales.exportinvoice;
import org.example.storemanager.enums.sales.OrderStatus;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateExportInvoiceRequest {
    private String invoiceCode;
    private LocalDateTime invoiceDate;
    private BigDecimal subTotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private Long customerId;
    private Long branchId;
    private Long posSessionId;

    // Chứa danh sách sản phẩm xuất bán
    private List<ExportInvoiceDetailRequest> details;
}