package org.example.storemanager.dto.response.catalog.inventory;

import org.springframework.beans.factory.annotation.Value;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface InventorySummaryProjection {
    Long getId();
    BigDecimal getQuantityPhysical();
    BigDecimal getQuantityAllocated();
    
    @Value("#{target.quantityPhysical - (target.quantityAllocated != null ? target.quantityAllocated : 0)}")
    BigDecimal getQuantityAvailable();

    @Value("#{target.warehouseZone.id}")
    Long getWarehouseZoneId();

    @Value("#{target.warehouseZone.zoneName}")
    String getWarehouseZoneName();

    @Value("#{target.warehouseZone.branch.id}")
    Long getBranchId();

    @Value("#{target.warehouseZone.branch.branchName}")
    String getBranchName();

    @Value("#{target.product.id}")
    Long getProductId();

    @Value("#{target.product.productCode}")
    String getProductCode();

    @Value("#{target.product.name}")
    String getProductName();

    @Value("#{target.size != null ? target.size.id : null}")
    Long getSizeId();

    @Value("#{target.size != null ? target.size.sizeCode : null}")
    String getSizeCode();

    @Value("#{target.size != null ? target.size.sizeName : null}")
    String getSizeName();

    @Value("#{target.color != null ? target.color.id : null}")
    Long getColorId();

    @Value("#{target.color != null ? target.color.colorCode : null}")
    String getColorCode();

    @Value("#{target.color != null ? target.color.colorName : null}")
    String getColorName();

    LocalDateTime getUpdatedAt();
}
