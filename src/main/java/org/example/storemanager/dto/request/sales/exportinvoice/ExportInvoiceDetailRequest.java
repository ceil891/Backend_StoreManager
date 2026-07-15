package org.example.storemanager.dto.request.sales.exportinvoice;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExportInvoiceDetailRequest {
    private Long productId;
    private Long batchId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal subTotal;
}