package org.example.storemanager.modules.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockOutDetailDTO {
    private Long id;
    private String productName;
    private String variant;
    private String sku;
    private String barcode;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
}
