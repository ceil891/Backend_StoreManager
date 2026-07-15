package org.example.storemanager.dto.response.sales.exportinvoice;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExportInvoiceDetailResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long batchId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal subTotal;
}