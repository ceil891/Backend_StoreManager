package org.example.storemanager.modules.inventory.controller;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.inventory.dto.ProductBatchDTO;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.catalog.entity.SerialNumber;
import org.example.storemanager.modules.inventory.entity.InventoryBalance;
import org.example.storemanager.modules.inventory.entity.InventoryTransaction;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.catalog.repository.ProductRepository;
import org.example.storemanager.modules.catalog.repository.SerialNumberRepository;
import org.example.storemanager.modules.inventory.repository.InventoryBalanceRepository;
import org.example.storemanager.modules.inventory.repository.InventoryTransactionRepository;
import org.example.storemanager.modules.inventory.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/inventory", "/api/v1/inventories"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional(readOnly = true)
public class InventoryTrackingApiController {

    private final InventoryService inventoryService;
    private final SerialNumberRepository serialNumberRepository;
    private final ProductRepository productRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    // ==========================================
    // --- BATCH CRUD ---
    // ==========================================

    @GetMapping("/batches")
    public ResponseEntity<ApiResponse<List<ProductBatchDTO>>> getAllBatches() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAllProductBatches()));
    }

    @GetMapping("/batches/{id}")
    public ResponseEntity<ApiResponse<ProductBatchDTO>> getBatchById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getProductBatchById(id)));
    }

    @GetMapping("/batches/expiring")
    public ResponseEntity<ApiResponse<List<ProductBatchDTO>>> getExpiringBatches() {
        List<ProductBatchDTO> expiring = inventoryService.getAllProductBatches().stream()
                .filter(b -> b.getExpiryDate() != null && b.getExpiryDate().isBefore(java.time.LocalDate.now().plusMonths(3)))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(expiring));
    }

    @GetMapping("/variants/{variantId}/batches")
    public ResponseEntity<ApiResponse<List<ProductBatchDTO>>> getBatchesByVariant(@PathVariable Long variantId) {
        List<ProductBatchDTO> list = inventoryService.getAllProductBatches().stream()
                .filter(b -> b.getProductId() != null && b.getProductId().equals(variantId)) // Match variant/product ID
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/batches")
    public ResponseEntity<ApiResponse<ProductBatchDTO>> createBatch(@Valid @RequestBody ProductBatchDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(inventoryService.createProductBatch(dto)));
    }

    @PutMapping("/batches/{id}")
    public ResponseEntity<ApiResponse<ProductBatchDTO>> updateBatch(@PathVariable Long id, @Valid @RequestBody ProductBatchDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.updateProductBatch(id, dto)));
    }

    @DeleteMapping("/batches/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBatch(@PathVariable Long id) {
        inventoryService.deleteProductBatch(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ==========================================
    // --- SERIALS CRUD ---
    // ==========================================

    @GetMapping("/serials")
    public ResponseEntity<ApiResponse<List<SerialNumberDTO>>> getAllSerials() {
        List<SerialNumberDTO> list = serialNumberRepository.findAll().stream()
                .filter(sn -> !Boolean.TRUE.equals(sn.getIsDeleted()))
                .map(this::toSerialDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/serials/{serialNumber}")
    public ResponseEntity<ApiResponse<SerialNumberDTO>> getSerialByNum(@PathVariable String serialNumber) {
        SerialNumber sn = serialNumberRepository.findBySerialNumberAndIsDeletedFalse(serialNumber)
                .orElseThrow(() -> new ResourceNotFoundException("SerialNumber", "serialNumber", serialNumber));
        return ResponseEntity.ok(ApiResponse.ok(toSerialDTO(sn)));
    }

    @GetMapping("/serials/status/{status}")
    public ResponseEntity<ApiResponse<List<SerialNumberDTO>>> getSerialsByStatus(@PathVariable String status) {
        List<SerialNumberDTO> list = serialNumberRepository.findAll().stream()
                .filter(sn -> !Boolean.TRUE.equals(sn.getIsDeleted()) && status.equalsIgnoreCase(sn.getStatus()))
                .map(this::toSerialDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PatchMapping("/serials/{id}/status")
    public ResponseEntity<ApiResponse<SerialNumberDTO>> updateSerialStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        SerialNumber sn = serialNumberRepository.findById(id)
                .filter(s -> !Boolean.TRUE.equals(s.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("SerialNumber", "id", id));
        sn.setStatus(status);
        SerialNumber saved = serialNumberRepository.save(sn);
        return ResponseEntity.ok(ApiResponse.ok(toSerialDTO(saved)));
    }

    // ==========================================
    // --- BALANCES ---
    // ==========================================

    @GetMapping("/balances")
    public ResponseEntity<ApiResponse<List<InventoryBalanceDTO>>> getAllBalances() {
        List<InventoryBalanceDTO> list = inventoryBalanceRepository.findAll().stream()
                .filter(b -> !Boolean.TRUE.equals(b.getIsDeleted()))
                .map(this::toBalanceDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/balances/{id}")
    public ResponseEntity<ApiResponse<InventoryBalanceDTO>> getBalanceById(@PathVariable Long id) {
        InventoryBalance b = inventoryBalanceRepository.findById(id)
                .filter(x -> !Boolean.TRUE.equals(x.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("InventoryBalance", "id", id));
        return ResponseEntity.ok(ApiResponse.ok(toBalanceDTO(b)));
    }

    @GetMapping("/branches/{branchId}/inventory")
    public ResponseEntity<ApiResponse<List<InventoryBalanceDTO>>> getInventoryByBranch(@PathVariable Long branchId) {
        List<InventoryBalanceDTO> list = inventoryBalanceRepository.findAll().stream()
                .filter(b -> !Boolean.TRUE.equals(b.getIsDeleted()) && branchId.equals(b.getBranch().getId()))
                .map(this::toBalanceDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/variants/{variantId}/stock")
    public ResponseEntity<ApiResponse<List<InventoryBalanceDTO>>> getStockByVariant(@PathVariable Long variantId) {
        List<InventoryBalanceDTO> list = inventoryBalanceRepository.findAll().stream()
                .filter(b -> !Boolean.TRUE.equals(b.getIsDeleted()) && variantId.equals(b.getProductVariant().getId()))
                .map(this::toBalanceDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    // ==========================================
    // --- TRANSACTION LOGS ---
    // ==========================================

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<InventoryTransactionDTO>>> getTransactions(
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String transactionType) {
        List<InventoryTransactionDTO> list = inventoryTransactionRepository.findAll().stream()
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .filter(t -> variantId == null || variantId.equals(t.getProductVariant().getId()))
                .filter(t -> branchId == null || (t.getSourceBranch() != null && branchId.equals(t.getSourceBranch().getId())))
                .filter(t -> transactionType == null || (t.getTransactionType() != null && transactionType.equalsIgnoreCase(t.getTransactionType().name())))
                .map(this::toTransactionDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<ApiResponse<InventoryTransactionDTO>> getTransactionById(@PathVariable Long id) {
        InventoryTransaction t = inventoryTransactionRepository.findById(id)
                .filter(x -> !Boolean.TRUE.equals(x.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("InventoryTransaction", "id", id));
        return ResponseEntity.ok(ApiResponse.ok(toTransactionDTO(t)));
    }

    private SerialNumberDTO toSerialDTO(SerialNumber sn) {
        return SerialNumberDTO.builder()
                .id(sn.getId())
                .serialNumber(sn.getSerialNumber())
                .status(sn.getStatus())
                .productId(sn.getProduct() != null ? sn.getProduct().getId() : null)
                .productName(sn.getProduct() != null ? sn.getProduct().getName() : "")
                .productCode(sn.getProduct() != null ? sn.getProduct().getProductCode() : "")
                .importReceiptId(sn.getImportReceiptId())
                .build();
    }

    private InventoryBalanceDTO toBalanceDTO(InventoryBalance b) {
        Long productId = null;
        Long productVariantId = null;
        String sku = null;
        String productName = "";

        if (b.getProductVariant() != null) {
            productVariantId = b.getProductVariant().getId();
            sku = b.getProductVariant().getSku();
            if (b.getProductVariant().getProduct() != null) {
                productId = b.getProductVariant().getProduct().getId();
                productName = b.getProductVariant().getProduct().getName();
            }
        }

        BigDecimal avail = b.getAvailableQuantity() != null ? b.getAvailableQuantity() : BigDecimal.ZERO;
        BigDecimal resv = b.getReservedQuantity() != null ? b.getReservedQuantity() : BigDecimal.ZERO;

        return InventoryBalanceDTO.builder()
                .id(b.getId())
                .productId(productId)
                .productVariantId(productVariantId)
                .sku(sku)
                .productName(productName)
                .branchId(b.getBranch() != null ? b.getBranch().getId() : null)
                .branchName(b.getBranch() != null ? b.getBranch().getBranchName() : "")
                .availableQuantity(avail)
                .reservedQuantity(resv)
                .damagedQuantity(b.getDamagedQuantity() != null ? b.getDamagedQuantity() : BigDecimal.ZERO)
                .onHandQuantity(avail.add(resv))
                .lastUpdated(b.getLastUpdated())
                .build();
    }

    private InventoryTransactionDTO toTransactionDTO(InventoryTransaction t) {
        return InventoryTransactionDTO.builder()
                .id(t.getId())
                .transactionCode(t.getTransactionCode())
                .productVariantId(t.getProductVariant().getId())
                .sku(t.getProductVariant().getSku())
                .productName(t.getProductVariant().getProduct() != null ? t.getProductVariant().getProduct().getName() : "")
                .sourceBranchId(t.getSourceBranch() != null ? t.getSourceBranch().getId() : null)
                .sourceBranchName(t.getSourceBranch() != null ? t.getSourceBranch().getBranchName() : "")
                .transactionType(t.getTransactionType() != null ? t.getTransactionType().name() : "")
                .quantity(t.getQuantity())
                .beforeQuantity(t.getBeforeQuantity())
                .afterQuantity(t.getAfterQuantity())
                .createdAt(t.getCreatedAt())
                .build();
    }

    @Data
    @Builder
    public static class SerialNumberDTO {
        private Long id;
        private String serialNumber;
        private String status;
        private Long productId;
        private String productName;
        private String productCode;
        private Long importReceiptId;
    }

    @Data
    @Builder
    public static class InventoryBalanceDTO {
        private Long id;
        private Long productId;
        private Long productVariantId;
        private String sku;
        private String productName;
        private Long branchId;
        private String branchName;
        private BigDecimal availableQuantity;
        private BigDecimal reservedQuantity;
        private BigDecimal damagedQuantity;
        private BigDecimal onHandQuantity;
        private LocalDateTime lastUpdated;
    }

    @Data
    @Builder
    public static class InventoryTransactionDTO {
        private Long id;
        private String transactionCode;
        private Long productVariantId;
        private String sku;
        private String productName;
        private Long sourceBranchId;
        private String sourceBranchName;
        private String transactionType;
        private BigDecimal quantity;
        private BigDecimal beforeQuantity;
        private BigDecimal afterQuantity;
        private LocalDateTime createdAt;
    }
}
