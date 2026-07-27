package org.example.storemanager.modules.catalog.dto.response.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustmentResponse {
    private Long inventoryId;
    private BigDecimal oldQuantity;
    private BigDecimal newQuantity;
    private BigDecimal changeQty;
    private String transactionType; // ADJUSTMENT
    private String reason;
}
