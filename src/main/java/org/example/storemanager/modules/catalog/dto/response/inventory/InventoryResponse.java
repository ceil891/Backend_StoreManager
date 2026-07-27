package org.example.storemanager.modules.catalog.dto.response.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private Long id;
    private Long warehouseZoneId;
    private String warehouseZoneName;
    private Long branchId;
    private String branchName;
    private Long productId;
    private String productCode;
    private String productName;
    private Long sizeId;
    private String sizeCode;
    private String sizeName;
    private Long colorId;
    private String colorCode;
    private String colorName;
    /** @deprecated Dùng quantityPhysical */
    @Deprecated
    private BigDecimal quantity;
    private BigDecimal quantityPhysical;
    private BigDecimal quantityAllocated;
    private BigDecimal quantityAvailable;
    private LocalDateTime lastUpdated;
}
