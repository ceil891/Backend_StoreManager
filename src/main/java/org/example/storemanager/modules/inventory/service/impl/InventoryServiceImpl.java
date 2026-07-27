package org.example.storemanager.modules.inventory.service.impl;

import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
        return sizeInventoryRepository.findAllWithAssociations().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockLedgerResponse> getStockLedger() {
        return stockLedgerRepository.findAllWithProductAndBranch().stream()
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

        ImportReceipt receipt = ImportReceipt.builder()
                .receiptCode(dto.getReceiptCode() != null ? dto.getReceiptCode() : "GRN-" + System.currentTimeMillis())
                .receiptDate(dto.getReceiptDate() != null ? dto.getReceiptDate() : LocalDateTime.now())
                .totalAmount(dto.getTotalAmount() != null ? dto.getTotalAmount() : BigDecimal.ZERO)
                .discount(dto.getDiscount() != null ? dto.getDiscount() : BigDecimal.ZERO)
                .tax(dto.getTax() != null ? dto.getTax() : BigDecimal.ZERO)
                .status(dto.getStatus() != null ? dto.getStatus() : "COMPLETE")
                .branch(branch)
                .supplier(supplier)
                .inspectedBy(dto.getInspectedBy())
                .note(dto.getNote())
                .build();
        receipt.setIsDeleted(false);
        ImportReceipt saved = importReceiptRepository.save(receipt);

        List<ImportReceiptDetailDTO> savedLines = new ArrayList<>();
        if (dto.getReceiptLines() != null && !dto.getReceiptLines().isEmpty()) {
            for (ImportReceiptDetailDTO line : dto.getReceiptLines()) {
                ProductVariant variant = null;
                if (line.getProductVariantId() != null) {
                    // 1. Tìm theo Variant ID
                    variant = productVariantRepository.findById(line.getProductVariantId()).orElse(null);
                    // 2. Nếu không tìm thấy, tìm Variant thuộc Product ID này
                    if (variant == null) {
                        variant = productVariantRepository.findByProductIdAndIsDeletedFalse(line.getProductVariantId())
                                .stream().findFirst().orElse(null);
                    }
                    // 3. Nếu Product tồn tại nhưng chưa có Variant nào, tạo mới Variant đúng cho Product này
                    if (variant == null) {
                        Product prod = productRepository.findByIdAndIsDeletedFalse(line.getProductVariantId()).orElse(null);
                        if (prod != null) {
                            ProductVariant newVar = ProductVariant.builder()
                                    .product(prod)
                                    .variantCode("VAR-" + prod.getProductCode() + "-" + System.currentTimeMillis() % 10000)
                                    .sku(prod.getProductCode() + "-DEFAULT")
                                    .price(prod.getBasePrice() != null ? prod.getBasePrice() : BigDecimal.ZERO)
                                    .build();
                            newVar.setIsDeleted(false);
                            variant = productVariantRepository.save(newVar);
                        }
                    }
                }
                // 4. Chỉ fallback khi hoàn toàn không có ID
                if (variant == null) {
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

        if ("COMPLETE".equalsIgnoreCase(saved.getStatus()) || "PASSED".equalsIgnoreCase(saved.getStatus()) || "APPROVED".equalsIgnoreCase(saved.getStatus())) {
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
        }
        if (dto.getSupplierId() != null) {
            Supplier s = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", dto.getSupplierId()));
            r.setSupplier(s);
        }
        r.setReceiptCode(dto.getReceiptCode());
        r.setTotalAmount(dto.getTotalAmount());
        r.setDiscount(dto.getDiscount());
        r.setTax(dto.getTax());
        ImportReceipt saved = importReceiptRepository.save(r);
        return toImportReceiptDTO(saved);
    }

    @Override
    @Transactional
    public void deleteImportReceipt(Long id) {
        ImportReceipt r = importReceiptRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ImportReceipt", "id", id));
        r.setIsDeleted(true);
        importReceiptRepository.save(r);
    }

    @Override
    @Transactional
    public ImportReceiptDTO completeImportReceipt(Long id) {
        ImportReceipt r = importReceiptRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ImportReceipt", "id", id));
        if ("COMPLETE".equals(r.getStatus())) {
            return toImportReceiptDTO(r);
        }

        r.setStatus("COMPLETE");
        ImportReceipt saved = importReceiptRepository.save(r);

        List<ImportReceiptDetail> details = importReceiptDetailRepository.findByReceiptIdAndIsDeletedFalse(id);
        WarehouseZone defaultZone = warehouseService.getOrCreateDefaultZone(r.getBranch());
        String username = getCurrentUsername();

        for (ImportReceiptDetail detail : details) {
            ProductVariant variant = detail.getProductVariant();
            Product product = variant.getProduct();

            // 1. Physical stock addition (SizeInventory & StockLedger)
            addStock(defaultZone.getId(), r.getBranch().getId(), product.getId(),
                    null, null, detail.getQuantity(),
                    "IMPORT", r.getReceiptCode(), r.getId());

            // 2. Set Bin status and update/create ProductLocation
            if (detail.getTargetBin() != null) {
                WarehouseBin bin = detail.getTargetBin();
                bin.setStatus("OCCUPIED");
                warehouseBinRepository.save(bin);

                ProductLocation loc = productLocationRepository.findByProductIdAndBinIdAndIsDeletedFalse(product.getId(), bin.getId())
                        .orElseGet(() -> ProductLocation.builder()
                                .product(product)
                                .bin(bin)
                                .quantity(BigDecimal.ZERO)
                                .build());
                loc.setQuantity(loc.getQuantity().add(detail.getQuantity()));
                loc.setIsDeleted(false);
                productLocationRepository.save(loc);
            }

            // 3. Update InventoryBalance
            InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(variant.getId(), r.getBranch().getId())
                    .orElseGet(() -> InventoryBalance.builder()
                            .productVariant(variant)
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
                    + "-" + String.format("%06d", inventoryTransactionRepository.count() + 1);

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
        if (!"PENDING_APPROVAL".equals(r.getStatus())) {
            throw new org.example.storemanager.shared.exception.BusinessException(
                org.example.storemanager.shared.enums.ErrorCode.INVALID_STATUS_TRANSITION,
                "Chỉ có thể phê duyệt phiếu ở trạng thái PENDING_APPROVAL");
        }
        r.setStatus("APPROVED");
        r.setUpdatedBy(getCurrentUsername());
        return toImportReceiptDTO(importReceiptRepository.save(r));
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
                .map(this::toReturnToSupplierDTO)
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

    @Override
    @Transactional
    public ReturnToSupplierDTO createReturnToSupplier(ReturnToSupplierDTO dto) {
        Branch branch = branchRepository.findByIdAndIsDeletedFalse(dto.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", dto.getSupplierId()));

        ReturnToSupplier r = ReturnToSupplier.builder()
                .returnCode(dto.getReturnCode())
                .returnDate(dto.getReturnDate() != null ? dto.getReturnDate() : LocalDateTime.now())
                .totalAmount(dto.getTotalAmount())
                .status(org.example.storemanager.shared.enums.inventory.ReturnToSupplierStatus.PENDING_SUPPLIER_APPROVAL)
                .reason(dto.getReason())
                .branch(branch)
                .supplier(supplier)
                .grnRefNumber(dto.getGrnRefNumber())
                .build();
        r.setIsDeleted(false);
        ReturnToSupplier saved = returnToSupplierRepository.save(r);

        List<ReturnToSupplierDetailDTO> savedLines = new ArrayList<>();
        if (dto.getReturnLines() != null) {
            for (ReturnToSupplierDetailDTO line : dto.getReturnLines()) {
                ProductVariant variant = productVariantRepository.findById(line.getProductVariantId())
                        .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", line.getProductVariantId()));

                ReturnToSupplierDetail detail = ReturnToSupplierDetail.builder()
                        .returnReceipt(saved)
                        .product(variant.getProduct())
                        .quantity(line.getQuantity())
                        .unitPrice(line.getUnitCost())
                        .subTotal(line.getSubTotal())
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
        if (r.getStatus() == org.example.storemanager.shared.enums.inventory.ReturnToSupplierStatus.APPROVED_CREDIT_NOTE) {
            return toReturnToSupplierDTO(r);
        }

        r.setStatus(org.example.storemanager.shared.enums.inventory.ReturnToSupplierStatus.APPROVED_CREDIT_NOTE);
        if (request != null && request.getApprovalNotes() != null) {
            r.setReason(request.getApprovalNotes());
        }
        ReturnToSupplier saved = returnToSupplierRepository.save(r);

        List<ReturnToSupplierDetail> details = returnToSupplierDetailRepository.findByReturnReceiptIdAndIsDeletedFalse(id);
        WarehouseZone defaultZone = warehouseService.getOrCreateDefaultZone(r.getBranch());

        for (ReturnToSupplierDetail detail : details) {
            deductStock(defaultZone.getId(), r.getBranch().getId(), detail.getProduct().getId(),
                    null, null, detail.getQuantity(),
                    "RETURN", r.getReturnCode(), r.getId());
        }

        return toReturnToSupplierDTO(saved);
    }

    @Override
    @Transactional
    public ReturnToSupplierDTO rejectReturnToSupplier(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.ReturnRejectRequest request) {
        ReturnToSupplier r = returnToSupplierRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnToSupplier", "id", id));
        r.setStatus(org.example.storemanager.shared.enums.inventory.ReturnToSupplierStatus.REJECTED);
        if (request != null && request.getRejectNotes() != null) {
            r.setReason(request.getRejectNotes());
        }
        ReturnToSupplier saved = returnToSupplierRepository.save(r);
        return toReturnToSupplierDTO(saved);
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

    @Override
    @Transactional(readOnly = true)
    public StockTransferDTO getStockTransferById(Long id) {
        StockTransfer t = stockTransferRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", "id", id));
        StockTransferDTO dto = toStockTransferDTO(t);
        dto.setTransferLines(stockTransferDetailRepository.findByTransferIdAndIsDeletedFalse(id).stream()
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

        StockTransfer t = StockTransfer.builder()
                .transferCode(dto.getTransferCode())
                .transferDate(dto.getTransferDate() != null ? dto.getTransferDate() : LocalDateTime.now())
                .status("PENDING_APPROVAL")
                .fromBranch(fromB)
                .toBranch(toB)
                .logisticsPartner(dto.getLogisticsPartner())
                .trackingRef(dto.getTrackingRef())
                .requestedBy(dto.getRequestedBy())
                .estArrivalDate(dto.getEstArrivalDate())
                .build();
        t.setIsDeleted(false);
        StockTransfer saved = stockTransferRepository.save(t);

        List<StockTransferDetailDTO> savedLines = new ArrayList<>();
        if (dto.getTransferLines() != null) {
            for (StockTransferDetailDTO line : dto.getTransferLines()) {
                ProductVariant variant = productVariantRepository.findById(line.getProductVariantId())
                        .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", line.getProductVariantId()));

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
        StockTransfer t = stockTransferRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", "id", id));

        t.setStatus(dto.getStatus());
        t.setApprovedBy(dto.getApprovedBy());
        StockTransfer saved = stockTransferRepository.save(t);
        return toStockTransferDTO(saved);
    }

    @Override
    @Transactional
    public void deleteStockTransfer(Long id) {
        StockTransfer t = stockTransferRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", "id", id));
        t.setIsDeleted(true);
        stockTransferRepository.save(t);
    }

    // --- PRODUCT BATCH ---

    @Override
    @Transactional(readOnly = true)
    public List<ProductBatchDTO> getAllProductBatches() {
        return productBatchRepository.findAllWithAssociations().stream()
                .map(this::toProductBatchDTO)
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
        b.setBatchNumber(dto.getBatchNumber());
        b.setManufactureDate(dto.getManufactureDate());
        b.setExpiryDate(dto.getExpiryDate());
        b.setQualityStatus(dto.getQualityStatus());
        b.setInspector(dto.getInspector());
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
        if (org.example.storemanager.shared.enums.inventory.CancelIssueStatus.APPROVED.equals(c.getStatus())) {
            return toCancelIssueDTO(c);
        }
        c.setStatus(org.example.storemanager.shared.enums.inventory.CancelIssueStatus.APPROVED);
        if (request != null && request.getApprovalNotes() != null) {
            c.setReason(c.getReason() != null ? c.getReason() + " | " + request.getApprovalNotes() : request.getApprovalNotes());
        }
        CancelIssue saved = cancelIssueRepository.save(c);

        List<CancelIssueDetail> details = cancelIssueDetailRepository.findByCancelIssueIdAndIsDeletedFalse(id);
        WarehouseZone defaultZone = warehouseService.getOrCreateDefaultZone(c.getBranch());

        for (CancelIssueDetail detail : details) {
            deductStock(defaultZone.getId(), c.getBranch().getId(), detail.getProduct().getId(),
                    null, null, detail.getQuantity(),
                    "CANCEL_ISSUE", c.getCancelCode(), c.getId());
        }
        return toCancelIssueDTO(saved);
    }

    @Override
    @Transactional
    public CancelIssueDTO rejectCancelIssue(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.CancelIssueRejectRequest request) {
        CancelIssue c = cancelIssueRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("CancelIssue", "id", id));
        c.setStatus(org.example.storemanager.shared.enums.inventory.CancelIssueStatus.REJECTED);
        if (request != null && request.getRejectNotes() != null) {
            c.setReason(c.getReason() != null ? c.getReason() + " | " + request.getRejectNotes() : request.getRejectNotes());
        }
        CancelIssue saved = cancelIssueRepository.save(c);
        return toCancelIssueDTO(saved);
    }

    @Override
    @Transactional
    public StockTransferDTO completeStockTransfer(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.TransferCompleteRequest request) {
        StockTransfer t = stockTransferRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", "id", id));
        if (TransferStatus.RECEIVED.name().equals(t.getStatus())) {
            return toStockTransferDTO(t);
        }
        t.setStatus(TransferStatus.RECEIVED.name());
        StockTransfer saved = stockTransferRepository.save(t);

        List<StockTransferDetail> details = stockTransferDetailRepository.findByTransferIdAndIsDeletedFalse(id);
        WarehouseZone toZone = warehouseService.getOrCreateDefaultZone(t.getToBranch());
        String username = getCurrentUsername();

        for (StockTransferDetail detail : details) {
            Product product = detail.getProduct();
            ProductVariant variant = productVariantRepository.findByProductIdAndIsDeletedFalse(product.getId()).stream().findFirst().orElse(null);
            BigDecimal quantity = detail.getQuantityShipped();

            // Target physical size inventory add
            addStock(toZone.getId(), t.getToBranch().getId(), product.getId(),
                    null, null, quantity,
                    "TRANSFER_IN", t.getTransferCode(), t.getId());

            // Target ProductLocation add
            ProductLocation loc = productLocationRepository.findByProductIdAndBinIdAndIsDeletedFalse(product.getId(), toZone.getId())
                    .orElseGet(() -> ProductLocation.builder()
                            .product(product)
                            .quantity(BigDecimal.ZERO)
                            .build());
            loc.setQuantity(loc.getQuantity().add(quantity));
            loc.setIsDeleted(false);
            productLocationRepository.save(loc);

            if (variant != null) {
                // Increase InventoryBalance at destination branch
                InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(variant.getId(), t.getToBranch().getId())
                        .orElseGet(() -> InventoryBalance.builder()
                                .productVariant(variant)
                                .branch(t.getToBranch())
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
                        + "-" + String.format("%06d", inventoryTransactionRepository.count() + 1);

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
        return toStockTransferDTO(saved);
    }

    @Override
    @Transactional
    public StockTransferDTO cancelStockTransfer(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.TransferCancelRequest request) {
        StockTransfer t = stockTransferRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", "id", id));
        if ("COMPLETED".equals(t.getStatus())) {
            throw new org.example.storemanager.shared.exception.inventory.InvalidStatusTransitionException(t.getStatus(), "CANCELLED");
        }
        t.setStatus("CANCELLED");
        StockTransfer saved = stockTransferRepository.save(t);
        return toStockTransferDTO(saved);
    }

    @Override
    @Transactional
    public ProductBatchDTO adjustProductBatch(Long id, org.example.storemanager.modules.catalog.dto.request.inventory.BatchAdjustRequest request) {
        ProductBatch b = productBatchRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBatch", "id", id));
        b.setRemainingUnits(request.getAdjustedQuantity());
        ProductBatch saved = productBatchRepository.save(b);
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

        List<InventoryCheckDetail> details = inventoryCheckDetailRepository.findByCheckIdAndIsDeletedFalse(id);
        
        for (InventoryCheckDetail detail : details) {
            BigDecimal diff = detail.getDiffQty();
            if (diff != null && diff.compareTo(BigDecimal.ZERO) > 0) {
                // actual > system => add stock
                addStock(c.getWarehouseZone().getId(), c.getBranch().getId(), detail.getProduct().getId(),
                        null, null, diff, "CHECK_ADJUST", c.getCheckCode(), c.getId());
            } else if (diff != null && diff.compareTo(BigDecimal.ZERO) < 0) {
                // actual < system => deduct stock
                deductStock(c.getWarehouseZone().getId(), c.getBranch().getId(), detail.getProduct().getId(),
                        null, null, diff.abs(), "CHECK_ADJUST", c.getCheckCode(), c.getId());
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
                .build();
    }

    private StockTransferDetailDTO toStockTransferDetailDTO(StockTransferDetail d) {
        return StockTransferDetailDTO.builder()
                .id(d.getId())
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

        r.setStatus(ReturnToSupplierStatus.COMPLETE);
        ReturnToSupplier saved = returnToSupplierRepository.save(r);

        List<ReturnToSupplierDetail> details = returnToSupplierDetailRepository.findByReturnReceiptIdAndIsDeletedFalse(id);
        String username = getCurrentUsername();
        Branch branch = r.getBranch();

        for (ReturnToSupplierDetail detail : details) {
            Product product = detail.getProduct();
            ProductVariant variant = productVariantRepository.findByProductIdAndIsDeletedFalse(product.getId()).stream().findFirst().orElse(null);
            if (variant == null) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Không tìm thấy biến thể cho sản phẩm: " + product.getName());
            }
            BigDecimal quantity = detail.getQuantity();

            List<ProductLocation> locations = productLocationRepository.findByProductIdAndIsDeletedFalse(product.getId());
            BigDecimal remainingToDeduct = quantity;
            for (ProductLocation loc : locations) {
                if (remainingToDeduct.compareTo(BigDecimal.ZERO) <= 0) break;
                BigDecimal available = loc.getQuantity();
                if (available.compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal deduct = available.min(remainingToDeduct);
                loc.setQuantity(available.subtract(deduct));
                productLocationRepository.save(loc);
                remainingToDeduct = remainingToDeduct.subtract(deduct);
            }

            InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(variant.getId(), branch.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "Không tìm thấy số dư tồn kho để trả hàng"));
            
            BigDecimal beforeQty = balance.getAvailableQuantity() != null ? balance.getAvailableQuantity() : BigDecimal.ZERO;
            BigDecimal afterQty = beforeQty.subtract(quantity);
            balance.setAvailableQuantity(afterQty);
            balance.setLastUpdated(LocalDateTime.now());
            inventoryBalanceRepository.save(balance);

            String txCode = "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + "-" + String.format("%06d", inventoryTransactionRepository.count() + 1);

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
        }

        return toReturnToSupplierDTO(saved);
    }

    @Override
    @Transactional
    public ReturnToSupplierDTO cancelReturnToSupplier(Long id) {
        ReturnToSupplier r = returnToSupplierRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnToSupplier", "id", id));
        r.setStatus(ReturnToSupplierStatus.CANCELLED);
        r.setUpdatedBy(getCurrentUsername());
        return toReturnToSupplierDTO(returnToSupplierRepository.save(r));
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

        r.setStatus(CancelIssueStatus.COMPLETE);
        CancelIssue saved = cancelIssueRepository.save(r);

        List<CancelIssueDetail> details = cancelIssueDetailRepository.findByCancelIssueIdAndIsDeletedFalse(id);
        String username = getCurrentUsername();
        Branch branch = r.getBranch();

        for (CancelIssueDetail detail : details) {
            Product product = detail.getProduct();
            ProductVariant variant = productVariantRepository.findByProductIdAndIsDeletedFalse(product.getId()).stream().findFirst().orElse(null);
            if (variant == null) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Không tìm thấy biến thể cho sản phẩm: " + product.getName());
            }
            BigDecimal quantity = detail.getQuantity();

            List<ProductLocation> locations = productLocationRepository.findByProductIdAndIsDeletedFalse(product.getId());
            BigDecimal remainingToDeduct = quantity;
            for (ProductLocation loc : locations) {
                if (remainingToDeduct.compareTo(BigDecimal.ZERO) <= 0) break;
                BigDecimal available = loc.getQuantity();
                if (available.compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal deduct = available.min(remainingToDeduct);
                loc.setQuantity(available.subtract(deduct));
                productLocationRepository.save(loc);
                remainingToDeduct = remainingToDeduct.subtract(deduct);
            }

            InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(variant.getId(), branch.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "Không tìm thấy số dư tồn kho để xuất hủy"));

            BigDecimal beforeQty = balance.getAvailableQuantity() != null ? balance.getAvailableQuantity() : BigDecimal.ZERO;
            BigDecimal afterQty = beforeQty.subtract(quantity);
            balance.setAvailableQuantity(afterQty);
            balance.setLastUpdated(LocalDateTime.now());
            inventoryBalanceRepository.save(balance);

            String txCode = "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + "-" + String.format("%06d", inventoryTransactionRepository.count() + 1);

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
        }

        return toCancelIssueDTO(saved);
    }

    @Override
    @Transactional
    public CancelIssueDTO cancelCancelIssue(Long id) {
        CancelIssue r = cancelIssueRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("CancelIssue", "id", id));
        r.setStatus(CancelIssueStatus.CANCELLED);
        r.setUpdatedBy(getCurrentUsername());
        return toCancelIssueDTO(cancelIssueRepository.save(r));
    }

    // --- Stock Transfer Actions ---
    @Override
    @Transactional
    public StockTransferDTO submitStockTransfer(Long id) {
        StockTransfer t = stockTransferRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", "id", id));
        t.setStatus(TransferStatus.PENDING_APPROVAL.name());
        t.setUpdatedBy(getCurrentUsername());
        return toStockTransferDTO(stockTransferRepository.save(t));
    }

    @Override
    @Transactional
    public StockTransferDTO approveStockTransfer(Long id) {
        StockTransfer t = stockTransferRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", "id", id));
        t.setStatus(TransferStatus.APPROVED.name());
        t.setUpdatedBy(getCurrentUsername());
        return toStockTransferDTO(stockTransferRepository.save(t));
    }

    @Override
    @Transactional
    public StockTransferDTO shipStockTransfer(Long id) {
        StockTransfer t = stockTransferRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", "id", id));
        t.setStatus(TransferStatus.SHIPPED.name());
        StockTransfer saved = stockTransferRepository.save(t);

        List<StockTransferDetail> details = stockTransferDetailRepository.findByTransferIdAndIsDeletedFalse(id);
        WarehouseZone fromZone = warehouseService.getOrCreateDefaultZone(t.getFromBranch());
        String username = getCurrentUsername();

        for (StockTransferDetail detail : details) {
            Product product = detail.getProduct();
            ProductVariant variant = productVariantRepository.findByProductIdAndIsDeletedFalse(product.getId()).stream().findFirst().orElse(null);
            BigDecimal quantity = detail.getQuantityShipped();

            deductStock(fromZone.getId(), t.getFromBranch().getId(), product.getId(),
                    null, null, quantity,
                    "TRANSFER_OUT", t.getTransferCode(), t.getId());

            List<ProductLocation> locations = productLocationRepository.findByProductIdAndIsDeletedFalse(product.getId());
            BigDecimal remainingToDeduct = quantity;
            for (ProductLocation loc : locations) {
                if (remainingToDeduct.compareTo(BigDecimal.ZERO) <= 0) break;
                BigDecimal available = loc.getQuantity();
                if (available.compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal deduct = available.min(remainingToDeduct);
                loc.setQuantity(available.subtract(deduct));
                productLocationRepository.save(loc);
                remainingToDeduct = remainingToDeduct.subtract(deduct);
            }

            if (variant != null) {
                InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(variant.getId(), t.getFromBranch().getId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "Không tìm thấy số dư tồn kho tại kho chuyển"));
                BigDecimal beforeQty = balance.getAvailableQuantity() != null ? balance.getAvailableQuantity() : BigDecimal.ZERO;
                BigDecimal afterQty = beforeQty.subtract(quantity);
                balance.setAvailableQuantity(afterQty);
                balance.setLastUpdated(LocalDateTime.now());
                inventoryBalanceRepository.save(balance);

                String txCode = "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                        + "-" + String.format("%06d", inventoryTransactionRepository.count() + 1);

                InventoryTransaction tx = InventoryTransaction.builder()
                        .transactionCode(txCode)
                        .productVariant(variant)
                        .sourceBranch(t.getFromBranch())
                        .transactionType(InventoryTransactionType.TRANSFER_OUT)
                        .quantity(quantity)
                        .beforeQuantity(beforeQty)
                        .afterQuantity(afterQty)
                        .build();
                tx.setIsDeleted(false);
                tx.setCreatedBy(username);
                inventoryTransactionRepository.save(tx);
            }
        }

        return toStockTransferDTO(saved);
    }
}
