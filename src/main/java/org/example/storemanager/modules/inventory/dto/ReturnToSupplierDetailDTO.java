package org.example.storemanager.modules.inventory.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnToSupplierDetailDTO {
    private Long id;
    private Long productVariantId;
    private String productName;
    private String sku;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal subTotal;
}
