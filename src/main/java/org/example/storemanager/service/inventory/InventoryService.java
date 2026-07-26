package org.example.storemanager.service.inventory;

import org.example.storemanager.dto.request.catalog.inventory.InventoryAdjustRequest;
import org.example.storemanager.dto.request.catalog.inventory.SearchInventoryRequest;
import org.example.storemanager.dto.response.catalog.inventory.AdjustmentResponse;
import org.example.storemanager.dto.response.catalog.inventory.InventoryResponse;
import org.example.storemanager.dto.response.catalog.inventory.LowStockResponse;
import org.example.storemanager.dto.response.catalog.inventory.StockLedgerResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface InventoryService {
    PageResponse<InventoryResponse> searchInventories(SearchInventoryRequest request, Pageable pageable);
    List<InventoryResponse> getAllInventories();
    List<StockLedgerResponse> getStockLedger();
    List<LowStockResponse> getLowStock();
    AdjustmentResponse adjustStock(InventoryAdjustRequest request);
    InventoryResponse getInventory(Long id);

    /**
     * Trừ tồn kho vật lý (SizeInventory). Dùng cho combo DYNAMIC_VIRTUAL và POS.
     */
    AdjustmentResponse deductStock(Long warehouseZoneId, Long branchId, Long productId,
                                   Long sizeId, Long colorId, BigDecimal quantity,
                                   String reason, String referenceDocument, Long referenceId);

    /**
     * Cộng tồn kho vật lý (SizeInventory).
     */
    AdjustmentResponse addStock(Long warehouseZoneId, Long branchId, Long productId,
                                Long sizeId, Long colorId, BigDecimal quantity,
                                String reason, String referenceDocument, Long referenceId);

    // ImportReceipt methods
    List<org.example.storemanager.dto.inventory.ImportReceiptDTO> getAllImportReceipts();
    org.example.storemanager.dto.inventory.ImportReceiptDTO getImportReceiptById(Long id);
    org.example.storemanager.dto.inventory.ImportReceiptDTO createImportReceipt(org.example.storemanager.dto.inventory.ImportReceiptDTO dto);
    org.example.storemanager.dto.inventory.ImportReceiptDTO updateImportReceipt(Long id, org.example.storemanager.dto.inventory.ImportReceiptDTO dto);
    void deleteImportReceipt(Long id);
    org.example.storemanager.dto.inventory.ImportReceiptDTO completeImportReceipt(Long id);
    org.example.storemanager.dto.inventory.ImportReceiptDTO submitImportReceipt(Long id);
    org.example.storemanager.dto.inventory.ImportReceiptDTO approveImportReceipt(Long id);

    // ReturnToSupplier methods
    List<org.example.storemanager.dto.inventory.ReturnToSupplierDTO> getAllReturnToSuppliers();
    org.example.storemanager.dto.inventory.ReturnToSupplierDTO getReturnToSupplierById(Long id);
    org.example.storemanager.dto.inventory.ReturnToSupplierDTO createReturnToSupplier(org.example.storemanager.dto.inventory.ReturnToSupplierDTO dto);
    org.example.storemanager.dto.inventory.ReturnToSupplierDTO updateReturnToSupplier(Long id, org.example.storemanager.dto.inventory.ReturnToSupplierDTO dto);
    void deleteReturnToSupplier(Long id);
    org.example.storemanager.dto.inventory.ReturnToSupplierDTO approveReturnToSupplier(Long id, org.example.storemanager.dto.request.catalog.inventory.ReturnApprovalRequest request);
    org.example.storemanager.dto.inventory.ReturnToSupplierDTO rejectReturnToSupplier(Long id, org.example.storemanager.dto.request.catalog.inventory.ReturnRejectRequest request);

    // CancelIssue methods
    List<org.example.storemanager.dto.inventory.CancelIssueDTO> getAllCancelIssues();
    org.example.storemanager.dto.inventory.CancelIssueDTO getCancelIssueById(Long id);
    org.example.storemanager.dto.inventory.CancelIssueDTO createCancelIssue(org.example.storemanager.dto.inventory.CancelIssueDTO dto);
    org.example.storemanager.dto.inventory.CancelIssueDTO updateCancelIssue(Long id, org.example.storemanager.dto.inventory.CancelIssueDTO dto);
    void deleteCancelIssue(Long id);

    // StockTransfer methods
    List<org.example.storemanager.dto.inventory.StockTransferDTO> getAllStockTransfers();
    org.example.storemanager.dto.inventory.StockTransferDTO getStockTransferById(Long id);
    org.example.storemanager.dto.inventory.StockTransferDTO createStockTransfer(org.example.storemanager.dto.inventory.StockTransferDTO dto);
    org.example.storemanager.dto.inventory.StockTransferDTO updateStockTransfer(Long id, org.example.storemanager.dto.inventory.StockTransferDTO dto);
    void deleteStockTransfer(Long id);

    // ProductBatch methods
    List<org.example.storemanager.dto.inventory.ProductBatchDTO> getAllProductBatches();
    org.example.storemanager.dto.inventory.ProductBatchDTO getProductBatchById(Long id);
    org.example.storemanager.dto.inventory.ProductBatchDTO createProductBatch(org.example.storemanager.dto.inventory.ProductBatchDTO dto);
    org.example.storemanager.dto.inventory.ProductBatchDTO updateProductBatch(Long id, org.example.storemanager.dto.inventory.ProductBatchDTO dto);
    void deleteProductBatch(Long id);

    // Missing actions
    org.example.storemanager.dto.inventory.ImportReceiptDTO cancelImportReceipt(Long id, org.example.storemanager.dto.request.catalog.inventory.ImportCancelRequest request);
    org.example.storemanager.dto.inventory.CancelIssueDTO approveCancelIssue(Long id, org.example.storemanager.dto.request.catalog.inventory.CancelIssueApprovalRequest request);
    org.example.storemanager.dto.inventory.CancelIssueDTO rejectCancelIssue(Long id, org.example.storemanager.dto.request.catalog.inventory.CancelIssueRejectRequest request);
    org.example.storemanager.dto.inventory.StockTransferDTO completeStockTransfer(Long id, org.example.storemanager.dto.request.catalog.inventory.TransferCompleteRequest request);
    org.example.storemanager.dto.inventory.StockTransferDTO cancelStockTransfer(Long id, org.example.storemanager.dto.request.catalog.inventory.TransferCancelRequest request);
    
    // Missing Batch Actions
    org.example.storemanager.dto.inventory.ProductBatchDTO adjustProductBatch(Long id, org.example.storemanager.dto.request.catalog.inventory.BatchAdjustRequest request);
    org.example.storemanager.dto.inventory.ProductBatchDTO expireProductBatch(Long id);
    List<org.example.storemanager.dto.inventory.ProductBatchDTO> getExpiringProductBatches(int days);

    // InventoryCheck (Adjustments) methods
    List<org.example.storemanager.dto.inventory.InventoryCheckDTO> getAllInventoryChecks();
    org.example.storemanager.dto.inventory.InventoryCheckDTO getInventoryCheckById(Long id);
    org.example.storemanager.dto.inventory.InventoryCheckDTO createInventoryCheck(org.example.storemanager.dto.inventory.InventoryCheckDTO dto);
    org.example.storemanager.dto.inventory.InventoryCheckDTO updateInventoryCheck(Long id, org.example.storemanager.dto.inventory.InventoryCheckDTO dto);
    void deleteInventoryCheck(Long id);
    org.example.storemanager.dto.inventory.InventoryCheckDTO approveInventoryCheck(Long id);

    org.example.storemanager.dto.inventory.ReturnToSupplierDTO submitReturnToSupplier(Long id);
    org.example.storemanager.dto.inventory.ReturnToSupplierDTO executeReturnToSupplier(Long id);
    org.example.storemanager.dto.inventory.ReturnToSupplierDTO cancelReturnToSupplier(Long id);

    org.example.storemanager.dto.inventory.CancelIssueDTO submitCancelIssue(Long id);
    org.example.storemanager.dto.inventory.CancelIssueDTO executeCancelIssue(Long id);
    org.example.storemanager.dto.inventory.CancelIssueDTO cancelCancelIssue(Long id);

    org.example.storemanager.dto.inventory.StockTransferDTO submitStockTransfer(Long id);
    org.example.storemanager.dto.inventory.StockTransferDTO approveStockTransfer(Long id);
    org.example.storemanager.dto.inventory.StockTransferDTO shipStockTransfer(Long id);
}
