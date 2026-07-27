package org.example.storemanager.modules.inventory.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferDetailDTO {
    private Long id;
    private Long productVariantId;
    private String productCode;
    private String productName;
    private BigDecimal transferQuantity;
}
