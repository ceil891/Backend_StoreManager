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
public class LowStockResponse {
    private Long productId;
    private String productCode;
    private String productName;
    private Long branchId;
    private String branchName;
    private Long warehouseZoneId;
    private String warehouseZoneName;
    private BigDecimal currentQuantity;
    private BigDecimal minStock;
    private BigDecimal shortage; // minStock - currentQuantity (if positive)
}
