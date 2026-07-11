package org.example.storemanager.service.inventory;

import org.example.storemanager.dto.request.catalog.inventory.InventoryAdjustRequest;
import org.example.storemanager.dto.request.catalog.inventory.SearchInventoryRequest;
import org.example.storemanager.dto.response.catalog.inventory.AdjustmentResponse;
import org.example.storemanager.dto.response.catalog.inventory.InventoryResponse;
import org.example.storemanager.dto.response.catalog.inventory.LowStockResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface InventoryService {
    PageResponse<InventoryResponse> searchInventories(SearchInventoryRequest request, Pageable pageable);
    List<LowStockResponse> getLowStock();
    AdjustmentResponse adjustStock(InventoryAdjustRequest request);
    InventoryResponse getInventory(Long id);

    /**
     * Trừ tồn kho vật lý (SizeInventory). Dùng cho combo DYNAMIC_VIRTUAL và POS.
     */
    AdjustmentResponse deductStock(Long warehouseZoneId, Long branchId, Long productId,
                                   Long sizeId, Long colorId, BigDecimal quantity,
                                   String reason, String referenceDocument, Long referenceId);
}
