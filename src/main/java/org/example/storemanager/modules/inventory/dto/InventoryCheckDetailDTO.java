package org.example.storemanager.modules.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCheckDetailDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private BigDecimal systemQty;
    private BigDecimal actualQty;
    private BigDecimal diffQty;
    private String reason;
}
