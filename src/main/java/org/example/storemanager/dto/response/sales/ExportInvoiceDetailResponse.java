package org.example.storemanager.dto.response.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportInvoiceDetailResponse {
    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private Long batchId;
    private String batchCode;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal subTotal;
}
