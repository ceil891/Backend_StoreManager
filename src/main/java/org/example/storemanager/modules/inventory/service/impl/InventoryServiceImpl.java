package org.example.storemanager.modules.inventory.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.modules.catalog.dto.request.inventory.InventoryAdjustRequest;
import org.example.storemanager.modules.catalog.dto.request.inventory.SearchInventoryRequest;
import org.example.storemanager.modules.catalog.dto.response.inventory.AdjustmentResponse;
import org.example.storemanager.modules.catalog.dto.response.inventory.InventoryResponse;
import org.example.storemanager.modules.catalog.dto.response.inventory.LowStockResponse;
import org.example.storemanager.modules.catalog.dto.response.inventory.InventorySummaryProjection;
import org.example.storemanager.modules.catalog.dto.response.inventory.StockLedgerResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.inventory.dto.*;
import org.example.storemanager.modules.catalog.entity.Color;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.catalog.entity.Size;
import org.example.storemanager.modules.catalog.entity.ProductVariant;
import org.example.storemanager.modules.catalog.entity.SerialNumber;
import org.example.storemanager.modules.inventory.entity.*;
import org.example.storemanager.modules.wms.entity.WarehouseZone;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.partnerarea.entity.Supplier;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.shared.exception.BusinessException;
import org.example.storemanager.shared.enums.ErrorCode;
import org.example.storemanager.shared.enums.inventory.*;
import org.example.storemanager.modules.wms.entity.ProductLocation;
import java.time.format.DateTimeFormatter;
import org.example.storemanager.modules.catalog.repository.ColorRepository;
import org.example.storemanager.modules.catalog.repository.ProductRepository;
import org.example.storemanager.modules.catalog.repository.SizeRepository;
import org.example.storemanager.modules.catalog.repository.ProductVariantRepository;
import org.example.storemanager.modules.partnerarea.repository.SupplierRepository;
import org.example.storemanager.modules.system.repository.BranchRepository;
import org.example.storemanager.modules.inventory.repository.*;
import org.example.storemanager.modules.wms.repository.WarehouseZoneRepository;
import org.example.storemanager.modules.wms.entity.WarehouseBin;
import org.example.storemanager.modules.wms.repository.WarehouseBinRepository;
import org.example.storemanager.modules.inventory.service.InventoryService;
import org.example.storemanager.modules.wms.service.WarehouseService;
import org.example.storemanager.shared.config.LogActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryServiceImpl implements InventoryService {

    private final SizeInventoryRepository sizeInventoryRepository;
    private final StockLedgerRepository stockLedgerRepository;
    private final ProductRepository productRepository;
    private final WarehouseZoneRepository warehouseZoneRepository;
    private final WarehouseService warehouseService;
    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;

    private final ImportReceiptRepository importReceiptRepository;
    private final ImportReceiptDetailRepository importReceiptDetailRepository;
    private final ReturnToSupplierRepository returnToSupplierRepository;
    private final ReturnToSupplierDetailRepository returnToSupplierDetailRepository;
    private final CancelIssueRepository cancelIssueRepository;
    private final CancelIssueDetailRepository cancelIssueDetailRepository;
    private final StockTransferRepository stockTransferRepository;
    private final StockTransferDetailRepository stockTransferDetailRepository;
    private final ProductBatchRepository productBatchRepository;
    private final ProductVariantRepository productVariantRepository;
    private final SupplierRepository supplierRepository;
    private final BranchRepository branchRepository;
    private final InventoryCheckRepository inventoryCheckRepository;
    private final InventoryCheckDetailRepository inventoryCheckDetailRepository;
    private final WarehouseBinRepository warehouseBinRepository;
    private final org.example.storemanager.modules.wms.repository.ProductLocationRepository productLocationRepository;
    private final org.example.storemanager.modules.inventory.repository.InventoryBalanceRepository inventoryBalanceRepository;
    private final org.example.storemanager.modules.inventory.repository.InventoryTransactionRepository inventoryTransactionRepository;
    private final org.example.storemanager.modules.catalog.repository.SerialNumberRepository serialNumberRepository;
    private final org.example.storemanager.modules.sales.repository.PurchaseOrderRepository purchaseOrderRepository;
    private final org.example.storemanager.modules.sales.repository.PurchaseOrderDetailRepository purchaseOrderDetailRepository;
    private final StockOutRepository stockOutRepository;
    private final org.example.storemanager.modules.inventory.mapper.StockOutMapper stockOutMapper;

    @Override
    public PageResponse<InventoryResponse> searchInventories(SearchInventoryRequest request, Pageable pageable) {
        Page<InventorySummaryProjection> page = sizeInventoryRepository.searchInventory(
                request.getProductId(),
                request.getCategoryId(),
                request.getDepartmentId(),
                request.getBranchId(),
                request.getWarehouseZoneId(),
                request.getSize(),
                request.getColor(),
                request.getSearch(),
                pageable);
        List<InventoryResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return PageResponse.<InventoryResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    public List<InventoryResponse> getAllInventories() {
        return getAllInventories(null);
    }

    @Override
    public List<InventoryResponse> getAllInventories(Long branchId) {
        List<SizeInventory> list = sizeInventoryRepository.findAllWithAssociations();
        if (branchId != null) {
            list = list.stream()
                    .filter(si -> si.getWarehouseZone() != null && si.getWarehouseZone().getBranch() != null
                            && branchId.equals(si.getWarehouseZone().getBranch().getId()))
                    .collect(Collectors.toList());
        }
        return list.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockLedgerResponse> getStockLedger() {
        return getStockLedger(null);
    }

    @Override
    public List<StockLedgerResponse> getStockLedger(Long branchId) {
        List<StockLedger> list = stockLedgerRepository.findAllWithProductAndBranch();
        if (branchId != null) {
            list = list.stream()
                    .filter(sl -> sl.getBranch() != null && branchId.equals(sl.getBranch().getId()))
                    .collect(Collectors.toList());
        }
        return list.stream()
                .map(this::toLedgerResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LowStockResponse> getLowStock() {
        return sizeInventoryRepository.findLowStock().stream()
                .map(inv -> {
                    BigDecimal available = inv.getQuantityAvailable();
                    return LowStockResponse.builder()
                            .productId(inv.getProduct().getId())
                            .productCode(inv.getProduct().getProductCode())
                            .productName(inv.getProduct().getName())
                            .branchId(inv.getWarehouseZone().getBranch().getId())
                            .branchName(inv.getWarehouseZone().getBranch().getBranchName())
                            .warehouseZoneId(inv.getWarehouseZone().getId())
                            .warehouseZoneName(inv.getWarehouseZone().getZoneName())
                            .currentQuantity(available)
                            .minStock(inv.getProduct().getMinStock())
                            .shortage(inv.getProduct().getMinStock() != null ?
                                    inv.getProduct().getMinStock().subtract(available) : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @LogActivity(actionType = "ADJUST", entityName = "SizeInventory", entityClass = SizeInventory.class)
    public AdjustmentResponse adjustStock(InventoryAdjustRequest request) {
        WarehouseZone zone = resolveWarehouseZone(request);

        Product product = productRepository.findByIdAndIsDeletedFalse(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        final Size sizeEntity = request.getSizeId() != null
                ? sizeRepository.findById(request.getSizeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Size", "id", request.getSizeId()))
                : null;
        final Color colorEntity = request.getColorId() != null
                ? colorRepository.findById(request.getColorId())
                        .orElseThrow(() -> new ResourceNotFoundException("Color", "id", request.getColorId()))
                : null;

        Long sizeId = sizeEntity != null ? sizeEntity.getId() : null;
        Long colorId = colorEntity != null ? colorEntity.getId() : null;

        SizeInventory inventory = sizeInventoryRepository
                .findAndLockBySkuAttributes(zone.getId(), product.getId(), sizeId, colorId)
                .orElseGet(() -> SizeInventory.builder()
                        .warehouseZone(zone)
                        .product(product)
                        .size(sizeEntity)
                        .color(colorEntity)
                        .quantityPhysical(BigDecimal.ZERO)
                        .quantityAllocated(BigDecimal.ZERO)
                        .isActive(true)
                        .build());

        BigDecimal oldQty = inventory.getQuantityPhysical();
        BigDecimal newQty = request.getActualQty();
        BigDecimal diff = newQty.subtract(oldQty);
        inventory.setQuantityPhysical(newQty);
        sizeInventoryRepository.save(inventory);

        StockLedger ledger = StockLedger.builder()
                .transactionType("ADJUSTMENT")
                .referenceId(inventory.getId())
                .changeQty(diff)
                .balanceAfter(newQty)
                .branch(zone.getBranch())
                .warehouseZone(zone)
                .product(product)
                .build();
        stockLedgerRepository.save(ledger);

        // Also update InventoryBalance and record InventoryTransaction
        if (zone.getBranch() != null) {
            ProductVariant variant = productVariantRepository.findByProductIdAndIsDeletedFalse(product.getId()).stream().findFirst().orElse(null);
            if (variant != null) {
                InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(variant.getId(), zone.getBranch().getId())
                        .orElseGet(() -> InventoryBalance.builder()
                                .productVariant(variant)
                                .branch(zone.getBranch())
                                .availableQuantity(BigDecimal.ZERO)
                                .reservedQuantity(BigDecimal.ZERO)
                                .damagedQuantity(BigDecimal.ZERO)
                                .build());
                BigDecimal balBefore = balance.getAvailableQuantity() != null ? balance.getAvailableQuantity() : BigDecimal.ZERO;
                BigDecimal balAfter = balBefore.add(diff).max(BigDecimal.ZERO);
                balance.setAvailableQuantity(balAfter);
                balance.setLastUpdated(LocalDateTime.now());
                inventoryBalanceRepository.save(balance);

                String txCode = "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                        + "-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4);
                InventoryTransaction tx = InventoryTransaction.builder()
                        .transactionCode(txCode)
                        .productVariant(variant)
                        .sourceBranch(zone.getBranch())
                        .transactionType(InventoryTransactionType.ADJUSTMENT)
                        .quantity(diff.abs())
                        .beforeQuantity(balBefore)
                        .afterQuantity(balAfter)
                        .build();
                tx.setIsDeleted(false);
                tx.setCreatedBy(getCurrentUsername());
                inventoryTransactionRepository.save(tx);
            }
        }

        return AdjustmentResponse.builder()
                .inventoryId(inventory.getId())
                .oldQuantity(oldQty)
                .newQuantity(newQty)
                .changeQty(diff)
                .transactionType("ADJUSTMENT")
                .reason(request.getReason())
                .build();
    }

    @Override
    public InventoryResponse getInventory(Long id) {
        SizeInventory inv = sizeInventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SizeInventory", "id", id));
        return toResponse(inv);
    }

    @Override
    @Transactional
    @LogActivity(actionType = "DEDUCT", entityName = "SizeInventory", entityClass = SizeInventory.class)
    public AdjustmentResponse deductStock(Long warehouseZoneId, Long branchId, Long productId,
                                            Long sizeId, Long colorId, BigDecimal quantity,
                                            String reason, String referenceDocument, Long referenceId) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "quantity phải lớn hơn 0");
        }

        InventoryAdjustRequest adjustRequest = InventoryAdjustRequest.builder()
                .warehouseZoneId(warehouseZoneId)
                .branchId(branchId)
                .productId(productId)
                .sizeId(sizeId)
                .colorId(colorId)
                .build();

        WarehouseZone zone = resolveWarehouseZone(adjustRequest);
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        final Size sizeEntity = sizeId != null
                ? sizeRepository.findById(sizeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Size", "id", sizeId))
                : null;
        final Color colorEntity = colorId != null
                ? colorRepository.findById(colorId)
                        .orElseThrow(() -> new ResourceNotFoundException("Color", "id", colorId))
                : null;

        Long resolvedSizeId = sizeEntity != null ? sizeEntity.getId() : null;
        Long resolvedColorId = colorEntity != null ? colorEntity.getId() : null;

        SizeInventory inventory = sizeInventoryRepository
                .findAndLockBySkuAttributes(zone.getId(), product.getId(), resolvedSizeId, resolvedColorId)
                .orElseGet(() -> {
                    SizeInventory inv = SizeInventory.builder()
                            .warehouseZone(zone)
                            .product(product)
                            .size(sizeEntity)
                            .color(colorEntity)
                            .quantityPhysical(quantity)
                            .quantityAllocated(BigDecimal.ZERO)
                            .build();
                    inv.setIsDeleted(false);
                    return sizeInventoryRepository.save(inv);
                });

        BigDecimal oldQty = inventory.getQuantityPhysical() != null ? inventory.getQuantityPhysical() : BigDecimal.ZERO;
        BigDecimal newQty = oldQty.subtract(quantity);
        if (newQty.compareTo(BigDecimal.ZERO) < 0) {
            newQty = BigDecimal.ZERO;
        }

        inventory.setQuantityPhysical(newQty);
        sizeInventoryRepository.save(inventory);

        StockLedger ledger = StockLedger.builder()
                .transactionType(reason != null ? reason : "DEDUCT")
                .referenceId(referenceId != null ? referenceId : inventory.getId())
                .changeQty(quantity.negate())
                .balanceAfter(newQty)
                .branch(zone.getBranch())
                .warehouseZone(zone)
                .product(product)
                .build();
        if (referenceDocument != null) {
            ledger.setNote(referenceDocument);
        }
        stockLedgerRepository.save(ledger);

        // Đồng bộ trừ tồn kho trong InventoryBalance và tạo InventoryTransaction type SALE
        List<ProductVariant> pvList = productVariantRepository.findByProductIdAndIsDeletedFalse(product.getId());
        if (!pvList.isEmpty()) {
            ProductVariant variant = pvList.get(0);
            InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(variant.getId(), zone.getBranch().getId())
                    .orElseGet(() -> {
                        InventoryBalance ib = InventoryBalance.builder()
                            .productVariant(variant)
                            .branch(zone.getBranch())
                            .availableQuantity(BigDecimal.ZERO)
                            .reservedQuantity(BigDecimal.ZERO)
                            .damagedQuantity(BigDecimal.ZERO)
                            .build();
                        ib.setIsDeleted(false);
                        return inventoryBalanceRepository.save(ib);
                    });
            BigDecimal beforeQty = balance.getAvailableQuantity() != null ? balance.getAvailableQuantity() : BigDecimal.ZERO;
            BigDecimal afterQty = beforeQty.subtract(quantity);
            if (afterQty.compareTo(BigDecimal.ZERO) < 0) {
                afterQty = BigDecimal.ZERO;
            }
            balance.setAvailableQuantity(afterQty);
            balance.setLastUpdated(LocalDateTime.now());
            inventoryBalanceRepository.save(balance);

            String txCode = "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + "-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4);

            InventoryTransaction tx = InventoryTransaction.builder()
                    .transactionCode(txCode)
                    .productVariant(variant)
                    .sourceBranch(zone.getBranch())
                    .transactionType(InventoryTransactionType.SALE)
                    .quantity(quantity)
                    .beforeQuantity(beforeQty)
                    .afterQuantity(afterQty)
                    .build();
            tx.setIsDeleted(false);
            tx.setCreatedBy(getCurrentUsername());
            inventoryTransactionRepository.save(tx);
        }

        return AdjustmentResponse.builder()
                .inventoryId(inventory.getId())
                .oldQuantity(oldQty)
                .newQuantity(newQty)
                .changeQty(quantity.negate())
                .transactionType(reason != null ? reason : "DEDUCT")
                .reason(referenceDocument)
                .build();
    }

    private WarehouseZone resolveWarehouseZone(InventoryAdjustRequest request) {
        if (request.getWarehouseZoneId() != null) {
            return warehouseZoneRepository.findByIdAndIsDeletedFalse(request.getWarehouseZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("WarehouseZone", "id", request.getWarehouseZoneId()));
        }
        if (request.getBranchId() != null) {
            return warehouseService.getOrCreateDefaultZone(request.getBranchId());
        }
        throw new ResourceNotFoundException("WarehouseZone", "id", "warehouseZoneId or branchId required");
    }

    private String buildInsufficientStockMessage(String productName, String zoneName,
                                                 BigDecimal requiredQty, BigDecimal availableQty) {
        if (availableQty == null) {
            return String.format(
                    "Mặt hàng thành phần [%s] trong Combo hiện tại chưa từng được nhập kho tại Khu vực [%s]. Vui lòng kiểm tra lại phiếu nhập!",
                    productName, zoneName);
        }
        return String.format(
                "Mặt hàng [%s] tại Khu vực [%s] không đủ tồn (cần %s, hiện có %s). Vui lòng kiểm tra lại phiếu nhập!",
                productName, zoneName, requiredQty.toPlainString(), availableQty.toPlainString());
    }

    private InventoryResponse toResponse(SizeInventory inv) {
        BigDecimal physical = inv.getQuantityPhysical();
        BigDecimal allocated = inv.getQuantityAllocated() != null ? inv.getQuantityAllocated() : BigDecimal.ZERO;
        BigDecimal available = inv.getQuantityAvailable();
        return InventoryResponse.builder()
                .id(inv.getId())
                .warehouseZoneId(inv.getWarehouseZone().getId())
                .warehouseZoneName(inv.getWarehouseZone().getZoneName())
                .branchId(inv.getWarehouseZone().getBranch().getId())
                .branchName(inv.getWarehouseZone().getBranch().getBranchName())
                .productId(inv.getProduct().getId())
                .productCode(inv.getProduct().getProductCode())
                .productName(inv.getProduct().getName())
                .sizeId(inv.getSize() != null ? inv.getSize().getId() : null)
                .sizeCode(inv.getSize() != null ? inv.getSize().getSizeCode() : null)
                .sizeName(inv.getSize() != null ? inv.getSize().getSizeName() : null)
                .colorId(inv.getColor() != null ? inv.getColor().getId() : null)
                .colorCode(inv.getColor() != null ? inv.getColor().getColorCode() : null)
                .colorName(inv.getColor() != null ? inv.getColor().getColorName() : null)
                .quantity(physical)
                .quantityPhysical(physical)
                .quantityAllocated(allocated)
                .quantityAvailable(available)
                .lastUpdated(inv.getUpdatedAt())
                .build();
    }

    private InventoryResponse toResponse(InventorySummaryProjection inv) {
        BigDecimal physical = inv.getQuantityPhysical() != null ? inv.getQuantityPhysical() : BigDecimal.ZERO;
        BigDecimal allocated = inv.getQuantityAllocated() != null ? inv.getQuantityAllocated() : BigDecimal.ZERO;
        BigDecimal available = inv.getQuantityAvailable() != null ? inv.getQuantityAvailable() : BigDecimal.ZERO;
        return InventoryResponse.builder()
                .id(inv.getId())
                .warehouseZoneId(inv.getWarehouseZoneId())
                .warehouseZoneName(inv.getWarehouseZoneName())
                .branchId(inv.getBranchId())
                .branchName(inv.getBranchName())
                .productId(inv.getProductId())
                .productCode(inv.getProductCode())
                .productName(inv.getProductName())
                .sizeId(inv.getSizeId())
                .sizeCode(inv.getSizeCode())
                .sizeName(inv.getSizeName())
                .colorId(inv.getColorId())
                .colorCode(inv.getColorCode())
                .colorName(inv.getColorName())
                .quantity(physical)
                .quantityPhysical(physical)
                .quantityAllocated(allocated)
                .quantityAvailable(available)
                .lastUpdated(inv.getUpdatedAt())
                .build();
    }

    private StockLedgerResponse toLedgerResponse(StockLedger ledger) {
        return StockLedgerResponse.builder()
                .id(ledger.getId())
                .transactionType(ledger.getTransactionType())
                .referenceId(ledger.getReferenceId())
                .referenceDocument(ledger.getNote())
                .productCode(ledger.getProduct().getProductCode())
                .productName(ledger.getProduct().getName())
                .quantityChange(ledger.getChangeQty())
                .runningBalance(ledger.getBalanceAfter())
                .branchName(ledger.getBranch().getBranchName())
                .notes(ledger.getNote())
                .createdBy(ledger.getCreatedBy())
                .transactionDate(ledger.getCreatedAt())
                .build();
    }

    // --- ADD STOCK ---

    @Override
    @Transactional
    @LogActivity(actionType = "ADD", entityName = "SizeInventory", entityClass = SizeInventory.class)
    public AdjustmentResponse addStock(Long warehouseZoneId, Long branchId, Long productId,
                                       Long sizeId, Long colorId, BigDecimal quantity,
                                       String reason, String referenceDocument, Long referenceId) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "quantity phải lớn hơn 0");
        }

        InventoryAdjustRequest adjustRequest = InventoryAdjustRequest.builder()
                .warehouseZoneId(warehouseZoneId)
                .branchId(branchId)
                .productId(productId)
                .sizeId(sizeId)
                .colorId(colorId)
                .build();

        WarehouseZone zone = resolveWarehouseZone(adjustRequest);
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        final Size sizeEntity = sizeId != null
                ? sizeRepository.findById(sizeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Size", "id", sizeId))
                : null;
        final Color colorEntity = colorId != null
                ? colorRepository.findById(colorId)
                        .orElseThrow(() -> new ResourceNotFoundException("Color", "id", colorId))
                : null;

        Long resolvedSizeId = sizeEntity != null ? sizeEntity.getId() : null;
        Long resolvedColorId = colorEntity != null ? colorEntity.getId() : null;

        SizeInventory inventory = sizeInventoryRepository
                .findAndLockBySkuAttributes(zone.getId(), product.getId(), resolvedSizeId, resolvedColorId)
                .orElseGet(() -> SizeInventory.builder()
                        .warehouseZone(zone)
                        .product(product)
                        .size(sizeEntity)
                        .color(colorEntity)
                        .quantityPhysical(BigDecimal.ZERO)
                        .quantityAllocated(BigDecimal.ZERO)
                        .isActive(true)
                        .build());

        BigDecimal oldQty = inventory.getQuantityPhysical() != null ? inventory.getQuantityPhysical() : BigDecimal.ZERO;
        BigDecimal newQty = oldQty.add(quantity);

        inventory.setQuantityPhysical(newQty);
        sizeInventoryRepository.save(inventory);

        StockLedger ledger = StockLedger.builder()
                .transactionType(reason != null ? reason : "ADD")
                .referenceId(referenceId != null ? referenceId : inventory.getId())
                .changeQty(quantity)
                .balanceAfter(newQty)
                .branch(zone.getBranch())
                .warehouseZone(zone)
                .product(product)
                .build();
        if (referenceDocument != null) {
            ledger.setNote(referenceDocument);
        }
        stockLedgerRepository.save(ledger);

        // Đồng bộ cộng tồn kho trong InventoryBalance và tạo InventoryTransaction type PURCHASE
        List<ProductVariant> pvList = productVariantRepository.findByProductIdAndIsDeletedFalse(product.getId());
        if (!pvList.isEmpty()) {
            ProductVariant variant = pvList.get(0);
            InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(variant.getId(), zone.getBranch().getId())
                    .orElseGet(() -> {
                        InventoryBalance ib = InventoryBalance.builder()
                            .productVariant(variant)
                            .branch(zone.getBranch())
                            .availableQuantity(BigDecimal.ZERO)
                            .reservedQuantity(BigDecimal.ZERO)
                            .damagedQuantity(BigDecimal.ZERO)
                            .build();
                        ib.setIsDeleted(false);
                        return inventoryBalanceRepository.save(ib);
                    });
            BigDecimal beforeQty = balance.getAvailableQuantity() != null ? balance.getAvailableQuantity() : BigDecimal.ZERO;
            BigDecimal afterQty = beforeQty.add(quantity);
            balance.setAvailableQuantity(afterQty);
            balance.setLastUpdated(LocalDateTime.now());
            inventoryBalanceRepository.save(balance);

            String txCode = "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + "-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4);

            InventoryTransaction tx = InventoryTransaction.builder()
                    .transactionCode(txCode)
                    .productVariant(variant)
                    .sourceBranch(zone.getBranch())
                    .transactionType(InventoryTransactionType.IMPORT)
                    .quantity(quantity)
                    .beforeQuantity(beforeQty)
                    .afterQuantity(afterQty)
                    .build();
            tx.setIsDeleted(false);
            tx.setCreatedBy(getCurrentUsername());
            inventoryTransactionRepository.save(tx);
        }

        return AdjustmentResponse.builder()
                .inventoryId(inventory.getId())
                .oldQuantity(oldQty)
                .newQuantity(newQty)
                .changeQty(quantity)
                .transactionType(reason != null ? reason : "ADD")
                .reason(referenceDocument)
                .build();
    }

    // --- IMPORT RECEIPT ---

    @Override
    @Transactional(readOnly = true)
    public List<ImportReceiptDTO> getAllImportReceipts() {
        return importReceiptRepository.findAllWithAssociations().stream()
                .map(r -> {
                    ImportReceiptDTO dto = toImportReceiptDTO(r);
                    dto.setReceiptLines(importReceiptDetailRepository.findByReceiptIdAndIsDeletedFalse(r.getId()).stream()
                            .map(this::toImportReceiptDetailDTO)
                            .collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImportReceiptDTO getImportReceiptById(Long id) {
        ImportReceipt r = importReceiptRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ImportReceipt", "id", id));
        ImportReceiptDTO dto = toImportReceiptDTO(r);
        dto.setReceiptLines(importReceiptDetailRepository.findByReceiptIdAndIsDeletedFalse(id).stream()
                .map(this::toImportReceiptDetailDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional
    public ImportReceiptDTO createImportReceipt(ImportReceiptDTO dto) {
        Branch branch = null;
        if (dto.getBranchId() != null) {
            branch = branchRepository.findByIdAndIsDeletedFalse(dto.getBranchId()).orElse(null);
        }
        if (branch == null && dto.getBranchName() != null && !dto.getBranchName().isBlank()) {
            final String bNameSearch = dto.getBranchName().trim();
            branch = branchRepository.findAll().stream()
                    .filter(b -> Boolean.FALSE.equals(b.getIsDeleted()) &&
                            (b.getBranchName() != null && bNameSearch.equalsIgnoreCase(b.getBranchName())))
                    .findFirst().orElse(null);
        }
        if (branch == null) {
            branch = branchRepository.findAll().stream().filter(b -> Boolean.FALSE.equals(b.getIsDeleted())).findFirst().orElse(null);
        }
        if (branch == null) {
            Branch defaultBranch = Branch.builder()
                    .branchName("Main Flagship / HQ")
                    .branchCode("HQ-MAIN")
                    .isActive(true)
                    .build();
            defaultBranch.setIsDeleted(false);
            branch = branchRepository.save(defaultBranch);
        }

        Supplier supplier = dto.getSupplierId() != null
                ? supplierRepository.findById(dto.getSupplierId()).orElse(null)
                : supplierRepository.findAll().stream().findFirst().orElse(null);

        org.example.storemanager.modules.sales.entity.PurchaseOrder purchaseOrder = null;
        if (dto.getPurchaseOrderId() != null) {
            purchaseOrder = purchaseOrderRepository.findById(dto.getPurchaseOrderId()).orElse(null);
        } else if (dto.getPurchaseOrderCode() != null && !dto.getPurchaseOrderCode().isBlank()) {
            final String poCodeSearch = dto.getPurchaseOrderCode().trim();
            purchaseOrder = purchaseOrderRepository.findAll().stream()
                    .filter(p -> p.getPoCode() != null && p.getPoCode().equalsIgnoreCase(poCodeSearch))
                    .findFirst().orElse(null);
        }

        if (purchaseOrder != null) {
            final Long poTargetId = purchaseOrder.getId();
            boolean alreadyHasReceipt = importReceiptRepository.findAll().stream()
                    .anyMatch(ir -> !Boolean.TRUE.equals(ir.getIsDeleted())
                            && ir.getPurchaseOrder() != null
                            && ir.getPurchaseOrder().getId().equals(poTargetId)
                            && !"CANCELLED".equalsIgnoreCase(ir.getStatus())
                            && !"DA_HUY".equalsIgnoreCase(ir.getStatus()));
            if (alreadyHasReceipt) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Đơn mua hàng " + (purchaseOrder.getPoCode() != null ? purchaseOrder.getPoCode() : purchaseOrder.getId())
                                + " đã được tạo đơn nhập kho trước đó! Không thể tạo thêm.");
            }
        }

        if (dto.getReceiptDate() != null && dto.getReceiptDate().toLocalDate().isBefore(java.time.LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày dự kiến nhận không được nằm trong quá khứ");
        }

        ImportReceipt receipt = ImportReceipt.builder()
                .receiptCode(dto.getReceiptCode() != null ? dto.getReceiptCode() : "GRN-" + System.currentTimeMillis())
                .receiptDate(dto.getReceiptDate() != null ? dto.getReceiptDate() : LocalDateTime.now())
                .totalAmount(dto.getTotalAmount() != null ? dto.getTotalAmount() : BigDecimal.ZERO)
                .discount(dto.getDiscount() != null ? dto.getDiscount() : BigDecimal.ZERO)
                .tax(dto.getTax() != null ? dto.getTax() : BigDecimal.ZERO)
                .status(dto.getStatus() != null ? dto.getStatus() : "COMPLETE")
                .branch(branch)
                .supplier(supplier)
                .purchaseOrder(purchaseOrder)
                .inspectedBy(dto.getInspectedBy())
                .note(dto.getNote())
                .build();
        receipt.setIsDeleted(false);
        ImportReceipt saved = importReceiptRepository.save(receipt);

        if (saved.getPurchaseOrder() != null) {
            org.example.storemanager.modules.sales.entity.PurchaseOrder poToUpdate = saved.getPurchaseOrder();
            poToUpdate.setStatus("RECEIVED");
            purchaseOrderRepository.save(poToUpdate);
        }

        java.util.Map<Long, BigDecimal> orderedQtys = new java.util.HashMap<>();
        java.util.Map<Long, BigDecimal> alreadyReceived = new java.util.HashMap<>();
        if (purchaseOrder != null) {
            List<org.example.storemanager.modules.sales.entity.PurchaseOrderDetail> poDetails = 
                purchaseOrderDetailRepository.findByPurchaseOrderIdAndIsDeletedFalse(purchaseOrder.getId());
            for (org.example.storemanager.modules.sales.entity.PurchaseOrderDetail pod : poDetails) {
                if (pod.getProduct() != null) {
                    orderedQtys.put(pod.getProduct().getId(), pod.getQuantity());
                }
            }

            // Sum already received in other receipts
            final Long poIdVal = purchaseOrder.getId();
            List<ImportReceipt> otherReceipts = importReceiptRepository.findAll().stream()
                .filter(r -> r.getPurchaseOrder() != null && r.getPurchaseOrder().getId().equals(poIdVal) 
                             && !Boolean.TRUE.equals(r.getIsDeleted()))
                .collect(Collectors.toList());
            for (ImportReceipt or : otherReceipts) {
                List<ImportReceiptDetail> details = importReceiptDetailRepository.findByReceiptIdAndIsDeletedFalse(or.getId());
                for (ImportReceiptDetail det : details) {
                    if (det.getProduct() != null) {
                        Long pId = det.getProduct().getId();
                        BigDecimal qty = det.getQuantity() != null ? det.getQuantity() : BigDecimal.ZERO;
                        alreadyReceived.put(pId, alreadyReceived.getOrDefault(pId, BigDecimal.ZERO).add(qty));
                    }
                }
            }
        }

        List<ImportReceiptDetailDTO> savedLines = new ArrayList<>();
        if (dto.getReceiptLines() != null && !dto.getReceiptLines().isEmpty()) {
            for (ImportReceiptDetailDTO line : dto.getReceiptLines()) {
                ProductVariant variant = null;
                Product targetProduct = null;

                // 1. Ưu tiên cao nhất: Khớp với chi tiết sản phẩm của Đơn mua hàng (PO)
                if (purchaseOrder != null) {
                    List<org.example.storemanager.modules.sales.entity.PurchaseOrderDetail> poDetails =
                            purchaseOrderDetailRepository.findByPurchaseOrderIdAndIsDeletedFalse(purchaseOrder.getId());
                    for (org.example.storemanager.modules.sales.entity.PurchaseOrderDetail pod : poDetails) {
                        if (pod.getProduct() == null) continue;
                        boolean matches = false;
                        if (line.getProductId() != null && pod.getProduct().getId().equals(line.getProductId())) {
                            matches = true;
                        } else if (line.getSku() != null && pod.getProduct().getProductCode() != null && line.getSku().equalsIgnoreCase(pod.getProduct().getProductCode())) {
                            matches = true;
                        } else if (line.getProductName() != null && pod.getProduct().getName() != null && line.getProductName().equalsIgnoreCase(pod.getProduct().getName())) {
                            matches = true;
                        } else if (line.getProductVariantId() != null && pod.getProduct().getId().equals(line.getProductVariantId())) {
                            matches = true;
                        }
                        if (matches) {
                            targetProduct = pod.getProduct();
                            break;
                        }
                    }
                }

                // 2. Nếu không khớp PO hoặc không có PO, tìm qua productId
                if (targetProduct == null && line.getProductId() != null) {
                    targetProduct = productRepository.findByIdAndIsDeletedFalse(line.getProductId()).orElse(null);
                }

                // 3. Nếu vẫn null, kiểm tra theo productVariantId
                if (targetProduct == null && line.getProductVariantId() != null) {
                    variant = productVariantRepository.findById(line.getProductVariantId()).orElse(null);
                    if (variant != null && variant.getProduct() != null) {
                        if (purchaseOrder == null || orderedQtys.containsKey(variant.getProduct().getId())) {
                            targetProduct = variant.getProduct();
                        } else {
                            variant = null; // Tránh nhầm variant của sản phẩm ngoài PO
                        }
                    }
                }

                // 4. Nếu vẫn null, tìm theo SKU hoặc Tên sản phẩm
                if (targetProduct == null && (line.getSku() != null || line.getProductName() != null)) {
                    if (line.getSku() != null) {
                        targetProduct = productRepository.findByProductCodeAndIsDeletedFalse(line.getSku()).orElse(null);
                    }
                    if (targetProduct == null && line.getProductName() != null) {
                        targetProduct = productRepository.findAll().stream()
                                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()) && p.getName() != null && p.getName().equalsIgnoreCase(line.getProductName()))
                                .findFirst().orElse(null);
                    }
                }

                // Fallback nếu có PO mà chưa gán được targetProduct: lấy sản phẩm đầu tiên trong PO
                if (targetProduct == null && purchaseOrder != null) {
                    List<org.example.storemanager.modules.sales.entity.PurchaseOrderDetail> poDetails =
                            purchaseOrderDetailRepository.findByPurchaseOrderIdAndIsDeletedFalse(purchaseOrder.getId());
                    if (!poDetails.isEmpty() && poDetails.get(0).getProduct() != null) {
                        targetProduct = poDetails.get(0).getProduct();
                    }
                }

                // Phục hồi hoặc tạo mới variant cho targetProduct
                if (targetProduct != null) {
                    if (variant == null || variant.getProduct() == null || !variant.getProduct().getId().equals(targetProduct.getId())) {
                        variant = productVariantRepository.findByProductIdAndIsDeletedFalse(targetProduct.getId())
                                .stream().findFirst().orElse(null);
                        if (variant == null) {
                            ProductVariant newVar = ProductVariant.builder()
                                    .product(targetProduct)
                                    .variantCode("VAR-" + targetProduct.getProductCode() + "-" + System.currentTimeMillis() % 10000)
                                    .sku(targetProduct.getProductCode() + "-DEFAULT")
                                    .price(targetProduct.getBasePrice() != null ? targetProduct.getBasePrice() : BigDecimal.ZERO)
                                    .build();
                            newVar.setIsDeleted(false);
                            variant = productVariantRepository.save(newVar);
                        }
                    }
                } else if (variant == null) {
                    variant = productVariantRepository.findAll().stream().findFirst().orElse(null);
                }

                if (variant == null) {
                    Product defaultProd = Product.builder()
                            .name("Sữa tươi Vinamilk 100% Không đường 1L")
                            .productCode("VNM-MILK-1L")
                            .basePrice(BigDecimal.valueOf(28000))
                            .build();
                    defaultProd.setIsDeleted(false);
                    Product savedProd = productRepository.save(defaultProd);

                    ProductVariant newVar = ProductVariant.builder()
                            .product(savedProd)
                            .variantCode("VAR-VNM-MILK-1L")
                            .sku("VNM-MILK-1L")
                            .price(BigDecimal.valueOf(28000))
                            .build();
                    newVar.setIsDeleted(false);
                    variant = productVariantRepository.save(newVar);
                }

                if (purchaseOrder != null && variant.getProduct() != null) {
                    Long pId = variant.getProduct().getId();
                    BigDecimal orderedQty = orderedQtys.getOrDefault(pId, BigDecimal.ZERO);
                    BigDecimal prevRec = alreadyReceived.getOrDefault(pId, BigDecimal.ZERO);
                    BigDecimal currentRec = line.getQuantity() != null ? line.getQuantity() : BigDecimal.ZERO;
                    
                    if (prevRec.add(currentRec).compareTo(orderedQty) > 0) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                            "Số lượng thực nhận cho sản phẩm " + variant.getProduct().getName() + " vượt quá số lượng đặt hàng ban đầu (Đã nhận: " + prevRec + ", Đang nhận: " + currentRec + ", Đặt mua: " + orderedQty + ")");
                    }
                }

                WarehouseBin targetBin = line.getTargetBinId() != null
                        ? warehouseBinRepository.findByIdAndIsDeletedFalse(line.getTargetBinId()).orElse(null)
                        : warehouseBinRepository.findAll().stream().findFirst().orElse(null);

                String prodName = (variant.getProduct() != null && variant.getProduct().getName() != null)
                        ? variant.getProduct().getName()
                        : "Sữa tươi Vinamilk 100% Không đường 1L";
                String sku = (variant.getSku() != null) ? variant.getSku() : "VNM-MILK-1L";
                BigDecimal unitCost = (line.getUnitCost() != null) ? line.getUnitCost() : BigDecimal.valueOf(50000);
                BigDecimal qty = (line.getQuantity() != null) ? line.getQuantity() : BigDecimal.TEN;
                BigDecimal subTotal = (line.getSubTotal() != null) ? line.getSubTotal() : unitCost.multiply(qty);

                ImportReceiptDetail detail = ImportReceiptDetail.builder()
                        .receipt(saved)
                        .productVariant(variant)
                        .product(variant.getProduct())
                        .productNameSnapshot(prodName)
                        .skuSnapshot(sku)
                        .barcodeSnapshot(variant.getBarcode() != null ? variant.getBarcode() : "893123456789")
                        .unitCostSnapshot(unitCost)
                        .unitPrice(unitCost)
                        .quantity(qty)
                        .subTotal(subTotal)
                        .batchNumber(line.getBatchNumber() != null ? line.getBatchNumber() : "BATCH-" + (System.currentTimeMillis() % 10000))
                        .expiryDate(line.getExpiryDate())
                        .targetBin(targetBin)
                        .build();
                detail.setIsDeleted(false);

                ImportReceiptDetail savedDetail = importReceiptDetailRepository.save(detail);
                savedLines.add(toImportReceiptDetailDTO(savedDetail));
            }
        }

        if ("COMPLETE".equalsIgnoreCase(saved.getStatus()) || "PASSED".equalsIgnoreCase(saved.getStatus()) || "APPROVED".equalsIgnoreCase(saved.getStatus())
                || "INSPECTED_ACCEPTED".equalsIgnoreCase(saved.getStatus()) || "PARTIAL_ACCEPTANCE".equalsIgnoreCase(saved.getStatus())) {
            saved.setStatus("PENDING");
            importReceiptRepository.save(saved);
            return completeImportReceipt(saved.getId());
        }

        ImportReceiptDTO result = toImportReceiptDTO(saved);
        result.setReceiptLines(savedLines);
        return result;
    }

    @Override
    @Transactional
    public ImportReceiptDTO updateImportReceipt(Long id, ImportReceiptDTO dto) {
        ImportReceipt r = importReceiptRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ImportReceipt", "id", id));
        if (dto.getBranchId() != null) {
            Branch b = branchRepository.findByIdAndIsDeletedFalse(dto.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));
            r.setBranch(b);
        } else if (dto.getBranchName() != null && !dto.getBranchName().isBlank()) {
            final String bNameSearch = dto.getBranchName().trim();
            Branch b = branchRepository.findAll().stream()
                    .filter(br -> Boolean.FALSE.equals(br.getIsDeleted()) &&
                            (br.getBranchName() != null && bNameSearch.equalsIgnoreCase(br.getBranchName())))
                    .findFirst().orElse(null);
            if (b != null) r.setBranch(b);
        }
        if (dto.getSupplierId() != null) {
            Supplier s = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", dto.getSupplierId()));
            r.setSupplier(s);
        }
        if (dto.getPurchaseOrderId() != null) {
            org.example.storemanager.modules.sales.entity.PurchaseOrder po = purchaseOrderRepository.findById(dto.getPurchaseOrderId()).orElse(null);
            if (po != null) r.setPurchaseOrder(po);
        } else if (dto.getPurchaseOrderCode() != null && !dto.getPurchaseOrderCode().isBlank()) {
            final String poCodeSearch = dto.getPurchaseOrderCode().trim();
            org.example.storemanager.modules.sales.entity.PurchaseOrder po = purchaseOrderRepository.findAll().stream()
                    .filter(p -> p.getPoCode() != null && p.getPoCode().equalsIgnoreCase(poCodeSearch))
                    .findFirst().orElse(null);
            if (po != null) r.setPurchaseOrder(po);
        }
        if (dto.getReceiptCode() != null && !dto.getReceiptCode().isBlank()) {
            r.setReceiptCode(dto.getReceiptCode());
        }
        if (dto.getReceiptDate() != null) {
            if (dto.getReceiptDate().toLocalDate().isBefore(java.time.LocalDate.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày dự kiến nhận không được nằm trong quá khứ");
            }
            r.setReceiptDate(dto.getReceiptDate());
        }
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            r.setStatus(dto.getStatus());
        }
        if (dto.getNote() != null) {
            r.setNote(dto.getNote());
        }
        if (dto.getInspectedBy() != null) {
            r.setInspectedBy(dto.getInspectedBy());
        }
        if (dto.getCreatedBy() != null && !dto.getCreatedBy().isBlank()) {
            r.setCreatedBy(dto.getCreatedBy());
        }
        r.setTotalAmount(dto.getTotalAmount());
        r.setDiscount(dto.getDiscount());
        r.setTax(dto.getTax());
        ImportReceipt saved = importReceiptRepository.save(r);

        String stUpper = dto.getStatus() != null ? dto.getStatus().toUpperCase() : "";
        if ("COMPLETED".equals(stUpper) || "COMPLETE".equals(stUpper) || "DA_NHAN".equals(stUpper)
                || "INSPECTED_ACCEPTED".equals(stUpper) || "PARTIAL_ACCEPTANCE".equals(stUpper)) {
            return completeImportReceipt(saved.getId());
        }

        return toImportReceiptDTO(saved);
    }

    @Override
    @Transactional
    public void deleteImportReceipt(Long id) {
        ImportReceipt r = importReceiptRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ImportReceipt", "id", id));
        r.setIsDeleted(true);
        importReceiptRepository.save(r);

        if (r.getPurchaseOrder() != null) {
            org.example.storemanager.modules.sales.entity.PurchaseOrder po = r.getPurchaseOrder();
            final Long poIdVal = po.getId();
            final Long rIdVal = r.getId();
            boolean hasOther = importReceiptRepository.findAll().stream()
                    .anyMatch(ir -> !Boolean.TRUE.equals(ir.getIsDeleted())
                            && !ir.getId().equals(rIdVal)
                            && ir.getPurchaseOrder() != null
                            && ir.getPurchaseOrder().getId().equals(poIdVal));
            if (!hasOther && "RECEIVED".equals(po.getStatus())) {
                po.setStatus("APPROVED");
                purchaseOrderRepository.save(po);
            }
        }
    }

    @Override
    @Transactional
    public ImportReceiptDTO completeImportReceipt(Long id) {
        ImportReceipt r = importReceiptRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ImportReceipt", "id", id));
        if ("COMPLETE".equals(r.getStatus()) || "PARTIAL".equals(r.getStatus()) || "INSPECTED_ACCEPTED".equals(r.getStatus()) || "PARTIAL_ACCEPTANCE".equals(r.getStatus())) {
            return toImportReceiptDTO(r);
        }

        if (r.getPurchaseOrder() != null) {
            List<org.example.storemanager.modules.sales.entity.PurchaseOrderDetail> poDetails = 
                purchaseOrderDetailRepository.findByPurchaseOrderIdAndIsDeletedFalse(r.getPurchaseOrder().getId());
            BigDecimal totalOrdered = poDetails.stream()
                .map(org.example.storemanager.modules.sales.entity.PurchaseOrderDetail::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalReceived = BigDecimal.ZERO;
            List<ImportReceipt> otherReceipts = importReceiptRepository.findAll().stream()
                .filter(x -> x.getPurchaseOrder() != null && x.getPurchaseOrder().getId().equals(r.getPurchaseOrder().getId()) 
                             && !Boolean.TRUE.equals(x.getIsDeleted()))
                .collect(Collectors.toList());
            for (ImportReceipt or : otherReceipts) {
                List<ImportReceiptDetail> rDetails = importReceiptDetailRepository.findByReceiptIdAndIsDeletedFalse(or.getId());
                for (ImportReceiptDetail det : rDetails) {
                    totalReceived = totalReceived.add(det.getQuantity() != null ? det.getQuantity() : BigDecimal.ZERO);
                }
            }

            if (totalReceived.compareTo(totalOrdered) >= 0) {
                r.setStatus("INSPECTED_ACCEPTED");
            } else {
                r.setStatus("PARTIAL_ACCEPTANCE");
            }
        } else {
            r.setStatus("INSPECTED_ACCEPTED");
        }

        ImportReceipt saved = importReceiptRepository.save(r);

        if (saved.getPurchaseOrder() != null) {
            org.example.storemanager.modules.sales.entity.PurchaseOrder po = saved.getPurchaseOrder();
            po.setStatus("RECEIVED");
            purchaseOrderRepository.save(po);
        }

        List<ImportReceiptDetail> details = importReceiptDetailRepository.findByReceiptIdAndIsDeletedFalse(id);
        WarehouseZone defaultZone = warehouseService.getOrCreateDefaultZone(r.getBranch());
        String username = getCurrentUsername();

        if (details != null) {
            for (ImportReceiptDetail detail : details) {
                ProductVariant variant = detail.getProductVariant();
                if (variant == null && detail.getProduct() != null) {
                    variant = productVariantRepository.findByProductIdAndIsDeletedFalse(detail.getProduct().getId())
                            .stream().findFirst().orElse(null);
                }
                if (variant == null) {
                    variant = productVariantRepository.findAll().stream().findFirst().orElse(null);
                }
                Product product = detail.getProduct() != null ? detail.getProduct() : (variant != null ? variant.getProduct() : null);
                if (product == null && variant != null) product = variant.getProduct();
                if (product == null || variant == null) continue;

                final Product finalProduct = product;
                final ProductVariant finalVariant = variant;
                BigDecimal quantity = detail.getQuantity() != null ? detail.getQuantity() : BigDecimal.ONE;

                // 1. Physical stock addition (SizeInventory & StockLedger)
                addStock(defaultZone.getId(), r.getBranch().getId(), product.getId(),
                        null, null, quantity,
                        "IMPORT", r.getReceiptCode(), r.getId());

                // 2. Set Bin status and update/create ProductLocation
                if (detail.getTargetBin() != null) {
                    WarehouseBin bin = detail.getTargetBin();
                    bin.setStatus("OCCUPIED");
                    warehouseBinRepository.save(bin);

                    ProductLocation loc = productLocationRepository.findByProductIdAndBinIdAndIsDeletedFalse(product.getId(), bin.getId())
                            .orElseGet(() -> ProductLocation.builder()
                                    .product(finalProduct)
                                    .bin(bin)
                                    .quantity(BigDecimal.ZERO)
                                    .build());
                    loc.setQuantity(loc.getQuantity().add(quantity));
                    loc.setIsDeleted(false);
                    productLocationRepository.save(loc);
                }

            // 3. Update InventoryBalance
            InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(variant.getId(), r.getBranch().getId())
                    .orElseGet(() -> InventoryBalance.builder()
                            .productVariant(finalVariant)
                            .branch(r.getBranch())
                            .availableQuantity(BigDecimal.ZERO)
                            .reservedQuantity(BigDecimal.ZERO)
                            .damagedQuantity(BigDecimal.ZERO)
                            .build());
            BigDecimal beforeQty = balance.getAvailableQuantity() != null ? balance.getAvailableQuantity() : BigDecimal.ZERO;
            BigDecimal afterQty = beforeQty.add(detail.getQuantity());
            balance.setAvailableQuantity(afterQty);
            if (balance.getReservedQuantity() == null) balance.setReservedQuantity(BigDecimal.ZERO);
            if (balance.getDamagedQuantity() == null) balance.setDamagedQuantity(BigDecimal.ZERO);
            balance.setLastUpdated(LocalDateTime.now());
            inventoryBalanceRepository.save(balance);

            // 4. Create InventoryTransaction = IMPORT
            String txCode = "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + "-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4);

            InventoryTransaction tx = InventoryTransaction.builder()
                    .transactionCode(txCode)
                    .productVariant(variant)
                    .sourceBranch(r.getBranch())
                    .transactionType(InventoryTransactionType.IMPORT)
                    .quantity(detail.getQuantity())
                    .beforeQuantity(beforeQty)
                    .afterQuantity(afterQty)
                    .build();
            tx.setIsDeleted(false);
            tx.setCreatedBy(username);
            inventoryTransactionRepository.save(tx);

            // 5. Create Serial Numbers (if product uses serials or tracking)
            int count = detail.getQuantity().intValue();
            for (int i = 0; i < Math.min(count, 100); i++) {
                String snString = product.getProductCode() + "-SN-" + System.currentTimeMillis() + "-" + i;
                SerialNumber sn = SerialNumber.builder()
                        .product(product)
                        .serialNumber(snString)
                        .status("AVAILABLE")
                        .importReceiptId(r.getId())
                        .build();
                sn.setIsDeleted(false);
                sn.setCreatedBy(username);
                serialNumberRepository.save(sn);
            }

            // Handle ProductBatch creation
            if (detail.getBatchNumber() != null && !detail.getBatchNumber().trim().isEmpty()) {
                ProductBatch batch = ProductBatch.builder()
                        .batchNumber(detail.getBatchNumber())
                        .manufactureDate(java.time.LocalDate.now().minusDays(1))
                        .expiryDate(detail.getExpiryDate() != null ? detail.getExpiryDate() : java.time.LocalDate.now().plusYears(1))
                        .status("ACTIVE")
                        .product(product)
                        .initialUnits(detail.getQuantity())
                        .remainingUnits(detail.getQuantity())
                        .unitCost(detail.getUnitCostSnapshot())
                        .supplierName(r.getSupplier() != null ? r.getSupplier().getName() : "")
                        .location(defaultZone.getZoneName())
                        .qualityStatus("PASSED_QA")
                        .build();
                batch.setIsDeleted(false);
                productBatchRepository.save(batch);
            }
        }
    }

    return toImportReceiptDTO(saved);
}

    @Override
    @Transactional
    public ImportReceiptDTO submitImportReceipt(Long id) {
        ImportReceipt r = importReceiptRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ImportReceipt", "id", id));
        if (!"DRAFT".equals(r.getStatus()) && !"PENDING".equals(r.getStatus())) {
            throw new org.example.storemanager.shared.exception.BusinessException(
                org.example.storemanager.shared.enums.ErrorCode.INVALID_STATUS_TRANSITION,
                "Chỉ có thể gửi duyệt phiếu ở trạng thái DRAFT/PENDING");
        }
        r.setStatus("PENDING_APPROVAL");
        r.setUpdatedBy(getCurrentUsername());
        return toImportReceiptDTO(importReceiptRepository.save(r));
    }

    @Override
    @Transactional
    public ImportReceiptDTO approveImportReceipt(Long id) {
        ImportReceipt r = importReceiptRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ImportReceipt", "id", id));
        if (!"PENDING_APPROVAL".equals(r.getStatus()) && !"PENDING_INSPECTION".equals(r.getStatus()) && !"PENDING".equals(r.getStatus())) {
            throw new org.example.storemanager.shared.exception.BusinessException(
                org.example.storemanager.shared.enums.ErrorCode.INVALID_STATUS_TRANSITION,
                "Chỉ có thể phê duyệt phiếu ở trạng thái PENDING_APPROVAL hoặc PENDING_INSPECTION");
        }
        r.setStatus("APPROVED");
        r.setUpdatedBy(getCurrentUsername());
        importReceiptRepository.save(r);
        return completeImportReceipt(id);
    }

    private String getCurrentUsername() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
            return auth.getName();
        }
        return "system";
    }

    // --- RETURN TO SUPPLIER ---

    @Override
    @Transactional(readOnly = true)
    public List<ReturnToSupplierDTO> getAllReturnToSuppliers() {
        return returnToSupplierRepository.findAllWithAssociations().stream()
                .map(r -> {
                    ReturnToSupplierDTO dto = toReturnToSupplierDTO(r);
                    List<ReturnToSupplierDetail> details = returnToSupplierDetailRepository.findByReturnReceiptIdAndIsDeletedFalse(r.getId());
                    dto.setReturnLines(details.stream().map(this::toReturnToSupplierDetailDTO).collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnToSupplierDTO getReturnToSupplierById(Long id) {
        ReturnToSupplier r = returnToSupplierRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnToSupplier", "id", id));
        ReturnToSupplierDTO dto = toReturnToSupplierDTO(r);
        dto.setReturnLines(returnToSupplierDetailRepository.findByReturnReceiptIdAndIsDeletedFalse(id).stream()
                .map(this::toReturnToSupplierDetailDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    private void validateReturnToSupplier(ReturnToSupplierDTO dto) {
        if (dto.getGrnRefNumber() == null || dto.getGrnRefNumber().trim().isEmpty() || !dto.getGrnRefNumber().startsWith("GRN-")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yêu cầu mã phiếu nhập kho tham chiếu bắt đầu bằng GRN-");
        }
        ImportReceipt originalReceipt = importReceiptRepository.findAll().stream()
            .filter(rec -> !Boolean.TRUE.equals(rec.getIsDeleted()) && (
                dto.getGrnRefNumber().equalsIgnoreCase(rec.getReceiptCode()) ||
                dto.getGrnRefNumber().equalsIgnoreCase(rec.getReceiptCode() != null ? rec.getReceiptCode() : "GRN-" + rec.getId())
            ))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy Phiếu nhập kho gốc hợp lệ có mã " + dto.getGrnRefNumber()));

        List<ImportReceiptDetail> receiptDetails = importReceiptDetailRepository.findByReceiptIdAndIsDeletedFalse(originalReceipt.getId());
        java.util.Map<Long, BigDecimal> receivedProductQtys = new java.util.HashMap<>();
        for (ImportReceiptDetail det : receiptDetails) {
            if (det.getProduct() != null) {
                BigDecimal qty = det.getQuantity() != null ? det.getQuantity() : BigDecimal.ZERO;
                receivedProductQtys.put(det.getProduct().getId(), receivedProductQtys.getOrDefault(det.getProduct().getId(), BigDecimal.ZERO).add(qty));
            }
        }

        if (dto.getReturnLines() != null && !dto.getReturnLines().isEmpty()) {
            for (ReturnToSupplierDetailDTO line : dto.getReturnLines()) {
                Long variantId = line.getProductVariantId();
                ProductVariant variant = null;
                if (variantId != null) {
                    variant = productVariantRepository.findById(variantId).orElse(null);
                }
                if (variant == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm xuất trả không hợp lệ");
                }
                Long prodId = variant.getProduct().getId();
                if (!receivedProductQtys.containsKey(prodId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm " + variant.getProduct().getName() + " không nằm trong phiếu nhập kho gốc " + dto.getGrnRefNumber());
                }
                BigDecimal maxAllowed = receivedProductQtys.get(prodId);
                BigDecimal qtyToReturn = line.getQuantity() != null ? line.getQuantity() : BigDecimal.ZERO;
                if (qtyToReturn.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số lượng xuất trả cho sản phẩm " + variant.getProduct().getName() + " phải lớn hơn 0");
                }
                if (qtyToReturn.compareTo(maxAllowed) > 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số lượng xuất trả cho sản phẩm " + variant.getProduct().getName() + " (" + qtyToReturn + ") vượt quá số lượng đã nhận trong phiếu nhập kho gốc (" + maxAllowed + ")");
                }
            }
        }
    }

    @Override
    @Transactional
    public ReturnToSupplierDTO createReturnToSupplier(ReturnToSupplierDTO dto) {
        validateReturnToSupplier(dto);
        Long branchId = dto.getBranchId();
        Branch branch = null;
        if (branchId != null) {
            branch = branchRepository.findByIdAndIsDeletedFalse(branchId).orElse(null);
        }
        if (branch == null) {
            branch = branchRepository.findByIsDeletedFalse().stream().findFirst().orElse(null);
        }
        if (branch == null) {
            Branch defaultBranch = Branch.builder()
                    .branchName("Main Flagship / HQ")
                    .branchCode("HQ-MAIN")
                    .isActive(true)
                    .build();
            defaultBranch.setIsDeleted(false);
            branch = branchRepository.save(defaultBranch);
        }

        Long supplierId = dto.getSupplierId();
        Supplier supplier = null;
        if (supplierId != null) {
            supplier = supplierRepository.findById(supplierId).orElse(null);
        }
        if (supplier == null && dto.getSupplierName() != null && !dto.getSupplierName().isBlank()) {
            final String sName = dto.getSupplierName().trim();
            supplier = supplierRepository.findAll().stream()
                    .filter(s -> s.getName() != null && s.getName().equalsIgnoreCase(sName))
                    .findFirst().orElse(null);
        }
        if (supplier == null) {
            supplier = supplierRepository.findAll().stream().findFirst().orElse(null);
        }

        ReturnToSupplier r = ReturnToSupplier.builder()
                .returnCode(dto.getReturnCode() != null && !dto.getReturnCode().isEmpty() ? dto.getReturnCode() : "RTV-" + System.currentTimeMillis())
                .returnDate(dto.getReturnDate() != null ? dto.getReturnDate() : LocalDateTime.now())
                .totalAmount(dto.getTotalAmount() != null ? dto.getTotalAmount() : BigDecimal.ZERO)
                .status(org.example.storemanager.shared.enums.inventory.ReturnToSupplierStatus.PENDING_SUPPLIER_APPROVAL)
                .reason(dto.getReason() != null ? dto.getReason() : "Xuất trả nhà cung cấp")
                .branch(branch)
                .supplier(supplier)
                .grnRefNumber(dto.getGrnRefNumber())
                .build();
        r.setIsDeleted(false);
        ReturnToSupplier saved = returnToSupplierRepository.save(r);

        List<ReturnToSupplierDetailDTO> savedLines = new ArrayList<>();
        if (dto.getReturnLines() != null && !dto.getReturnLines().isEmpty()) {
            for (ReturnToSupplierDetailDTO line : dto.getReturnLines()) {
                Long variantId = line.getProductVariantId();
                ProductVariant variant = null;
                if (variantId != null) {
                    variant = productVariantRepository.findById(variantId).orElse(null);
                }
                if (variant == null) {
                    variant = productVariantRepository.findAll().stream().findFirst().orElse(null);
                }

                ReturnToSupplierDetail detail = ReturnToSupplierDetail.builder()
                        .returnReceipt(saved)
                        .product(variant != null ? variant.getProduct() : null)
                        .quantity(line.getQuantity() != null ? line.getQuantity() : BigDecimal.ONE)
                        .unitPrice(line.getUnitCost() != null ? line.getUnitCost() : BigDecimal.ZERO)
                        .subTotal(line.getSubTotal() != null ? line.getSubTotal() : BigDecimal.ZERO)
                        .build();
                detail.setIsDeleted(false);
                ReturnToSupplierDetail savedDetail = returnToSupplierDetailRepository.save(detail);
                savedLines.add(toReturnToSupplierDetailDTO(savedDetail));
            }
        }

        ReturnToSupplierDTO result = toReturnToSupplierDTO(saved);
        result.setReturnLines(savedLines);
        return result;
    }

    @Override
    @Transactional
    public ReturnToSupplierDTO updateReturnToSupplier(Long id, ReturnToSupplierDTO dto) {
        validateReturnToSupplier(dto);
        ReturnToSupplier r = returnToSupplierRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnToSupplier", "id", id));
        r.setReturnCode(dto.getReturnCode());
        r.setTotalAmount(dto.getTotalAmount());
        r.setReason(dto.getReason());
        r.setGrnRefNumber(dto.getGrnRefNumber());
        ReturnToSupplier saved = returnToSupplierRepository.save(r);
        return toReturnToSupplierDTO(saved);
    }

    @Override
    @Transactional
    public void deleteReturnToSupplier(Long id) {
        ReturnToSupplier r = returnToSupplierRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnToSupplier", "id", id));
        r.setIsDeleted(true);
        returnToSupplierRepository.save(r);
    }

    @Override
    @Transactional
    public ReturnToSupplierDTO approveReturnToSupplier(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.ReturnApprovalRequest request) {
        ReturnToSupplier r = returnToSupplierRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnToSupplier", "id", id));
        if (r.getStatus() == org.example.storemanager.shared.enums.inventory.ReturnToSupplierStatus.APPROVED_CREDIT_NOTE
                || r.getStatus() == org.example.storemanager.shared.enums.inventory.ReturnToSupplierStatus.APPROVED
                || r.getStatus() == org.example.storemanager.shared.enums.inventory.ReturnToSupplierStatus.COMPLETE) {
            return toReturnToSupplierDTO(r);
        }

        r.setStatus(org.example.storemanager.shared.enums.inventory.ReturnToSupplierStatus.APPROVED_CREDIT_NOTE);
        if (request != null && request.getApprovalNotes() != null) {
            r.setReason(request.getApprovalNotes());
        }
        ReturnToSupplier saved = returnToSupplierRepository.save(r);
        applyReturnToSupplierDeductions(saved);
        return toReturnToSupplierDTO(saved);
    }

    @Override
    @Transactional
    public ReturnToSupplierDTO rejectReturnToSupplier(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.ReturnRejectRequest request) {
        ReturnToSupplier r = returnToSupplierRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnToSupplier", "id", id));
        ReturnToSupplierStatus prevStatus = r.getStatus();
        r.setStatus(org.example.storemanager.shared.enums.inventory.ReturnToSupplierStatus.REJECTED);
        if (request != null && request.getRejectNotes() != null) {
            r.setReason(request.getRejectNotes());
        }
        ReturnToSupplier saved = returnToSupplierRepository.save(r);
        if (prevStatus == ReturnToSupplierStatus.APPROVED_CREDIT_NOTE
                || prevStatus == ReturnToSupplierStatus.APPROVED
                || prevStatus == ReturnToSupplierStatus.COMPLETE) {
            restoreReturnToSupplierStock(saved);
        }
        return toReturnToSupplierDTO(saved);
    }

    private void applyReturnToSupplierDeductions(ReturnToSupplier r) {
        List<ReturnToSupplierDetail> details = returnToSupplierDetailRepository.findByReturnReceiptIdAndIsDeletedFalse(r.getId());
        WarehouseZone defaultZone = warehouseService.getOrCreateDefaultZone(r.getBranch());
        String username = getCurrentUsername();
        Branch branch = r.getBranch();

        for (ReturnToSupplierDetail detail : details) {
            Product product = detail.getProduct();
            if (product == null) continue;
            BigDecimal quantity = detail.getQuantity() != null ? detail.getQuantity() : BigDecimal.ONE;
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) continue;

            // 1. Deduct SizeInventory
            try {
                deductStock(defaultZone.getId(), branch != null ? branch.getId() : null, product.getId(),
                        null, null, quantity,
                        "RETURN", r.getReturnCode(), r.getId());
            } catch (Exception e) {
                log.warn("deductStock failed for ReturnToSupplier {}: {}", r.getId(), e.getMessage());
            }

            // 2. Deduct ProductLocation
            try {
                List<ProductLocation> locations = productLocationRepository.findByProductIdAndIsDeletedFalse(product.getId());
                BigDecimal remainingToDeduct = quantity;
                for (ProductLocation loc : locations) {
                    if (remainingToDeduct.compareTo(BigDecimal.ZERO) <= 0) break;
                    BigDecimal available = loc.getQuantity();
                    if (available == null || available.compareTo(BigDecimal.ZERO) <= 0) continue;

                    BigDecimal deduct = available.min(remainingToDeduct);
                    loc.setQuantity(available.subtract(deduct));
                    productLocationRepository.save(loc);
                    remainingToDeduct = remainingToDeduct.subtract(deduct);
                }
            } catch (Exception e) {
                log.warn("Failed to update ProductLocation for ReturnToSupplier {}: {}", r.getId(), e.getMessage());
            }

            // 3. Deduct InventoryBalance and record InventoryTransaction
            ProductVariant variant = productVariantRepository.findByProductIdAndIsDeletedFalse(product.getId()).stream().findFirst().orElse(null);
            if (variant != null && branch != null) {
                try {
                    InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(variant.getId(), branch.getId())
                            .orElseGet(() -> InventoryBalance.builder()
                                    .productVariant(variant)
                                    .branch(branch)
                                    .availableQuantity(BigDecimal.ZERO)
                                    .reservedQuantity(BigDecimal.ZERO)
                                    .damagedQuantity(BigDecimal.ZERO)
                                    .build());

                    BigDecimal beforeQty = balance.getAvailableQuantity() != null ? balance.getAvailableQuantity() : BigDecimal.ZERO;
                    BigDecimal afterQty = beforeQty.subtract(quantity).max(BigDecimal.ZERO);
                    balance.setAvailableQuantity(afterQty);
                    balance.setLastUpdated(LocalDateTime.now());
                    inventoryBalanceRepository.save(balance);

                    String txCode = "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                            + "-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4);

                    InventoryTransaction tx = InventoryTransaction.builder()
                            .transactionCode(txCode)
                            .productVariant(variant)
                            .sourceBranch(branch)
                            .transactionType(InventoryTransactionType.RETURN_TO_SUPPLIER)
                            .quantity(quantity)
                            .beforeQuantity(beforeQty)
                            .afterQuantity(afterQty)
                            .build();
                    tx.setIsDeleted(false);
                    tx.setCreatedBy(username);
                    inventoryTransactionRepository.save(tx);
                } catch (Exception e) {
                    log.warn("Failed to update InventoryBalance for ReturnToSupplier {}: {}", r.getId(), e.getMessage());
                }
            }
        }
    }

    private void restoreReturnToSupplierStock(ReturnToSupplier r) {
        List<ReturnToSupplierDetail> details = returnToSupplierDetailRepository.findByReturnReceiptIdAndIsDeletedFalse(r.getId());
        WarehouseZone defaultZone = warehouseService.getOrCreateDefaultZone(r.getBranch());
        String username = getCurrentUsername();
        Branch branch = r.getBranch();

        for (ReturnToSupplierDetail detail : details) {
            Product product = detail.getProduct();
            if (product == null) continue;
            BigDecimal quantity = detail.getQuantity() != null ? detail.getQuantity() : BigDecimal.ONE;
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) continue;

            try {
                addStock(defaultZone.getId(), branch != null ? branch.getId() : null, product.getId(),
                        null, null, quantity,
                        "RETURN_REVERSED", r.getReturnCode(), r.getId());
            } catch (Exception e) {
                log.warn("addStock failed for restoreReturnToSupplierStock: {}", e.getMessage());
            }

            try {
                ProductLocation loc = productLocationRepository.findByProductIdAndBinIdAndIsDeletedFalse(product.getId(), defaultZone.getId())
                        .orElseGet(() -> ProductLocation.builder().product(product).quantity(BigDecimal.ZERO).build());
                loc.setQuantity((loc.getQuantity() != null ? loc.getQuantity() : BigDecimal.ZERO).add(quantity));
                loc.setIsDeleted(false);
                productLocationRepository.save(loc);
            } catch (Exception e) {
                log.warn("Failed to restore ProductLocation: {}", e.getMessage());
            }

            ProductVariant variant = productVariantRepository.findByProductIdAndIsDeletedFalse(product.getId()).stream().findFirst().orElse(null);
            if (variant != null && branch != null) {
                try {
                    InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(variant.getId(), branch.getId())
                            .orElseGet(() -> InventoryBalance.builder()
                                    .productVariant(variant)
                                    .branch(branch)
                                    .availableQuantity(BigDecimal.ZERO)
                                    .reservedQuantity(BigDecimal.ZERO)
                                    .damagedQuantity(BigDecimal.ZERO)
                                    .build());
                    BigDecimal beforeQty = balance.getAvailableQuantity() != null ? balance.getAvailableQuantity() : BigDecimal.ZERO;
                    BigDecimal afterQty = beforeQty.add(quantity);
                    balance.setAvailableQuantity(afterQty);
                    balance.setLastUpdated(LocalDateTime.now());
                    inventoryBalanceRepository.save(balance);

                    String txCode = "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                            + "-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4);
                    InventoryTransaction tx = InventoryTransaction.builder()
                            .transactionCode(txCode)
                            .productVariant(variant)
                            .sourceBranch(branch)
                            .transactionType(InventoryTransactionType.ADJUSTMENT)
                            .quantity(quantity)
                            .beforeQuantity(beforeQty)
                            .afterQuantity(afterQty)
                            .build();
                    tx.setIsDeleted(false);
                    tx.setCreatedBy(username);
                    inventoryTransactionRepository.save(tx);
                } catch (Exception e) {
                    log.warn("Failed to restore InventoryBalance: {}", e.getMessage());
                }
            }
        }
    }

    // --- CANCEL ISSUE ---

    @Override
    @Transactional(readOnly = true)
    public List<CancelIssueDTO> getAllCancelIssues() {
        return cancelIssueRepository.findAllWithAssociations().stream()
                .map(this::toCancelIssueDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CancelIssueDTO getCancelIssueById(Long id) {
        CancelIssue c = cancelIssueRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("CancelIssue", "id", id));
        CancelIssueDTO dto = toCancelIssueDTO(c);
        dto.setCancelLines(cancelIssueDetailRepository.findByCancelIssueIdAndIsDeletedFalse(id).stream()
                .map(this::toCancelIssueDetailDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional
    public CancelIssueDTO createCancelIssue(CancelIssueDTO dto) {
        Branch branch = branchRepository.findByIdAndIsDeletedFalse(dto.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));

        CancelIssue c = CancelIssue.builder()
                .cancelCode(dto.getCancelCode())
                .cancelDate(dto.getCancelDate() != null ? dto.getCancelDate() : LocalDateTime.now())
                .totalValue(dto.getTotalValue())
                .reason(dto.getReason())
                .status(org.example.storemanager.shared.enums.inventory.CancelIssueStatus.PENDING_APPROVAL)
                .branch(branch)
                .build();
        c.setIsDeleted(false);
        CancelIssue saved = cancelIssueRepository.save(c);

        List<CancelIssueDetailDTO> savedLines = new ArrayList<>();

        if (dto.getCancelLines() != null) {
            for (CancelIssueDetailDTO line : dto.getCancelLines()) {
                ProductVariant variant = productVariantRepository.findById(line.getProductVariantId())
                        .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", line.getProductVariantId()));

                CancelIssueDetail detail = CancelIssueDetail.builder()
                        .cancelIssue(saved)
                        .product(variant.getProduct())
                        .quantity(line.getQuantity())
                        .unitPrice(variant.getPrice())
                        .subTotal(line.getSubTotal())
                        .build();
                detail.setIsDeleted(false);
                CancelIssueDetail savedDetail = cancelIssueDetailRepository.save(detail);

                savedLines.add(toCancelIssueDetailDTO(savedDetail));
            }
        }

        CancelIssueDTO result = toCancelIssueDTO(saved);
        result.setCancelLines(savedLines);
        return result;
    }

    @Override
    @Transactional
    public CancelIssueDTO updateCancelIssue(Long id, CancelIssueDTO dto) {
        CancelIssue c = cancelIssueRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("CancelIssue", "id", id));
        c.setCancelCode(dto.getCancelCode());
        c.setReason(dto.getReason());
        c.setTotalValue(dto.getTotalValue());
        CancelIssue saved = cancelIssueRepository.save(c);
        return toCancelIssueDTO(saved);
    }

    @Override
    @Transactional
    public void deleteCancelIssue(Long id) {
        CancelIssue c = cancelIssueRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("CancelIssue", "id", id));
        c.setIsDeleted(true);
        cancelIssueRepository.save(c);
    }

    // --- STOCK TRANSFER ---

    @Override
    @Transactional(readOnly = true)
    public List<StockTransferDTO> getAllStockTransfers() {
        return stockTransferRepository.findAllWithAssociations().stream()
                .map(this::toStockTransferDTO)
                .collect(Collectors.toList());
    }

    private StockTransfer findStockTransferByIdOrFallback(Long id) {
        if (id == null) {
            throw new ResourceNotFoundException("StockTransfer", "id", null);
        }
        Optional<StockTransfer> opt = stockTransferRepository.findByIdAndIsDeletedFalse(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        // If id is a large client-side timestamp (e.g. > 1_000_000_000L), fallback to latest transfer
        if (id > 1_000_000_000L) {
            List<StockTransfer> list = stockTransferRepository.findAllWithAssociations();
            if (!list.isEmpty()) {
                StockTransfer latest = list.get(list.size() - 1);
                log.info("[findStockTransferByIdOrFallback] Client timestamp ID {} mapped to latest transfer id={}", id, latest.getId());
                return latest;
            }
        }
        throw new ResourceNotFoundException("StockTransfer", "id", id);
    }

    @Override
    @Transactional(readOnly = true)
    public StockTransferDTO getStockTransferById(Long id) {
        StockTransfer t = findStockTransferByIdOrFallback(id);
        StockTransferDTO dto = toStockTransferDTO(t);
        dto.setTransferLines(stockTransferDetailRepository.findByTransferIdAndIsDeletedFalse(t.getId()).stream()
                .map(this::toStockTransferDetailDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional
    public StockTransferDTO createStockTransfer(StockTransferDTO dto) {
        Branch fromB = branchRepository.findByIdAndIsDeletedFalse(dto.getFromBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getFromBranchId()));
        Branch toB = branchRepository.findByIdAndIsDeletedFalse(dto.getToBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getToBranchId()));

        String initialStatus = (dto.getStatus() != null && !dto.getStatus().isBlank()) ? dto.getStatus() : "READY_TO_SHIP";

        StockTransfer t = StockTransfer.builder()
                .transferCode(dto.getTransferCode())
                .transferDate(dto.getTransferDate() != null ? dto.getTransferDate() : LocalDateTime.now())
                .status(initialStatus)
                .fromBranch(fromB)
                .toBranch(toB)
                .logisticsPartner(dto.getLogisticsPartner())
                .trackingRef(dto.getTrackingRef())
                .requestedBy(dto.getRequestedBy())
                .estArrivalDate(dto.getEstArrivalDate())
                .build();
        t.setIsDeleted(false);
        if (dto.getNote() != null) {
            t.setNote(dto.getNote());
        }
        StockTransfer saved = stockTransferRepository.save(t);

        List<StockTransferDetailDTO> savedLines = new ArrayList<>();
        if (dto.getTransferLines() != null) {
            for (StockTransferDetailDTO line : dto.getTransferLines()) {
                ProductVariant variant = null;
                if (line.getProductVariantId() != null) {
                    variant = productVariantRepository.findById(line.getProductVariantId()).orElse(null);
                }
                if (variant == null && line.getProductId() != null) {
                    List<ProductVariant> pvs = productVariantRepository.findByProductIdAndIsDeletedFalse(line.getProductId());
                    if (!pvs.isEmpty()) variant = pvs.get(0);
                }
                if (variant == null) {
                    variant = productVariantRepository.findAll().stream().filter(v -> !Boolean.TRUE.equals(v.getIsDeleted())).findFirst().orElse(null);
                }
                if (variant == null) {
                    throw new ResourceNotFoundException("ProductVariant", "id", line.getProductVariantId());
                }

                StockTransferDetail detail = StockTransferDetail.builder()
                        .transfer(saved)
                        .product(variant.getProduct())
                        .quantityShipped(line.getTransferQuantity())
                        .quantityReceived(BigDecimal.ZERO)
                        .build();
                detail.setIsDeleted(false);
                StockTransferDetail savedDetail = stockTransferDetailRepository.save(detail);
                savedLines.add(toStockTransferDetailDTO(savedDetail));
            }
        }

        StockTransferDTO result = toStockTransferDTO(saved);
        result.setTransferLines(savedLines);
        return result;
    }

    @Override
    @Transactional
    public StockTransferDTO updateStockTransfer(Long id, StockTransferDTO dto) {
        StockTransfer t = findStockTransferByIdOrFallback(id);

        if (dto.getStatus() != null) {
            t.setStatus(dto.getStatus());
        }
        if (dto.getApprovedBy() != null) {
            t.setApprovedBy(dto.getApprovedBy());
        }
        if (dto.getRequestedBy() != null) {
            t.setRequestedBy(dto.getRequestedBy());
        }
        if (dto.getLogisticsPartner() != null) {
            t.setLogisticsPartner(dto.getLogisticsPartner());
        }
        if (dto.getTrackingRef() != null) {
            t.setTrackingRef(dto.getTrackingRef());
        }
        if (dto.getNote() != null) {
            t.setNote(dto.getNote());
        }
        if (dto.getEstArrivalDate() != null) {
            t.setEstArrivalDate(dto.getEstArrivalDate());
        }
        if (dto.getTransferDate() != null) {
            t.setTransferDate(dto.getTransferDate());
        }
        if (dto.getFromBranchId() != null) {
            t.setFromBranch(branchRepository.findById(dto.getFromBranchId()).orElse(t.getFromBranch()));
        }
        if (dto.getToBranchId() != null) {
            t.setToBranch(branchRepository.findById(dto.getToBranchId()).orElse(t.getToBranch()));
        }

        StockTransfer saved = stockTransferRepository.save(t);

        // Update details if provided
        if (dto.getTransferLines() != null && !dto.getTransferLines().isEmpty()) {
            List<StockTransferDetail> existingDetails = stockTransferDetailRepository.findByTransferIdAndIsDeletedFalse(saved.getId());
            for (StockTransferDetail d : existingDetails) {
                d.setIsDeleted(true);
                stockTransferDetailRepository.save(d);
            }
            for (StockTransferDetailDTO line : dto.getTransferLines()) {
                ProductVariant variant = null;
                if (line.getProductVariantId() != null) {
                    variant = productVariantRepository.findById(line.getProductVariantId()).orElse(null);
                }
                if (variant == null && line.getProductId() != null) {
                    List<ProductVariant> pvs = productVariantRepository.findByProductIdAndIsDeletedFalse(line.getProductId());
                    if (!pvs.isEmpty()) variant = pvs.get(0);
                }
                if (variant == null) {
                    variant = productVariantRepository.findAll().stream().filter(v -> !Boolean.TRUE.equals(v.getIsDeleted())).findFirst().orElse(null);
                }
                if (variant != null) {
                    StockTransferDetail detail = StockTransferDetail.builder()
                            .transfer(saved)
                            .product(variant.getProduct())
                            .quantityShipped(line.getTransferQuantity())
                            .quantityReceived(BigDecimal.ZERO)
                            .build();
                    detail.setIsDeleted(false);
                    stockTransferDetailRepository.save(detail);
                }
            }
        }

        StockTransferDTO result = toStockTransferDTO(saved);
        result.setTransferLines(stockTransferDetailRepository.findByTransferIdAndIsDeletedFalse(saved.getId()).stream()
                .map(this::toStockTransferDetailDTO)
                .collect(Collectors.toList()));
        return result;
    }

    @Override
    @Transactional
    public void deleteStockTransfer(Long id) {
        StockTransfer t = findStockTransferByIdOrFallback(id);
        t.setIsDeleted(true);
        stockTransferRepository.save(t);
    }

    // --- PRODUCT BATCH ---

    @Override
    @Transactional(readOnly = true)
    public List<ProductBatchDTO> getAllProductBatches() {
        return productBatchRepository.findAllWithAssociations().stream()
                .map(b -> {
                    // Auto-recalculate status on read based on expiry date
                    if (b.getExpiryDate() != null && b.getExpiryDate().isBefore(java.time.LocalDate.now())) {
                        if (!"EXPIRED".equalsIgnoreCase(b.getQualityStatus())) {
                            b.setQualityStatus("EXPIRED");
                            b.setStatus("EXPIRED");
                        }
                    }
                    return toProductBatchDTO(b);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBatchDTO getProductBatchById(Long id) {
        ProductBatch b = productBatchRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBatch", "id", id));
        return toProductBatchDTO(b);
    }

    @Override
    @Transactional
    public ProductBatchDTO createProductBatch(ProductBatchDTO dto) {
        Product product = productRepository.findByIdAndIsDeletedFalse(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", dto.getProductId()));

        ProductBatch batch = ProductBatch.builder()
                .batchNumber(dto.getBatchNumber())
                .manufactureDate(dto.getManufactureDate())
                .expiryDate(dto.getExpiryDate())
                .status("ACTIVE")
                .product(product)
                .initialUnits(dto.getInitialUnits())
                .remainingUnits(dto.getInitialUnits())
                .unitCost(dto.getUnitCost())
                .supplierName(dto.getSupplierName())
                .location(dto.getLocation())
                .qualityStatus(dto.getQualityStatus())
                .inspector(dto.getInspector())
                .build();
        batch.setIsDeleted(false);
        ProductBatch saved = productBatchRepository.save(batch);
        return toProductBatchDTO(saved);
    }

    @Override
    @Transactional
    public ProductBatchDTO updateProductBatch(Long id, ProductBatchDTO dto) {
        ProductBatch b = productBatchRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBatch", "id", id));

        if (dto.getBatchNumber() != null && !dto.getBatchNumber().isBlank()) {
            b.setBatchNumber(dto.getBatchNumber().trim());
        }
        if (dto.getManufactureDate() != null) b.setManufactureDate(dto.getManufactureDate());
        if (dto.getExpiryDate() != null) b.setExpiryDate(dto.getExpiryDate());
        if (dto.getUnitCost() != null) b.setUnitCost(dto.getUnitCost());
        if (dto.getSupplierName() != null) b.setSupplierName(dto.getSupplierName());
        if (dto.getLocation() != null) b.setLocation(dto.getLocation());
        if (dto.getQualityStatus() != null) b.setQualityStatus(dto.getQualityStatus());
        if (dto.getInspector() != null) b.setInspector(dto.getInspector());

        b.setStatus(determineBatchStatus(b));
        ProductBatch saved = productBatchRepository.save(b);
        return toProductBatchDTO(saved);
    }



    @Override
    @Transactional
    public void deleteProductBatch(Long id) {
        ProductBatch b = productBatchRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBatch", "id", id));
        b.setIsDeleted(true);
        productBatchRepository.save(b);
    }

    // --- NEW ACTION METHODS ---

    @Override
    @Transactional
    public ImportReceiptDTO cancelImportReceipt(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.ImportCancelRequest request) {
        ImportReceipt r = importReceiptRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ImportReceipt", "id", id));
        if ("COMPLETE".equals(r.getStatus())) {
            throw new org.example.storemanager.shared.exception.inventory.InvalidStatusTransitionException(r.getStatus(), "CANCELLED");
        }
        r.setStatus("CANCELLED");
        ImportReceipt saved = importReceiptRepository.save(r);
        return toImportReceiptDTO(saved);
    }

    @Override
    @Transactional
    public CancelIssueDTO approveCancelIssue(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.CancelIssueApprovalRequest request) {
        CancelIssue c = cancelIssueRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("CancelIssue", "id", id));
        if (org.example.storemanager.shared.enums.inventory.CancelIssueStatus.APPROVED.equals(c.getStatus())
                || org.example.storemanager.shared.enums.inventory.CancelIssueStatus.COMPLETE.equals(c.getStatus())) {
            return toCancelIssueDTO(c);
        }
        c.setStatus(org.example.storemanager.shared.enums.inventory.CancelIssueStatus.APPROVED);
        if (request != null && request.getApprovalNotes() != null) {
            c.setReason(c.getReason() != null ? c.getReason() + " | " + request.getApprovalNotes() : request.getApprovalNotes());
        }
        CancelIssue saved = cancelIssueRepository.save(c);
        applyCancelIssueDeductions(saved);
        return toCancelIssueDTO(saved);
    }

    @Override
    @Transactional
    public CancelIssueDTO rejectCancelIssue(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.CancelIssueRejectRequest request) {
        CancelIssue c = cancelIssueRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("CancelIssue", "id", id));
        CancelIssueStatus prevStatus = c.getStatus();
        c.setStatus(org.example.storemanager.shared.enums.inventory.CancelIssueStatus.REJECTED);
        if (request != null && request.getRejectNotes() != null) {
            c.setReason(c.getReason() != null ? c.getReason() + " | " + request.getRejectNotes() : request.getRejectNotes());
        }
        CancelIssue saved = cancelIssueRepository.save(c);
        if (CancelIssueStatus.APPROVED.equals(prevStatus) || CancelIssueStatus.COMPLETE.equals(prevStatus)) {
            restoreCancelIssueStock(saved);
        }
        return toCancelIssueDTO(saved);
    }

    private void applyCancelIssueDeductions(CancelIssue c) {
        List<CancelIssueDetail> details = cancelIssueDetailRepository.findByCancelIssueIdAndIsDeletedFalse(c.getId());
        WarehouseZone defaultZone = warehouseService.getOrCreateDefaultZone(c.getBranch());
        String username = getCurrentUsername();
        Branch branch = c.getBranch();

        for (CancelIssueDetail detail : details) {
            Product product = detail.getProduct();
            if (product == null) continue;
            BigDecimal quantity = detail.getQuantity() != null ? detail.getQuantity() : BigDecimal.ONE;
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) continue;

            // 1. Deduct SizeInventory
            try {
                deductStock(defaultZone.getId(), branch != null ? branch.getId() : null, product.getId(),
                        null, null, quantity,
                        "CANCEL_ISSUE", c.getCancelCode(), c.getId());
            } catch (Exception e) {
                log.warn("deductStock failed for CancelIssue {}: {}", c.getId(), e.getMessage());
            }

            // 2. Deduct ProductLocation
            try {
                List<ProductLocation> locations = productLocationRepository.findByProductIdAndIsDeletedFalse(product.getId());
                BigDecimal remainingToDeduct = quantity;
                for (ProductLocation loc : locations) {
                    if (remainingToDeduct.compareTo(BigDecimal.ZERO) <= 0) break;
                    BigDecimal available = loc.getQuantity();
                    if (available == null || available.compareTo(BigDecimal.ZERO) <= 0) continue;

                    BigDecimal deduct = available.min(remainingToDeduct);
                    loc.setQuantity(available.subtract(deduct));
                    productLocationRepository.save(loc);
                    remainingToDeduct = remainingToDeduct.subtract(deduct);
                }
            } catch (Exception e) {
                log.warn("Failed to update ProductLocation for CancelIssue {}: {}", c.getId(), e.getMessage());
            }

            // 3. Deduct InventoryBalance and record InventoryTransaction
            ProductVariant variant = productVariantRepository.findByProductIdAndIsDeletedFalse(product.getId()).stream().findFirst().orElse(null);
            if (variant != null && branch != null) {
                try {
                    InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(variant.getId(), branch.getId())
                            .orElseGet(() -> InventoryBalance.builder()
                                    .productVariant(variant)
                                    .branch(branch)
                                    .availableQuantity(BigDecimal.ZERO)
                                    .reservedQuantity(BigDecimal.ZERO)
                                    .damagedQuantity(BigDecimal.ZERO)
                                    .build());

                    BigDecimal beforeQty = balance.getAvailableQuantity() != null ? balance.getAvailableQuantity() : BigDecimal.ZERO;
                    BigDecimal afterQty = beforeQty.subtract(quantity).max(BigDecimal.ZERO);
                    balance.setAvailableQuantity(afterQty);
                    balance.setLastUpdated(LocalDateTime.now());
                    inventoryBalanceRepository.save(balance);

                    String txCode = "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                            + "-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4);

                    InventoryTransaction tx = InventoryTransaction.builder()
                            .transactionCode(txCode)
                            .productVariant(variant)
                            .sourceBranch(branch)
                            .transactionType(InventoryTransactionType.CANCEL_ISSUE)
                            .quantity(quantity)
                            .beforeQuantity(beforeQty)
                            .afterQuantity(afterQty)
                            .build();
                    tx.setIsDeleted(false);
                    tx.setCreatedBy(username);
                    inventoryTransactionRepository.save(tx);
                } catch (Exception e) {
                    log.warn("Failed to update InventoryBalance for CancelIssue {}: {}", c.getId(), e.getMessage());
                }
            }
        }
    }

    private void restoreCancelIssueStock(CancelIssue c) {
        List<CancelIssueDetail> details = cancelIssueDetailRepository.findByCancelIssueIdAndIsDeletedFalse(c.getId());
        WarehouseZone defaultZone = warehouseService.getOrCreateDefaultZone(c.getBranch());
        String username = getCurrentUsername();
        Branch branch = c.getBranch();

        for (CancelIssueDetail detail : details) {
            Product product = detail.getProduct();
            if (product == null) continue;
            BigDecimal quantity = detail.getQuantity() != null ? detail.getQuantity() : BigDecimal.ONE;
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) continue;

            try {
                addStock(defaultZone.getId(), branch != null ? branch.getId() : null, product.getId(),
                        null, null, quantity,
                        "CANCEL_ISSUE_REVERSED", c.getCancelCode(), c.getId());
            } catch (Exception e) {
                log.warn("addStock failed for restoreCancelIssueStock: {}", e.getMessage());
            }

            try {
                ProductLocation loc = productLocationRepository.findByProductIdAndBinIdAndIsDeletedFalse(product.getId(), defaultZone.getId())
                        .orElseGet(() -> ProductLocation.builder().product(product).quantity(BigDecimal.ZERO).build());
                loc.setQuantity((loc.getQuantity() != null ? loc.getQuantity() : BigDecimal.ZERO).add(quantity));
                loc.setIsDeleted(false);
                productLocationRepository.save(loc);
            } catch (Exception e) {
                log.warn("Failed to restore ProductLocation: {}", e.getMessage());
            }

            ProductVariant variant = productVariantRepository.findByProductIdAndIsDeletedFalse(product.getId()).stream().findFirst().orElse(null);
            if (variant != null && branch != null) {
                try {
                    InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(variant.getId(), branch.getId())
                            .orElseGet(() -> InventoryBalance.builder()
                                    .productVariant(variant)
                                    .branch(branch)
                                    .availableQuantity(BigDecimal.ZERO)
                                    .reservedQuantity(BigDecimal.ZERO)
                                    .damagedQuantity(BigDecimal.ZERO)
                                    .build());
                    BigDecimal beforeQty = balance.getAvailableQuantity() != null ? balance.getAvailableQuantity() : BigDecimal.ZERO;
                    BigDecimal afterQty = beforeQty.add(quantity);
                    balance.setAvailableQuantity(afterQty);
                    balance.setLastUpdated(LocalDateTime.now());
                    inventoryBalanceRepository.save(balance);

                    String txCode = "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                            + "-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4);
                    InventoryTransaction tx = InventoryTransaction.builder()
                            .transactionCode(txCode)
                            .productVariant(variant)
                            .sourceBranch(branch)
                            .transactionType(InventoryTransactionType.ADJUSTMENT)
                            .quantity(quantity)
                            .beforeQuantity(beforeQty)
                            .afterQuantity(afterQty)
                            .build();
                    tx.setIsDeleted(false);
                    tx.setCreatedBy(username);
                    inventoryTransactionRepository.save(tx);
                } catch (Exception e) {
                    log.warn("Failed to restore InventoryBalance: {}", e.getMessage());
                }
            }
        }
    }

    @Override
    @Transactional
    public StockTransferDTO completeStockTransfer(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.TransferCompleteRequest request) {
        StockTransfer t = findStockTransferByIdOrFallback(id);
        if (TransferStatus.RECEIVED.name().equalsIgnoreCase(t.getStatus()) || "COMPLETED".equalsIgnoreCase(t.getStatus())) {
            return toStockTransferDTO(t);
        }

        if (!TransferStatus.SHIPPED.name().equalsIgnoreCase(t.getStatus()) && !TransferStatus.IN_TRANSIT.name().equalsIgnoreCase(t.getStatus())) {
            // Transfer has not been shipped yet; deduct stock from origin branch first
            shipStockTransfer(t.getId());
            t = stockTransferRepository.findByIdAndIsDeletedFalse(id)
                    .orElseGet(() -> findStockTransferByIdOrFallback(id));
        }

        t.setStatus(TransferStatus.RECEIVED.name());
        t.setUpdatedBy(getCurrentUsername());
        StockTransfer saved = stockTransferRepository.save(t);

        final Branch toBranch = t.getToBranch();
        List<StockTransferDetail> details = stockTransferDetailRepository.findByTransferIdAndIsDeletedFalse(t.getId());
        WarehouseZone toZone = warehouseService.getOrCreateDefaultZone(toBranch);
        String username = getCurrentUsername();

        // Ensure default WarehouseBin exists for toZone & toBranch so targetBin is never null
        WarehouseBin defaultTargetBin = warehouseBinRepository.findByZoneId(toZone.getId()).stream().findFirst()
                .orElseGet(() -> warehouseBinRepository.findByBranchId(toBranch.getId()).stream().findFirst()
                        .orElseGet(() -> warehouseBinRepository.findAll().stream().filter(b -> Boolean.FALSE.equals(b.getIsDeleted())).findFirst().orElse(null)));

        for (StockTransferDetail detail : details) {
            Product product = detail.getProduct();
            if (product == null) continue;

            ProductVariant variant = productVariantRepository.findByProductIdAndIsDeletedFalse(product.getId()).stream().findFirst()
                    .orElseGet(() -> productVariantRepository.findAll().stream()
                            .filter(v -> v.getProduct() != null && product.getId().equals(v.getProduct().getId()) && !Boolean.TRUE.equals(v.getIsDeleted()))
                            .findFirst()
                            .orElseGet(() -> {
                                ProductVariant newVar = ProductVariant.builder()
                                        .product(product)
                                        .sku(product.getProductCode() + "-DEF")
                                        .price(product.getBasePrice() != null ? product.getBasePrice() : BigDecimal.ZERO)
                                        .build();
                                newVar.setIsDeleted(false);
                                return productVariantRepository.save(newVar);
                            }));

            BigDecimal quantity = detail.getQuantityShipped();
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                quantity = BigDecimal.ONE;
            }

            // Update quantityReceived on detail so UI shows received qty
            detail.setQuantityReceived(quantity);
            stockTransferDetailRepository.save(detail);

            // Target physical size inventory add
            try {
                addStock(toZone.getId(), toBranch.getId(), product.getId(),
                        null, null, quantity,
                        "TRANSFER_IN", t.getTransferCode(), t.getId());
            } catch (Exception e) {
                log.warn("addStock failed for completeStockTransfer: {}", e.getMessage());
            }

            // Target ProductLocation add
            try {
                ProductLocation loc = productLocationRepository.findByProductIdAndBinIdAndIsDeletedFalse(product.getId(), defaultTargetBin.getId())
                        .orElseGet(() -> ProductLocation.builder()
                                .product(product)
                                .bin(defaultTargetBin)
                                .quantity(BigDecimal.ZERO)
                                .build());
                loc.setBin(defaultTargetBin);
                loc.setQuantity((loc.getQuantity() != null ? loc.getQuantity() : BigDecimal.ZERO).add(quantity));
                loc.setIsDeleted(false);
                productLocationRepository.save(loc);
            } catch (Exception e) {
                log.warn("Failed to update ProductLocation on completeStockTransfer: {}", e.getMessage());
            }

            if (variant != null) {
                // Increase InventoryBalance at destination branch
                InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(variant.getId(), toBranch.getId())
                        .orElseGet(() -> InventoryBalance.builder()
                                .productVariant(variant)
                                .branch(toBranch)
                                .availableQuantity(BigDecimal.ZERO)
                                .reservedQuantity(BigDecimal.ZERO)
                                .damagedQuantity(BigDecimal.ZERO)
                                .build());
                BigDecimal beforeQty = balance.getAvailableQuantity() != null ? balance.getAvailableQuantity() : BigDecimal.ZERO;
                BigDecimal afterQty = beforeQty.add(quantity);
                balance.setAvailableQuantity(afterQty);
                balance.setLastUpdated(LocalDateTime.now());
                inventoryBalanceRepository.save(balance);

                // Create InventoryTransaction = TRANSFER_IN
                String txCode = "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                        + "-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4);

                InventoryTransaction tx = InventoryTransaction.builder()
                        .transactionCode(txCode)
                        .productVariant(variant)
                        .sourceBranch(t.getToBranch())
                        .transactionType(InventoryTransactionType.TRANSFER_IN)
                        .quantity(quantity)
                        .beforeQuantity(beforeQty)
                        .afterQuantity(afterQty)
                        .build();
                tx.setIsDeleted(false);
                tx.setCreatedBy(username);
                inventoryTransactionRepository.save(tx);
            }
        }

        // Auto-create GRN (ImportReceipt) at destination branch safely
        try {
            String grnCode = "GRN-TR-" + t.getTransferCode();
            boolean grnExists = importReceiptRepository.findAll().stream()
                    .anyMatch(ir -> grnCode.equalsIgnoreCase(ir.getReceiptCode()));
            if (!grnExists) {
                BigDecimal totalAmount = BigDecimal.ZERO;
                ImportReceipt grn = ImportReceipt.builder()
                        .receiptCode(grnCode)
                        .receiptDate(LocalDateTime.now())
                        .status("COMPLETE")
                        .branch(toBranch)
                        .note("Nhập kho tự động từ phiếu chuyển kho " + t.getTransferCode())
                        .inspectedBy(username)
                        .totalAmount(BigDecimal.ZERO)
                        .discount(BigDecimal.ZERO)
                        .tax(BigDecimal.ZERO)
                        .build();
                grn.setIsDeleted(false);
                ImportReceipt savedGrn = importReceiptRepository.save(grn);

                for (StockTransferDetail detail : details) {
                    Product product = detail.getProduct();
                    if (product == null) continue;
                    ProductVariant variant = productVariantRepository.findByProductIdAndIsDeletedFalse(product.getId()).stream().findFirst()
                            .orElseGet(() -> productVariantRepository.findAll().stream()
                                    .filter(v -> v.getProduct() != null && product.getId().equals(v.getProduct().getId()) && !Boolean.TRUE.equals(v.getIsDeleted()))
                                    .findFirst().orElse(null));
                    if (variant == null) continue;

                    BigDecimal unitCost = variant.getPrice() != null ? variant.getPrice() : (product.getBasePrice() != null ? product.getBasePrice() : BigDecimal.ZERO);
                    BigDecimal qty = detail.getQuantityShipped() != null ? detail.getQuantityShipped() : BigDecimal.ONE;
                    BigDecimal subTotal = unitCost.multiply(qty);
                    totalAmount = totalAmount.add(subTotal);

                    ImportReceiptDetail d = ImportReceiptDetail.builder()
                            .receipt(savedGrn)
                            .product(product)
                            .productVariant(variant)
                            .productNameSnapshot(product.getName())
                            .skuSnapshot(product.getProductCode())
                            .barcodeSnapshot(variant.getBarcode())
                            .unitCostSnapshot(unitCost)
                            .unitPrice(unitCost)
                            .quantity(qty)
                            .subTotal(subTotal)
                            .batchNumber("BATCH-TR-" + t.getTransferCode())
                            .targetBin(defaultTargetBin)
                            .build();
                    d.setIsDeleted(false);
                    importReceiptDetailRepository.save(d);
                }
                savedGrn.setTotalAmount(totalAmount);
                importReceiptRepository.save(savedGrn);
            }
        } catch (Exception e) {
            log.warn("Failed to auto-create ImportReceipt for StockTransfer {}: {}", t.getTransferCode(), e.getMessage());
        }
        return toStockTransferDTO(saved);
    }

    @Override
    @Transactional
    public StockTransferDTO cancelStockTransfer(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.TransferCancelRequest request) {
        StockTransfer t = stockTransferRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", "id", id));
        if (TransferStatus.RECEIVED.name().equalsIgnoreCase(t.getStatus()) || "COMPLETED".equalsIgnoreCase(t.getStatus())) {
            throw new org.example.storemanager.shared.exception.inventory.InvalidStatusTransitionException(t.getStatus(), "CANCELLED");
        }
        String previousStatus = t.getStatus();
        t.setStatus("CANCELLED");
        if (request != null && request.getCancelReason() != null) {
            t.setNote(t.getNote() != null ? t.getNote() + " | " + request.getCancelReason() : request.getCancelReason());
        }
        t.setUpdatedBy(getCurrentUsername());
        StockTransfer saved = stockTransferRepository.save(t);

        // If goods were already shipped, restore them back to fromBranch
        if (TransferStatus.SHIPPED.name().equalsIgnoreCase(previousStatus) || TransferStatus.IN_TRANSIT.name().equalsIgnoreCase(previousStatus)) {
            final Branch fromBranch = t.getFromBranch();
            List<StockTransferDetail> details = stockTransferDetailRepository.findByTransferIdAndIsDeletedFalse(id);
            WarehouseZone fromZone = warehouseService.getOrCreateDefaultZone(fromBranch);
            String username = getCurrentUsername();

            for (StockTransferDetail detail : details) {
                Product product = detail.getProduct();
                if (product == null) continue;
                ProductVariant variant = productVariantRepository.findByProductIdAndIsDeletedFalse(product.getId()).stream().findFirst().orElse(null);
                BigDecimal quantity = detail.getQuantityShipped();
                if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                    quantity = BigDecimal.ONE;
                }

                // Add stock back to SizeInventory
                try {
                    addStock(fromZone.getId(), fromBranch.getId(), product.getId(),
                            null, null, quantity,
                            "TRANSFER_CANCEL_RESTORE", t.getTransferCode(), t.getId());
                } catch (Exception e) {
                    log.warn("addStock failed for cancelStockTransfer: {}", e.getMessage());
                }

                // Restore ProductLocation
                try {
                    ProductLocation loc = productLocationRepository.findByProductIdAndBinIdAndIsDeletedFalse(product.getId(), fromZone.getId())
                            .orElseGet(() -> ProductLocation.builder().product(product).quantity(BigDecimal.ZERO).build());
                    loc.setQuantity((loc.getQuantity() != null ? loc.getQuantity() : BigDecimal.ZERO).add(quantity));
                    loc.setIsDeleted(false);
                    productLocationRepository.save(loc);
                } catch (Exception e) {
                    log.warn("Failed to restore ProductLocation: {}", e.getMessage());
                }

                if (variant != null && fromBranch != null) {
                    try {
                        InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(variant.getId(), fromBranch.getId())
                                .orElseGet(() -> InventoryBalance.builder()
                                        .productVariant(variant)
                                        .branch(fromBranch)
                                        .availableQuantity(BigDecimal.ZERO)
                                        .reservedQuantity(BigDecimal.ZERO)
                                        .damagedQuantity(BigDecimal.ZERO)
                                        .build());
                        BigDecimal beforeQty = balance.getAvailableQuantity() != null ? balance.getAvailableQuantity() : BigDecimal.ZERO;
                        BigDecimal afterQty = beforeQty.add(quantity);
                        balance.setAvailableQuantity(afterQty);
                        balance.setLastUpdated(LocalDateTime.now());
                        inventoryBalanceRepository.save(balance);

                        String txCode = "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                                + "-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4);

                        InventoryTransaction tx = InventoryTransaction.builder()
                                .transactionCode(txCode)
                                .productVariant(variant)
                                .sourceBranch(fromBranch)
                                .transactionType(InventoryTransactionType.ADJUSTMENT)
                                .quantity(quantity)
                                .beforeQuantity(beforeQty)
                                .afterQuantity(afterQty)
                                .build();
                        tx.setIsDeleted(false);
                        tx.setCreatedBy(username);
                        inventoryTransactionRepository.save(tx);
                    } catch (Exception e) {
                        log.warn("Failed to restore InventoryBalance for cancelStockTransfer: {}", e.getMessage());
                    }
                }
            }
        }

        return toStockTransferDTO(saved);
    }

    @Override
    @Transactional
    public ProductBatchDTO adjustProductBatch(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.BatchAdjustRequest request) {
        ProductBatch b = productBatchRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBatch", "id", id));

        if (request.getAdjustedQuantity() == null || request.getAdjustedQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số lượng điều chỉnh không được nhỏ hơn 0");
        }

        BigDecimal oldQty = b.getRemainingUnits() != null ? b.getRemainingUnits() : BigDecimal.ZERO;
        BigDecimal newQty = request.getAdjustedQuantity();
        BigDecimal changeQty = newQty.subtract(oldQty);

        b.setRemainingUnits(newQty);
        b.setStatus(determineBatchStatus(b));
        ProductBatch saved = productBatchRepository.save(b);

        if (changeQty.compareTo(BigDecimal.ZERO) != 0 && b.getProduct() != null) {
            Branch mainBranch = branchRepository.findByIsDeletedFalse().stream().findFirst().orElse(null);
            if (mainBranch != null) {
                org.example.storemanager.modules.inventory.entity.StockLedger ledger =
                        org.example.storemanager.modules.inventory.entity.StockLedger.builder()
                                .transactionType("BATCH_ADJUSTMENT")
                                .product(b.getProduct())
                                .branch(mainBranch)
                                .batch(saved)
                                .changeQty(changeQty)
                                .balanceAfter(newQty)
                                .build();
                ledger.setIsDeleted(false);
                ledger.setCreatedBy(getCurrentUsername());
                stockLedgerRepository.save(ledger);
            }
        }

        return toProductBatchDTO(saved);
    }

    @Override
    @Transactional
    public ProductBatchDTO expireProductBatch(Long id) {
        ProductBatch b = productBatchRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBatch", "id", id));
        b.setStatus("EXPIRED");
        ProductBatch saved = productBatchRepository.save(b);
        return toProductBatchDTO(saved);
    }

    private String determineBatchStatus(ProductBatch b) {
        if ("INACTIVE".equalsIgnoreCase(b.getStatus())) {
            return "INACTIVE";
        }
        if (b.getRemainingUnits() != null && b.getRemainingUnits().compareTo(BigDecimal.ZERO) <= 0) {
            return "DEPLETED";
        }
        if (b.getExpiryDate() != null && b.getExpiryDate().isBefore(java.time.LocalDate.now())) {
            return "EXPIRED";
        }
        return "ACTIVE";
    }



    @Override
    public List<ProductBatchDTO> getExpiringProductBatches(int days) {
        LocalDateTime threshold = LocalDateTime.now().plusDays(days);
        return productBatchRepository.findAll().stream()
                .filter(b -> !b.getIsDeleted() && "ACTIVE".equals(b.getStatus()) && b.getExpiryDate() != null)
                .filter(b -> b.getExpiryDate().atStartOfDay().isBefore(threshold) || b.getExpiryDate().atStartOfDay().isEqual(threshold))
                .map(this::toProductBatchDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryCheckDTO> getAllInventoryChecks() {
        return inventoryCheckRepository.findAll().stream()
                .filter(c -> !c.getIsDeleted())
                .map(this::toInventoryCheckDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InventoryCheckDTO getInventoryCheckById(Long id) {
        InventoryCheck c = inventoryCheckRepository.findById(id)
                .filter(check -> !check.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("InventoryCheck", "id", id));
        List<InventoryCheckDetail> details = inventoryCheckDetailRepository.findByCheckIdAndIsDeletedFalse(id);
        InventoryCheckDTO dto = toInventoryCheckDTO(c, details);
        dto.setCheckLines(details.stream()
                .map(this::toInventoryCheckDetailDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional
    public InventoryCheckDTO createInventoryCheck(InventoryCheckDTO dto) {
        Branch branch = branchRepository.findByIdAndIsDeletedFalse(dto.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));
        WarehouseZone zone = dto.getWarehouseZoneId() != null ?
                warehouseZoneRepository.findByIdAndIsDeletedFalse(dto.getWarehouseZoneId()).orElse(null) :
                warehouseService.getOrCreateDefaultZone(branch);

        InventoryCheck c = InventoryCheck.builder()
                .checkCode(dto.getCheckCode() != null ? dto.getCheckCode() : "CHK-" + System.currentTimeMillis())
                .checkDate(dto.getCheckDate() != null ? dto.getCheckDate() : LocalDateTime.now())
                .status("DRAFT")
                .branch(branch)
                .warehouseZone(zone)
                .build();
        c.setIsDeleted(false);
        InventoryCheck saved = inventoryCheckRepository.save(c);

        List<InventoryCheckDetailDTO> savedLines = new ArrayList<>();
        if (dto.getCheckLines() != null) {
            for (InventoryCheckDetailDTO line : dto.getCheckLines()) {
                Product product = productRepository.findById(line.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", line.getProductId()));
                InventoryCheckDetail detail = InventoryCheckDetail.builder()
                        .check(saved)
                        .product(product)
                        .systemQty(line.getSystemQty() != null ? line.getSystemQty() : BigDecimal.ZERO)
                        .actualQty(line.getActualQty() != null ? line.getActualQty() : BigDecimal.ZERO)
                        .diffQty(line.getDiffQty())
                        .reason(line.getReason())
                        .build();
                detail.setIsDeleted(false);
                savedLines.add(toInventoryCheckDetailDTO(inventoryCheckDetailRepository.save(detail)));
            }
        }
        
        InventoryCheckDTO response = toInventoryCheckDTO(saved);
        response.setCheckLines(savedLines);
        return response;
    }

    @Override
    @Transactional
    public InventoryCheckDTO updateInventoryCheck(Long id, InventoryCheckDTO dto) {
        InventoryCheck c = inventoryCheckRepository.findById(id)
                .filter(check -> !check.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("InventoryCheck", "id", id));
        c.setStatus(dto.getStatus() != null ? dto.getStatus() : c.getStatus());
        if (dto.getCheckDate() != null) c.setCheckDate(dto.getCheckDate());
        InventoryCheck saved = inventoryCheckRepository.save(c);

        if (dto.getCheckLines() != null) {
            List<InventoryCheckDetail> existing = inventoryCheckDetailRepository.findByCheckIdAndIsDeletedFalse(id);
            for (InventoryCheckDetail d : existing) {
                d.setIsDeleted(true);
                inventoryCheckDetailRepository.save(d);
            }
            for (InventoryCheckDetailDTO line : dto.getCheckLines()) {
                if (line.getProductId() == null) continue;
                Product product = productRepository.findById(line.getProductId()).orElse(null);
                if (product == null) continue;
                BigDecimal systemQty = line.getSystemQty() != null ? line.getSystemQty() : BigDecimal.ZERO;
                BigDecimal actualQty = line.getActualQty() != null ? line.getActualQty() : BigDecimal.ZERO;
                BigDecimal diffQty = line.getDiffQty() != null ? line.getDiffQty() : actualQty.subtract(systemQty);
                InventoryCheckDetail detail = InventoryCheckDetail.builder()
                        .check(saved)
                        .product(product)
                        .systemQty(systemQty)
                        .actualQty(actualQty)
                        .diffQty(diffQty)
                        .reason(line.getReason())
                        .build();
                detail.setIsDeleted(false);
                inventoryCheckDetailRepository.save(detail);
            }
        }

        return toInventoryCheckDTO(saved);
    }

    @Override
    @Transactional
    public void deleteInventoryCheck(Long id) {
        InventoryCheck c = inventoryCheckRepository.findById(id)
                .filter(check -> !check.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("InventoryCheck", "id", id));
        c.setIsDeleted(true);
        inventoryCheckRepository.save(c);
    }

    @Override
    @Transactional
    public InventoryCheckDTO approveInventoryCheck(Long id) {
        InventoryCheck c = inventoryCheckRepository.findById(id)
                .filter(check -> !check.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("InventoryCheck", "id", id));
        if ("BALANCED".equals(c.getStatus())) {
            return toInventoryCheckDTO(c);
        }
        c.setStatus("BALANCED");
        InventoryCheck saved = inventoryCheckRepository.save(c);

        WarehouseZone zone = c.getWarehouseZone();
        if (zone == null && c.getBranch() != null) {
            zone = warehouseService.getOrCreateDefaultZone(c.getBranch());
        }
        Long zoneId = zone != null ? zone.getId() : null;
        Long branchId = c.getBranch() != null ? c.getBranch().getId() : null;

        List<InventoryCheckDetail> details = inventoryCheckDetailRepository.findByCheckIdAndIsDeletedFalse(id);
        
        for (InventoryCheckDetail detail : details) {
            BigDecimal diff = detail.getDiffQty();
            if (diff == null && detail.getActualQty() != null && detail.getSystemQty() != null) {
                diff = detail.getActualQty().subtract(detail.getSystemQty());
            }
            if (diff != null && diff.compareTo(BigDecimal.ZERO) > 0 && zoneId != null && branchId != null && detail.getProduct() != null) {
                // actual > system => add stock
                try {
                    addStock(zoneId, branchId, detail.getProduct().getId(),
                            null, null, diff, "CHECK_ADJUST", c.getCheckCode(), c.getId());
                } catch (Exception e) {
                    log.warn("addStock failed for approveInventoryCheck: {}", e.getMessage());
                }
            } else if (diff != null && diff.compareTo(BigDecimal.ZERO) < 0 && zoneId != null && branchId != null && detail.getProduct() != null) {
                // actual < system => deduct stock
                try {
                    deductStock(zoneId, branchId, detail.getProduct().getId(),
                            null, null, diff.abs(), "CHECK_ADJUST", c.getCheckCode(), c.getId());
                } catch (Exception e) {
                    log.warn("deductStock failed for approveInventoryCheck: {}", e.getMessage());
                }
            }

            // Sync InventoryBalance and record InventoryTransaction
            if (diff != null && diff.compareTo(BigDecimal.ZERO) != 0 && branchId != null && detail.getProduct() != null) {
                ProductVariant variant = productVariantRepository.findByProductIdAndIsDeletedFalse(detail.getProduct().getId()).stream().findFirst().orElse(null);
                if (variant != null && c.getBranch() != null) {
                    try {
                        InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(variant.getId(), branchId)
                                .orElseGet(() -> InventoryBalance.builder()
                                        .productVariant(variant)
                                        .branch(c.getBranch())
                                        .availableQuantity(BigDecimal.ZERO)
                                        .reservedQuantity(BigDecimal.ZERO)
                                        .damagedQuantity(BigDecimal.ZERO)
                                        .build());
                        BigDecimal balBefore = balance.getAvailableQuantity() != null ? balance.getAvailableQuantity() : BigDecimal.ZERO;
                        BigDecimal balAfter = balBefore.add(diff).max(BigDecimal.ZERO);
                        balance.setAvailableQuantity(balAfter);
                        balance.setLastUpdated(LocalDateTime.now());
                        inventoryBalanceRepository.save(balance);

                        String txCode = "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                                + "-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4);
                        InventoryTransaction tx = InventoryTransaction.builder()
                                .transactionCode(txCode)
                                .productVariant(variant)
                                .sourceBranch(c.getBranch())
                                .transactionType(InventoryTransactionType.ADJUSTMENT)
                                .quantity(diff.abs())
                                .beforeQuantity(balBefore)
                                .afterQuantity(balAfter)
                                .build();
                        tx.setIsDeleted(false);
                        tx.setCreatedBy(getCurrentUsername());
                        inventoryTransactionRepository.save(tx);
                    } catch (Exception e) {
                        log.warn("Failed to sync InventoryBalance for approveInventoryCheck: {}", e.getMessage());
                    }
                }
            }
        }
        return toInventoryCheckDTO(saved);
    }

    // --- INTERNAL DTO MAPPERS ---

    private InventoryCheckDTO toInventoryCheckDTO(InventoryCheck c) {
        List<InventoryCheckDetail> details = inventoryCheckDetailRepository.findByCheckIdAndIsDeletedFalse(c.getId());
        return toInventoryCheckDTO(c, details);
    }

    private InventoryCheckDTO toInventoryCheckDTO(InventoryCheck c, List<InventoryCheckDetail> details) {
        List<InventoryCheckDetail> lines = details != null ? details : List.of();
        int totalItems = lines.size();
        int discrepancyCount = (int) lines.stream()
                .filter(d -> d.getDiffQty() != null && d.getDiffQty().compareTo(BigDecimal.ZERO) != 0)
                .count();
        BigDecimal netVariance = lines.stream()
                .map(d -> d.getDiffQty() != null ? d.getDiffQty() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return InventoryCheckDTO.builder()
                .id(c.getId())
                .checkCode(c.getCheckCode())
                .checkDate(c.getCheckDate())
                .status(c.getStatus())
                .branchId(c.getBranch() != null ? c.getBranch().getId() : null)
                .branchName(c.getBranch() != null ? c.getBranch().getBranchName() : null)
                .warehouseZoneId(c.getWarehouseZone() != null ? c.getWarehouseZone().getId() : null)
                .warehouseZoneName(c.getWarehouseZone() != null ? c.getWarehouseZone().getZoneName() : null)
                .totalItems(totalItems)
                .discrepancyCount(discrepancyCount)
                .netVariance(netVariance)
                .createdBy(c.getCreatedBy())
                .checkLines(lines.stream().map(this::toInventoryCheckDetailDTO).collect(Collectors.toList()))
                .build();
    }

    private InventoryCheckDetailDTO toInventoryCheckDetailDTO(InventoryCheckDetail d) {
        return InventoryCheckDetailDTO.builder()
                .id(d.getId())
                .productId(d.getProduct() != null ? d.getProduct().getId() : null)
                .productName(d.getProduct() != null ? d.getProduct().getName() : null)
                .sku(d.getProduct() != null ? d.getProduct().getProductCode() : null)
                .systemQty(d.getSystemQty())
                .actualQty(d.getActualQty())
                .diffQty(d.getDiffQty())
                .reason(d.getReason())
                .build();
    }

    private ImportReceiptDTO toImportReceiptDTO(ImportReceipt r) {
        List<ImportReceiptDetail> details = importReceiptDetailRepository.findByReceiptIdAndIsDeletedFalse(r.getId());
        List<ImportReceiptDetailDTO> lines = details.stream()
                .map(this::toImportReceiptDetailDTO)
                .collect(Collectors.toList());

        return ImportReceiptDTO.builder()
                .id(r.getId())
                .receiptCode(r.getReceiptCode())
                .receiptDate(r.getReceiptDate())
                .totalAmount(r.getTotalAmount())
                .discount(r.getDiscount())
                .tax(r.getTax())
                .status(r.getStatus())
                .branchId(r.getBranch() != null ? r.getBranch().getId() : null)
                .branchName(r.getBranch() != null ? r.getBranch().getBranchName() : null)
                .supplierId(r.getSupplier() != null ? r.getSupplier().getId() : null)
                .supplierName(r.getSupplier() != null ? r.getSupplier().getName() : null)
                .purchaseOrderId(r.getPurchaseOrder() != null ? r.getPurchaseOrder().getId() : null)
                .purchaseOrderCode(r.getPurchaseOrder() != null ? r.getPurchaseOrder().getPoCode() : null)
                .createdBy(r.getCreatedBy())
                .inspectedBy(r.getInspectedBy())
                .note(r.getNote())
                .receiptLines(lines)
                .build();
    }

    private ImportReceiptDetailDTO toImportReceiptDetailDTO(ImportReceiptDetail d) {
        return ImportReceiptDetailDTO.builder()
                .id(d.getId())
                .productVariantId(d.getProductVariant() != null ? d.getProductVariant().getId() : null)
                .productName(d.getProductNameSnapshot())
                .sku(d.getSkuSnapshot())
                .barcode(d.getBarcodeSnapshot())
                .quantity(d.getQuantity())
                .unitCost(d.getUnitCostSnapshot())
                .subTotal(d.getSubTotal())
                .batchNumber(d.getBatchNumber())
                .expiryDate(d.getExpiryDate())
                .targetBinId(d.getTargetBin() != null ? d.getTargetBin().getId() : null)
                .targetBinCode(d.getTargetBin() != null ? d.getTargetBin().getBinCode() : null)
                .build();
    }


    private ReturnToSupplierDTO toReturnToSupplierDTO(ReturnToSupplier r) {
        List<ReturnToSupplierDetail> details = returnToSupplierDetailRepository.findByReturnReceiptIdAndIsDeletedFalse(r.getId());
        List<ReturnToSupplierDetailDTO> lines = details.stream()
                .map(this::toReturnToSupplierDetailDTO)
                .collect(Collectors.toList());

        return ReturnToSupplierDTO.builder()
                .id(r.getId())
                .returnCode(r.getReturnCode())
                .returnDate(r.getReturnDate())
                .totalAmount(r.getTotalAmount())
                .status(r.getStatus() != null ? r.getStatus().name() : null)
                .reason(r.getReason())
                .branchId(r.getBranch() != null ? r.getBranch().getId() : null)
                .branchName(r.getBranch() != null ? r.getBranch().getBranchName() : null)
                .supplierId(r.getSupplier() != null ? r.getSupplier().getId() : null)
                .supplierName(r.getSupplier() != null ? r.getSupplier().getName() : null)
                .grnRefNumber(r.getGrnRefNumber())
                .createdBy(r.getCreatedBy())
                .note(r.getNote())
                .returnLines(lines)
                .build();
    }

    private ReturnToSupplierDetailDTO toReturnToSupplierDetailDTO(ReturnToSupplierDetail d) {
        return ReturnToSupplierDetailDTO.builder()
                .id(d.getId())
                .productVariantId(d.getProduct() != null ? d.getProduct().getId() : null)
                .productName(d.getProduct() != null ? d.getProduct().getName() : null)
                .sku(d.getProduct() != null ? d.getProduct().getProductCode() : null)
                .quantity(d.getQuantity())
                .unitCost(d.getUnitPrice())
                .subTotal(d.getSubTotal())
                .build();
    }

    private CancelIssueDTO toCancelIssueDTO(CancelIssue c) {
        List<CancelIssueDetail> details = cancelIssueDetailRepository.findByCancelIssueIdAndIsDeletedFalse(c.getId());
        List<CancelIssueDetailDTO> lines = details.stream()
                .map(this::toCancelIssueDetailDTO)
                .collect(Collectors.toList());

        return CancelIssueDTO.builder()
                .id(c.getId())
                .cancelCode(c.getCancelCode())
                .cancelDate(c.getCancelDate())
                .totalValue(c.getTotalValue())
                .reason(c.getReason())
                .status(c.getStatus() != null ? c.getStatus().name() : null)
                .branchId(c.getBranch() != null ? c.getBranch().getId() : null)
                .branchName(c.getBranch() != null ? c.getBranch().getBranchName() : null)
                .createdBy(c.getCreatedBy())
                .note(c.getNote())
                .cancelLines(lines)
                .build();
    }

    private CancelIssueDetailDTO toCancelIssueDetailDTO(CancelIssueDetail d) {
        return CancelIssueDetailDTO.builder()
                .id(d.getId())
                .productVariantId(d.getProduct() != null ? d.getProduct().getId() : null)
                .productCode(d.getProduct() != null ? d.getProduct().getProductCode() : null)
                .productName(d.getProduct() != null ? d.getProduct().getName() : null)
                .quantity(d.getQuantity())
                .subTotal(d.getSubTotal())
                .build();
    }

    private StockTransferDTO toStockTransferDTO(StockTransfer t) {
        List<StockTransferDetail> details = stockTransferDetailRepository.findByTransferIdAndIsDeletedFalse(t.getId());
        List<StockTransferDetailDTO> lines = details.stream()
                .map(this::toStockTransferDetailDTO)
                .collect(Collectors.toList());

        return StockTransferDTO.builder()
                .id(t.getId())
                .transferCode(t.getTransferCode())
                .transferDate(t.getTransferDate())
                .status(t.getStatus())
                .fromBranchId(t.getFromBranch() != null ? t.getFromBranch().getId() : null)
                .fromBranchName(t.getFromBranch() != null ? t.getFromBranch().getBranchName() : null)
                .toBranchId(t.getToBranch() != null ? t.getToBranch().getId() : null)
                .toBranchName(t.getToBranch() != null ? t.getToBranch().getBranchName() : null)
                .logisticsPartner(t.getLogisticsPartner())
                .trackingRef(t.getTrackingRef())
                .requestedBy(t.getRequestedBy())
                .approvedBy(t.getApprovedBy())
                .estArrivalDate(t.getEstArrivalDate())
                .createdBy(t.getCreatedBy())
                .note(t.getNote())
                .transferLines(lines)
                .build();
    }

    private StockTransferDetailDTO toStockTransferDetailDTO(StockTransferDetail d) {
        return StockTransferDetailDTO.builder()
                .id(d.getId())
                .productId(d.getProduct() != null ? d.getProduct().getId() : null)
                .productVariantId(d.getProduct() != null ? d.getProduct().getId() : null)
                .productCode(d.getProduct() != null ? d.getProduct().getProductCode() : null)
                .productName(d.getProduct() != null ? d.getProduct().getName() : null)
                .transferQuantity(d.getQuantityShipped())
                .build();
    }

    private ProductBatchDTO toProductBatchDTO(ProductBatch b) {
        return ProductBatchDTO.builder()
                .id(b.getId())
                .batchNumber(b.getBatchNumber())
                .manufactureDate(b.getManufactureDate())
                .expiryDate(b.getExpiryDate())
                .status(b.getStatus())
                .productId(b.getProduct() != null ? b.getProduct().getId() : null)
                .productName(b.getProduct() != null ? b.getProduct().getName() : null)
                .sku(b.getProduct() != null ? b.getProduct().getProductCode() : null)
                .initialUnits(b.getInitialUnits())
                .remainingUnits(b.getRemainingUnits())
                .unitCost(b.getUnitCost())
                .supplierName(b.getSupplierName())
                .location(b.getLocation())
                .qualityStatus(b.getQualityStatus())
                .inspector(b.getInspector())
                .notes(b.getNote())
                .createdBy(b.getCreatedBy())
                .build();
    }

    // --- Return to Supplier Actions ---
    @Override
    @Transactional
    public ReturnToSupplierDTO submitReturnToSupplier(Long id) {
        ReturnToSupplier r = returnToSupplierRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnToSupplier", "id", id));
        r.setStatus(ReturnToSupplierStatus.PENDING_APPROVAL);
        r.setUpdatedBy(getCurrentUsername());
        return toReturnToSupplierDTO(returnToSupplierRepository.save(r));
    }

    @Override
    @Transactional
    public ReturnToSupplierDTO executeReturnToSupplier(Long id) {
        ReturnToSupplier r = returnToSupplierRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnToSupplier", "id", id));
        
        if (ReturnToSupplierStatus.COMPLETE.equals(r.getStatus())) {
            return toReturnToSupplierDTO(r);
        }

        boolean alreadyDeducted = ReturnToSupplierStatus.APPROVED_CREDIT_NOTE.equals(r.getStatus())
                || ReturnToSupplierStatus.APPROVED.equals(r.getStatus());
        r.setStatus(ReturnToSupplierStatus.COMPLETE);
        r.setUpdatedBy(getCurrentUsername());
        ReturnToSupplier saved = returnToSupplierRepository.save(r);

        if (!alreadyDeducted) {
            applyReturnToSupplierDeductions(saved);
        }

        return toReturnToSupplierDTO(saved);
    }

    @Override
    @Transactional
    public ReturnToSupplierDTO cancelReturnToSupplier(Long id) {
        ReturnToSupplier r = returnToSupplierRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnToSupplier", "id", id));
        ReturnToSupplierStatus prevStatus = r.getStatus();
        r.setStatus(ReturnToSupplierStatus.CANCELLED);
        r.setUpdatedBy(getCurrentUsername());
        ReturnToSupplier saved = returnToSupplierRepository.save(r);

        if (prevStatus == ReturnToSupplierStatus.APPROVED_CREDIT_NOTE
                || prevStatus == ReturnToSupplierStatus.APPROVED
                || prevStatus == ReturnToSupplierStatus.COMPLETE) {
            restoreReturnToSupplierStock(saved);
        }
        return toReturnToSupplierDTO(saved);
    }

    // --- Cancel Issue Actions ---
    @Override
    @Transactional
    public CancelIssueDTO submitCancelIssue(Long id) {
        CancelIssue r = cancelIssueRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("CancelIssue", "id", id));
        r.setStatus(CancelIssueStatus.PENDING_APPROVAL);
        r.setUpdatedBy(getCurrentUsername());
        return toCancelIssueDTO(cancelIssueRepository.save(r));
    }

    @Override
    @Transactional
    public CancelIssueDTO executeCancelIssue(Long id) {
        CancelIssue r = cancelIssueRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("CancelIssue", "id", id));

        if (CancelIssueStatus.COMPLETE.equals(r.getStatus())) {
            return toCancelIssueDTO(r);
        }

        boolean alreadyDeducted = CancelIssueStatus.APPROVED.equals(r.getStatus());
        r.setStatus(CancelIssueStatus.COMPLETE);
        r.setUpdatedBy(getCurrentUsername());
        CancelIssue saved = cancelIssueRepository.save(r);

        if (!alreadyDeducted) {
            applyCancelIssueDeductions(saved);
        }

        return toCancelIssueDTO(saved);
    }

    @Override
    @Transactional
    public CancelIssueDTO cancelCancelIssue(Long id) {
        CancelIssue r = cancelIssueRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("CancelIssue", "id", id));
        CancelIssueStatus prevStatus = r.getStatus();
        r.setStatus(CancelIssueStatus.CANCELLED);
        r.setUpdatedBy(getCurrentUsername());
        CancelIssue saved = cancelIssueRepository.save(r);

        if (CancelIssueStatus.APPROVED.equals(prevStatus) || CancelIssueStatus.COMPLETE.equals(prevStatus)) {
            restoreCancelIssueStock(saved);
        }
        return toCancelIssueDTO(saved);
    }

    // --- Stock Transfer Actions ---
    @Override
    @Transactional
    public StockTransferDTO submitStockTransfer(Long id) {
        StockTransfer t = findStockTransferByIdOrFallback(id);
        t.setStatus(TransferStatus.PENDING_APPROVAL.name());
        t.setUpdatedBy(getCurrentUsername());
        return toStockTransferDTO(stockTransferRepository.save(t));
    }

    @Override
    @Transactional
    public StockTransferDTO approveStockTransfer(Long id) {
        StockTransfer t = findStockTransferByIdOrFallback(id);
        t.setStatus(TransferStatus.APPROVED.name());
        t.setUpdatedBy(getCurrentUsername());
        return toStockTransferDTO(stockTransferRepository.save(t));
    }

    @Override
    @Transactional
    public StockTransferDTO shipStockTransfer(Long id) {
        StockTransfer t = findStockTransferByIdOrFallback(id);
        if (TransferStatus.SHIPPED.name().equalsIgnoreCase(t.getStatus())
                || TransferStatus.IN_TRANSIT.name().equalsIgnoreCase(t.getStatus())
                || TransferStatus.RECEIVED.name().equalsIgnoreCase(t.getStatus())
                || "COMPLETED".equalsIgnoreCase(t.getStatus())) {
            return toStockTransferDTO(t);
        }
        t.setStatus(TransferStatus.SHIPPED.name());
        t.setUpdatedBy(getCurrentUsername());
        StockTransfer saved = stockTransferRepository.save(t);

        List<StockTransferDetail> details = stockTransferDetailRepository.findByTransferIdAndIsDeletedFalse(t.getId());
        WarehouseZone fromZone = warehouseService.getOrCreateDefaultZone(t.getFromBranch());
        String username = getCurrentUsername();

        for (StockTransferDetail detail : details) {
            Product product = detail.getProduct();
            if (product == null) continue;
            ProductVariant variant = productVariantRepository.findByProductIdAndIsDeletedFalse(product.getId()).stream().findFirst().orElse(null);
            BigDecimal quantity = detail.getQuantityShipped();
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                quantity = BigDecimal.ONE;
                detail.setQuantityShipped(quantity);
                stockTransferDetailRepository.save(detail);
            }

            // 1. Deduct SizeInventory
            try {
                deductStock(fromZone.getId(), t.getFromBranch().getId(), product.getId(),
                        null, null, quantity,
                        "TRANSFER_OUT", t.getTransferCode(), t.getId());
            } catch (Exception e) {
                log.warn("deductStock failed for shipStockTransfer: {}", e.getMessage());
            }

            // 2. Deduct ProductLocation
            try {
                List<ProductLocation> locations = productLocationRepository.findByProductIdAndIsDeletedFalse(product.getId());
                BigDecimal remainingToDeduct = quantity;
                for (ProductLocation loc : locations) {
                    if (remainingToDeduct.compareTo(BigDecimal.ZERO) <= 0) break;
                    BigDecimal available = loc.getQuantity();
                    if (available == null || available.compareTo(BigDecimal.ZERO) <= 0) continue;

                    BigDecimal deduct = available.min(remainingToDeduct);
                    loc.setQuantity(available.subtract(deduct));
                    productLocationRepository.save(loc);
                    remainingToDeduct = remainingToDeduct.subtract(deduct);
                }
            } catch (Exception e) {
                log.warn("Failed to update ProductLocation for shipStockTransfer: {}", e.getMessage());
            }

            // 3. Deduct InventoryBalance at fromBranch and record InventoryTransaction
            if (variant != null && t.getFromBranch() != null) {
                try {
                    InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(variant.getId(), t.getFromBranch().getId())
                            .orElseGet(() -> InventoryBalance.builder()
                                    .productVariant(variant)
                                    .branch(t.getFromBranch())
                                    .availableQuantity(BigDecimal.ZERO)
                                    .reservedQuantity(BigDecimal.ZERO)
                                    .damagedQuantity(BigDecimal.ZERO)
                                    .build());
                    BigDecimal beforeQty = balance.getAvailableQuantity() != null ? balance.getAvailableQuantity() : BigDecimal.ZERO;
                    BigDecimal afterQty = beforeQty.subtract(quantity).max(BigDecimal.ZERO);
                    balance.setAvailableQuantity(afterQty);
                    balance.setLastUpdated(LocalDateTime.now());
                    inventoryBalanceRepository.save(balance);

                    String txCode = "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                            + "-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4);

                    InventoryTransaction tx = InventoryTransaction.builder()
                            .transactionCode(txCode)
                            .productVariant(variant)
                            .sourceBranch(t.getFromBranch())
                            .destinationBranch(t.getToBranch())
                            .transactionType(InventoryTransactionType.TRANSFER_OUT)
                            .quantity(quantity)
                            .beforeQuantity(beforeQty)
                            .afterQuantity(afterQty)
                            .build();
                    tx.setIsDeleted(false);
                    tx.setCreatedBy(username);
                    inventoryTransactionRepository.save(tx);
                } catch (Exception e) {
                    log.warn("Failed to update InventoryBalance for shipStockTransfer: {}", e.getMessage());
                }
            }
        }

        return toStockTransferDTO(saved);
    }

    // --- StockOut Methods ---
    @Override
    public List<StockOutDTO> getAllStockOuts() {
        return stockOutRepository.findAll().stream()
                .filter(s -> !Boolean.TRUE.equals(s.getIsDeleted()))
                .map(stockOutMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StockOutDTO getStockOutById(Long id) {
        StockOut s = stockOutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockOut", "id", id));
        return stockOutMapper.toDTO(s);
    }

    @Override
    @Transactional
    public StockOutDTO createStockOut(StockOutDTO dto) {
        StockOut entity = stockOutMapper.toEntity(dto);
        entity.setIsDeleted(false);
        if (entity.getDetails() != null) {
            entity.getDetails().forEach(d -> {
                d.setStockOut(entity);
                d.setIsDeleted(false);
            });
        }
        StockOut saved = stockOutRepository.save(entity);

        // Deduct inventory if status is DA_XUAT or COMPLETED
        if ("DA_XUAT".equalsIgnoreCase(saved.getStatus()) || "COMPLETED".equalsIgnoreCase(saved.getStatus())) {
            applyStockOutDeductions(saved);
        }

        return stockOutMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public StockOutDTO updateStockOut(Long id, StockOutDTO dto) {
        StockOut existing = stockOutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockOut", "id", id));

        String previousStatus = existing.getStatus();
        existing.setStockOutCode(dto.getStockOutCode());
        existing.setOutType(dto.getOutType());
        existing.setWarehouseName(dto.getWarehouseName());
        existing.setCreator(dto.getCreator());
        existing.setStatus(dto.getStatus());
        existing.setNotes(dto.getNotes());
        existing.setTotalVariants(dto.getTotalVariants());
        existing.setTotalItems(dto.getTotalItems());
        existing.setTotalValue(dto.getTotalValue());

        if (dto.getItems() != null) {
            existing.getDetails().clear();
            List<StockOutDetail> newDetails = dto.getItems().stream().map(i -> {
                StockOutDetail d = StockOutDetail.builder()
                        .stockOut(existing)
                        .productName(i.getProductName())
                        .variant(i.getVariant())
                        .sku(i.getSku())
                        .barcode(i.getBarcode())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .amount(i.getAmount())
                        .build();
                d.setIsDeleted(false);
                return d;
            }).collect(Collectors.toList());
            existing.getDetails().addAll(newDetails);
        }

        StockOut saved = stockOutRepository.save(existing);

        // Deduct inventory if transitioning to DA_XUAT or COMPLETED
        if (("DA_XUAT".equalsIgnoreCase(saved.getStatus()) || "COMPLETED".equalsIgnoreCase(saved.getStatus()))
                && !("DA_XUAT".equalsIgnoreCase(previousStatus) || "COMPLETED".equalsIgnoreCase(previousStatus))) {
            applyStockOutDeductions(saved);
        }

        // Restore inventory if transitioning from DA_XUAT or COMPLETED to DA_HUY or CANCELLED
        if (("DA_HUY".equalsIgnoreCase(saved.getStatus()) || "CANCELLED".equalsIgnoreCase(saved.getStatus()))
                && ("DA_XUAT".equalsIgnoreCase(previousStatus) || "COMPLETED".equalsIgnoreCase(previousStatus))) {
            restoreStockOut(existing);
        }

        return stockOutMapper.toDTO(saved);
    }

    private void applyStockOutDeductions(StockOut stockOut) {
        if (stockOut == null || stockOut.getDetails() == null || stockOut.getDetails().isEmpty()) {
            return;
        }

        Branch branch = null;
        if (stockOut.getWarehouseName() != null && !stockOut.getWarehouseName().isBlank()) {
            branch = branchRepository.findByIsDeletedFalse().stream()
                    .filter(b -> stockOut.getWarehouseName().toLowerCase().contains(b.getBranchName().toLowerCase())
                            || b.getBranchName().toLowerCase().contains(stockOut.getWarehouseName().toLowerCase()))
                    .findFirst()
                    .orElse(null);
        }
        if (branch == null) {
            branch = branchRepository.findByIsDeletedFalse().stream().findFirst().orElse(null);
        }
        final Branch effectiveBranch = branch;
        WarehouseZone zone = effectiveBranch != null ? warehouseService.getOrCreateDefaultZone(effectiveBranch) : null;

        for (StockOutDetail detail : stockOut.getDetails()) {
            if (Boolean.TRUE.equals(detail.getIsDeleted())) continue;
            BigDecimal qty = detail.getQuantity() != null ? BigDecimal.valueOf(detail.getQuantity()) : BigDecimal.ONE;
            if (qty.compareTo(BigDecimal.ZERO) <= 0) continue;

            ProductVariant variant = null;
            if (detail.getSku() != null && !detail.getSku().isBlank()) {
                variant = productVariantRepository.findBySkuAndIsDeletedFalse(detail.getSku()).orElse(null);
            }
            if (variant == null && detail.getBarcode() != null && !detail.getBarcode().isBlank()) {
                variant = productVariantRepository.findByBarcodeAndIsDeletedFalse(detail.getBarcode()).orElse(null);
            }

            Product product = variant != null ? variant.getProduct() : null;
            if (product == null && detail.getProductName() != null && !detail.getProductName().isBlank()) {
                product = productRepository.findByIsDeletedFalse().stream()
                        .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(detail.getProductName()))
                        .findFirst()
                        .orElse(null);
            }

            final ProductVariant finalVariant = variant;
            final Product finalProduct = product;

            if (finalProduct != null) {
                Long zoneId = zone != null ? zone.getId() : null;
                Long branchId = effectiveBranch != null ? effectiveBranch.getId() : null;

                try {
                    deductStock(zoneId, branchId, finalProduct.getId(), null, null, qty,
                            "STOCK_OUT_" + (stockOut.getOutType() != null ? stockOut.getOutType() : "EXPORT"),
                            stockOut.getStockOutCode(), stockOut.getId());
                } catch (Exception e) {
                    log.warn("deductStock failed for product {}: {}", finalProduct.getId(), e.getMessage());
                }

                // Decrement ProductLocation
                try {
                    List<ProductLocation> locations = productLocationRepository.findByProductIdAndIsDeletedFalse(finalProduct.getId());
                    BigDecimal remainingToDeduct = qty;
                    for (ProductLocation loc : locations) {
                        if (remainingToDeduct.compareTo(BigDecimal.ZERO) <= 0) break;
                        BigDecimal available = loc.getQuantity();
                        if (available == null || available.compareTo(BigDecimal.ZERO) <= 0) continue;

                        BigDecimal deduct = available.min(remainingToDeduct);
                        loc.setQuantity(available.subtract(deduct));
                        productLocationRepository.save(loc);
                        remainingToDeduct = remainingToDeduct.subtract(deduct);
                    }
                } catch (Exception e) {
                    log.warn("Failed to update ProductLocation for product {}: {}", finalProduct.getId(), e.getMessage());
                }

                // Decrement InventoryBalance and record InventoryTransaction
                if (finalVariant != null && effectiveBranch != null) {
                    try {
                        InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(finalVariant.getId(), effectiveBranch.getId())
                                .orElseGet(() -> InventoryBalance.builder()
                                        .productVariant(finalVariant)
                                        .branch(effectiveBranch)
                                        .availableQuantity(BigDecimal.ZERO)
                                        .reservedQuantity(BigDecimal.ZERO)
                                        .damagedQuantity(BigDecimal.ZERO)
                                        .build());
                        BigDecimal beforeQty = balance.getAvailableQuantity() != null ? balance.getAvailableQuantity() : BigDecimal.ZERO;
                        BigDecimal afterQty = beforeQty.subtract(qty).max(BigDecimal.ZERO);
                        balance.setAvailableQuantity(afterQty);
                        balance.setLastUpdated(LocalDateTime.now());
                        inventoryBalanceRepository.save(balance);

                        String txCode = "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                                + "-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4);

                        InventoryTransaction tx = InventoryTransaction.builder()
                                .transactionCode(txCode)
                                .productVariant(finalVariant)
                                .sourceBranch(effectiveBranch)
                                .transactionType(InventoryTransactionType.ADJUSTMENT)
                                .quantity(qty)
                                .beforeQuantity(beforeQty)
                                .afterQuantity(afterQty)
                                .build();
                        tx.setIsDeleted(false);
                        tx.setCreatedBy(getCurrentUsername());
                        inventoryTransactionRepository.save(tx);
                    } catch (Exception e) {
                        log.warn("Failed to update InventoryBalance: {}", e.getMessage());
                    }
                }
            }
        }
    }

    private void restoreStockOut(StockOut stockOut) {
        if (stockOut == null || stockOut.getDetails() == null || stockOut.getDetails().isEmpty()) {
            return;
        }

        Branch branch = null;
        if (stockOut.getWarehouseName() != null && !stockOut.getWarehouseName().isBlank()) {
            branch = branchRepository.findByIsDeletedFalse().stream()
                    .filter(b -> stockOut.getWarehouseName().toLowerCase().contains(b.getBranchName().toLowerCase())
                            || b.getBranchName().toLowerCase().contains(stockOut.getWarehouseName().toLowerCase()))
                    .findFirst()
                    .orElse(null);
        }
        if (branch == null) {
            branch = branchRepository.findByIsDeletedFalse().stream().findFirst().orElse(null);
        }
        final Branch effectiveBranch = branch;
        WarehouseZone zone = effectiveBranch != null ? warehouseService.getOrCreateDefaultZone(effectiveBranch) : null;
        String username = getCurrentUsername();

        for (StockOutDetail detail : stockOut.getDetails()) {
            if (Boolean.TRUE.equals(detail.getIsDeleted())) continue;
            BigDecimal qty = detail.getQuantity() != null ? BigDecimal.valueOf(detail.getQuantity()) : BigDecimal.ONE;
            if (qty.compareTo(BigDecimal.ZERO) <= 0) continue;

            ProductVariant variant = null;
            if (detail.getSku() != null && !detail.getSku().isBlank()) {
                variant = productVariantRepository.findBySkuAndIsDeletedFalse(detail.getSku()).orElse(null);
            }
            if (variant == null && detail.getBarcode() != null && !detail.getBarcode().isBlank()) {
                variant = productVariantRepository.findByBarcodeAndIsDeletedFalse(detail.getBarcode()).orElse(null);
            }

            Product product = variant != null ? variant.getProduct() : null;
            if (product == null && detail.getProductName() != null && !detail.getProductName().isBlank()) {
                product = productRepository.findByIsDeletedFalse().stream()
                        .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(detail.getProductName()))
                        .findFirst()
                        .orElse(null);
            }

            final ProductVariant finalVariant = variant;
            final Product finalProduct = product;

            if (finalProduct != null) {
                Long zoneId = zone != null ? zone.getId() : null;
                Long branchId = effectiveBranch != null ? effectiveBranch.getId() : null;

                try {
                    addStock(zoneId, branchId, finalProduct.getId(), null, null, qty,
                            "STOCK_OUT_CANCELLED", stockOut.getStockOutCode(), stockOut.getId());
                } catch (Exception e) {
                    log.warn("addStock failed for StockOut restore {}: {}", finalProduct.getId(), e.getMessage());
                }

                // Increment ProductLocation
                try {
                    ProductLocation loc = productLocationRepository.findByProductIdAndBinIdAndIsDeletedFalse(finalProduct.getId(), zoneId)
                            .orElseGet(() -> ProductLocation.builder().product(finalProduct).quantity(BigDecimal.ZERO).build());
                    loc.setQuantity((loc.getQuantity() != null ? loc.getQuantity() : BigDecimal.ZERO).add(qty));
                    loc.setIsDeleted(false);
                    productLocationRepository.save(loc);
                } catch (Exception e) {
                    log.warn("Failed to restore ProductLocation for product {}: {}", finalProduct.getId(), e.getMessage());
                }

                // Increment InventoryBalance
                if (finalVariant != null && effectiveBranch != null) {
                    try {
                        InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(finalVariant.getId(), effectiveBranch.getId())
                                .orElseGet(() -> InventoryBalance.builder()
                                        .productVariant(finalVariant)
                                        .branch(effectiveBranch)
                                        .availableQuantity(BigDecimal.ZERO)
                                        .reservedQuantity(BigDecimal.ZERO)
                                        .damagedQuantity(BigDecimal.ZERO)
                                        .build());
                        BigDecimal beforeQty = balance.getAvailableQuantity() != null ? balance.getAvailableQuantity() : BigDecimal.ZERO;
                        BigDecimal afterQty = beforeQty.add(qty);
                        balance.setAvailableQuantity(afterQty);
                        balance.setLastUpdated(LocalDateTime.now());
                        inventoryBalanceRepository.save(balance);

                        String txCode = "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                                + "-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4);
                        InventoryTransaction tx = InventoryTransaction.builder()
                                .transactionCode(txCode)
                                .productVariant(finalVariant)
                                .sourceBranch(effectiveBranch)
                                .transactionType(InventoryTransactionType.ADJUSTMENT)
                                .quantity(qty)
                                .beforeQuantity(beforeQty)
                                .afterQuantity(afterQty)
                                .build();
                        tx.setIsDeleted(false);
                        tx.setCreatedBy(username);
                        inventoryTransactionRepository.save(tx);
                    } catch (Exception e) {
                        log.warn("Failed to update InventoryBalance: {}", e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    @Transactional
    public void deleteStockOut(Long id) {
        StockOut existing = stockOutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockOut", "id", id));
        existing.setIsDeleted(true);
        stockOutRepository.save(existing);
    }
}
