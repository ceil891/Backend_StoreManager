package org.example.storemanager.service.inventory.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.catalog.inventory.InventoryAdjustRequest;
import org.example.storemanager.dto.request.catalog.inventory.SearchInventoryRequest;
import org.example.storemanager.dto.response.catalog.inventory.AdjustmentResponse;
import org.example.storemanager.dto.response.catalog.inventory.InventoryResponse;
import org.example.storemanager.dto.response.catalog.inventory.LowStockResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.entity.catalog.Color;
import org.example.storemanager.entity.catalog.Product;
import org.example.storemanager.entity.catalog.Size;
import org.example.storemanager.entity.inventory.SizeInventory;
import org.example.storemanager.entity.inventory.StockLedger;
import org.example.storemanager.entity.wms.WarehouseZone;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.catalog.ColorRepository;
import org.example.storemanager.repository.catalog.ProductRepository;
import org.example.storemanager.repository.catalog.SizeRepository;
import org.example.storemanager.repository.inventory.SizeInventoryRepository;
import org.example.storemanager.repository.inventory.StockLedgerRepository;
import org.example.storemanager.repository.wms.WarehouseZoneRepository;
import org.example.storemanager.service.inventory.InventoryService;
import org.example.storemanager.service.wms.WarehouseZoneService;
import org.example.storemanager.config.LogActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final SizeInventoryRepository sizeInventoryRepository;
    private final StockLedgerRepository stockLedgerRepository;
    private final ProductRepository productRepository;
    private final WarehouseZoneRepository warehouseZoneRepository;
    private final WarehouseZoneService warehouseZoneService;
    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryResponse> searchInventories(SearchInventoryRequest request, Pageable pageable) {
        Page<SizeInventory> page = sizeInventoryRepository.searchInventory(
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
    @Transactional(readOnly = true)
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
                .findByWarehouseZoneIdAndProductIdAndSizeIdAndColorId(zone.getId(), product.getId(), sizeId, colorId)
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
    @Transactional(readOnly = true)
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
                .findByWarehouseZoneIdAndProductIdAndSizeIdAndColorId(
                        zone.getId(), product.getId(), resolvedSizeId, resolvedColorId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        buildInsufficientStockMessage(product.getName(), zone.getZoneName(), quantity, null)));

        BigDecimal oldQty = inventory.getQuantityPhysical();
        BigDecimal newQty = oldQty.subtract(quantity);
        if (newQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    buildInsufficientStockMessage(product.getName(), zone.getZoneName(), quantity, oldQty));
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
            return warehouseZoneService.getOrCreateDefaultZone(request.getBranchId());
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
}
