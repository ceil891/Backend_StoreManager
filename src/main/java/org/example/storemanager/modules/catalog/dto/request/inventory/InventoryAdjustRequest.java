package org.example.storemanager.modules.catalog.dto.request.inventory;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustRequest {
    @Min(1)
    private Long warehouseZoneId;
    /** @deprecated Dùng warehouseZoneId. Giữ để tương thích — sẽ resolve zone mặc định của branch. */
    @Deprecated
    private Long branchId;
    @Min(1)
    private Long productId;
    private Long sizeId; // optional
    private Long colorId; // optional
    private BigDecimal actualQty;
    private String reason;
}
