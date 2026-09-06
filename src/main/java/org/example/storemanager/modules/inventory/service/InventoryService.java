package org.example.storemanager.modules.inventory.service;

import org.example.storemanager.modules.catalog.dto.request.inventory.InventoryAdjustRequest;
import org.example.storemanager.modules.catalog.dto.request.inventory.SearchInventoryRequest;
import org.example.storemanager.modules.catalog.dto.response.inventory.AdjustmentResponse;
import org.example.storemanager.modules.catalog.dto.response.inventory.InventoryResponse;
import org.example.storemanager.modules.catalog.dto.response.inventory.LowStockResponse;
import org.example.storemanager.modules.catalog.dto.response.inventory.StockLedgerResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface InventoryService {
    PageResponse<InventoryResponse> searchInventories(SearchInventoryRequest request, Pageable pageable);
    List<InventoryResponse> getAllInventories();
    default List<InventoryResponse> getAllInventories(Long branchId) {
        return getAllInventories();
    }
    List<StockLedgerResponse> getStockLedger();
    default List<StockLedgerResponse> getStockLedger(Long branchId) {
        return getStockLedger();
    }
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
    List<org.example.storemanager.modules.inventory.dto.ImportReceiptDTO> getAllImportReceipts();
    org.example.storemanager.modules.inventory.dto.ImportReceiptDTO getImportReceiptById(Long id);
    org.example.storemanager.modules.inventory.dto.ImportReceiptDTO createImportReceipt(org.example.storemanager.modules.inventory.dto.ImportReceiptDTO dto);
    org.example.storemanager.modules.inventory.dto.ImportReceiptDTO updateImportReceipt(Long id, org.example.storemanager.modules.inventory.dto.ImportReceiptDTO dto);
    void deleteImportReceipt(Long id);
    org.example.storemanager.modules.inventory.dto.ImportReceiptDTO completeImportReceipt(Long id);
    org.example.storemanager.modules.inventory.dto.ImportReceiptDTO submitImportReceipt(Long id);
    org.example.storemanager.modules.inventory.dto.ImportReceiptDTO approveImportReceipt(Long id);

    // ReturnToSupplier methods
    List<org.example.storemanager.modules.inventory.dto.ReturnToSupplierDTO> getAllReturnToSuppliers();
    org.example.storemanager.modules.inventory.dto.ReturnToSupplierDTO getReturnToSupplierById(Long id);
    org.example.storemanager.modules.inventory.dto.ReturnToSupplierDTO createReturnToSupplier(org.example.storemanager.modules.inventory.dto.ReturnToSupplierDTO dto);
    org.example.storemanager.modules.inventory.dto.ReturnToSupplierDTO updateReturnToSupplier(Long id, org.example.storemanager.modules.inventory.dto.ReturnToSupplierDTO dto);
    void deleteReturnToSupplier(Long id);
    org.example.storemanager.modules.inventory.dto.ReturnToSupplierDTO approveReturnToSupplier(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.ReturnApprovalRequest request);
    org.example.storemanager.modules.inventory.dto.ReturnToSupplierDTO rejectReturnToSupplier(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.ReturnRejectRequest request);

    // CancelIssue methods
    List<org.example.storemanager.modules.inventory.dto.CancelIssueDTO> getAllCancelIssues();
    org.example.storemanager.modules.inventory.dto.CancelIssueDTO getCancelIssueById(Long id);
    org.example.storemanager.modules.inventory.dto.CancelIssueDTO createCancelIssue(org.example.storemanager.modules.inventory.dto.CancelIssueDTO dto);
    org.example.storemanager.modules.inventory.dto.CancelIssueDTO updateCancelIssue(Long id, org.example.storemanager.modules.inventory.dto.CancelIssueDTO dto);
    void deleteCancelIssue(Long id);

    // StockTransfer methods
    List<org.example.storemanager.modules.inventory.dto.StockTransferDTO> getAllStockTransfers();
    org.example.storemanager.modules.inventory.dto.StockTransferDTO getStockTransferById(Long id);
    org.example.storemanager.modules.inventory.dto.StockTransferDTO createStockTransfer(org.example.storemanager.modules.inventory.dto.StockTransferDTO dto);
    org.example.storemanager.modules.inventory.dto.StockTransferDTO updateStockTransfer(Long id, org.example.storemanager.modules.inventory.dto.StockTransferDTO dto);
    void deleteStockTransfer(Long id);

    // ProductBatch methods
    List<org.example.storemanager.modules.inventory.dto.ProductBatchDTO> getAllProductBatches();
    org.example.storemanager.modules.inventory.dto.ProductBatchDTO getProductBatchById(Long id);
    org.example.storemanager.modules.inventory.dto.ProductBatchDTO createProductBatch(org.example.storemanager.modules.inventory.dto.ProductBatchDTO dto);
    org.example.storemanager.modules.inventory.dto.ProductBatchDTO updateProductBatch(Long id, org.example.storemanager.modules.inventory.dto.ProductBatchDTO dto);
    void deleteProductBatch(Long id);

    // Missing actions
    org.example.storemanager.modules.inventory.dto.ImportReceiptDTO cancelImportReceipt(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.ImportCancelRequest request);
    org.example.storemanager.modules.inventory.dto.CancelIssueDTO approveCancelIssue(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.CancelIssueApprovalRequest request);
    org.example.storemanager.modules.inventory.dto.CancelIssueDTO rejectCancelIssue(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.CancelIssueRejectRequest request);
    org.example.storemanager.modules.inventory.dto.StockTransferDTO completeStockTransfer(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.TransferCompleteRequest request);
    org.example.storemanager.modules.inventory.dto.StockTransferDTO cancelStockTransfer(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.TransferCancelRequest request);
    
    // Missing Batch Actions
    org.example.storemanager.modules.inventory.dto.ProductBatchDTO adjustProductBatch(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.BatchAdjustRequest request);
    org.example.storemanager.modules.inventory.dto.ProductBatchDTO expireProductBatch(Long id);
    List<org.example.storemanager.modules.inventory.dto.ProductBatchDTO> getExpiringProductBatches(int days);

    // InventoryCheck (Adjustments) methods
    List<org.example.storemanager.modules.inventory.dto.InventoryCheckDTO> getAllInventoryChecks();
    org.example.storemanager.modules.inventory.dto.InventoryCheckDTO getInventoryCheckById(Long id);
    org.example.storemanager.modules.inventory.dto.InventoryCheckDTO createInventoryCheck(org.example.storemanager.modules.inventory.dto.InventoryCheckDTO dto);
    org.example.storemanager.modules.inventory.dto.InventoryCheckDTO updateInventoryCheck(Long id, org.example.storemanager.modules.inventory.dto.InventoryCheckDTO dto);
    void deleteInventoryCheck(Long id);
    org.example.storemanager.modules.inventory.dto.InventoryCheckDTO approveInventoryCheck(Long id);

    org.example.storemanager.modules.inventory.dto.ReturnToSupplierDTO submitReturnToSupplier(Long id);
    org.example.storemanager.modules.inventory.dto.ReturnToSupplierDTO executeReturnToSupplier(Long id);
    org.example.storemanager.modules.inventory.dto.ReturnToSupplierDTO cancelReturnToSupplier(Long id);

    org.example.storemanager.modules.inventory.dto.CancelIssueDTO submitCancelIssue(Long id);
    org.example.storemanager.modules.inventory.dto.CancelIssueDTO executeCancelIssue(Long id);
    org.example.storemanager.modules.inventory.dto.CancelIssueDTO cancelCancelIssue(Long id);

    org.example.storemanager.modules.inventory.dto.StockTransferDTO submitStockTransfer(Long id);
    org.example.storemanager.modules.inventory.dto.StockTransferDTO approveStockTransfer(Long id);
    org.example.storemanager.modules.inventory.dto.StockTransferDTO shipStockTransfer(Long id);

    // StockOut methods
    List<org.example.storemanager.modules.inventory.dto.StockOutDTO> getAllStockOuts();
    org.example.storemanager.modules.inventory.dto.StockOutDTO getStockOutById(Long id);
    org.example.storemanager.modules.inventory.dto.StockOutDTO createStockOut(org.example.storemanager.modules.inventory.dto.StockOutDTO dto);
    org.example.storemanager.modules.inventory.dto.StockOutDTO updateStockOut(Long id, org.example.storemanager.modules.inventory.dto.StockOutDTO dto);
    void deleteStockOut(Long id);

    // TransferShipment methods
    List<org.example.storemanager.modules.inventory.dto.TransferShipmentDTO> getAllTransferShipments();
    org.example.storemanager.modules.inventory.dto.TransferShipmentDTO updateTransferShipmentStatus(Long id, String status);
}
