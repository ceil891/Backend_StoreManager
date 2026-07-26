package org.example.storemanager.service.catalog.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.config.LogActivity;
import org.example.storemanager.dto.request.catalog.variant.CreateSingleVariantRequest;
import org.example.storemanager.dto.request.catalog.variant.CreateVariantRequest;
import org.example.storemanager.dto.request.catalog.variant.UpdateVariantRequest;
import org.example.storemanager.dto.response.catalog.variant.CreateVariantResponse;
import org.example.storemanager.dto.response.catalog.variant.VariantResponse;
import org.example.storemanager.entity.catalog.*;
import org.example.storemanager.enums.catalog.VariantStatus;
import org.example.storemanager.enums.catalog.VariantStrategy;
import org.example.storemanager.exception.DuplicateResourceException;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.catalog.*;
import org.example.storemanager.repository.inventory.InventoryBalanceRepository;
import org.example.storemanager.repository.system.BranchRepository;
import org.example.storemanager.entity.inventory.InventoryBalance;
import org.example.storemanager.entity.system.Branch;
import java.math.BigDecimal;
import org.example.storemanager.service.catalog.ProductVariantService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final VariantAttributeValueRepository variantAttributeValueRepository;
    private final BranchRepository branchRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;

    // ──────────────────────────────────────────────────────────────────────────
    // TẠO BIẾN THỂ
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    @LogActivity(actionType = "CREATE", entityName = "ProductVariant", entityClass = ProductVariant.class)
    public List<CreateVariantResponse> createVariants(CreateVariantRequest request) {
        Product product = productRepository.findByIdAndIsDeletedFalse(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        // Xác định chiến lược: ưu tiên request, fallback sang Product
        VariantStrategy strategy = request.getVariantStrategy() != null
                ? request.getVariantStrategy()
                : product.getVariantStrategy();

        String username = getCurrentUsername();
        List<CreateVariantResponse> responses = new ArrayList<>();

        if (strategy == VariantStrategy.NONE) {
            // ── SP đơn giản: sinh 1 variant mặc định ──────────────────────────
            String sku = buildDefaultSku(product.getProductCode());
            if (productVariantRepository.existsBySkuAndIsDeletedFalse(sku)) {
                throw new DuplicateResourceException("ProductVariant", "sku", sku);
            }
            String variantCode = generateVariantCode();

            ProductVariant variant = ProductVariant.builder()
                    .product(product)
                    .variantCode(variantCode)
                    .sku(sku)
                    .status(VariantStatus.ACTIVE)
                    .build();
            variant.setIsActive(true);
            variant.setIsDeleted(false);
            variant.setCreatedBy(username);

            ProductVariant saved = productVariantRepository.save(variant);

            // Initialize inventory balance to 0 for all active branches
            List<Branch> activeBranches = branchRepository.findAllBranchesList(null, true);
            initializeInventoryBalanceBatch(List.of(saved), activeBranches, username);

            // Cập nhật strategy trên Product nếu cần
            if (product.getVariantStrategy() != VariantStrategy.NONE) {
                product.setVariantStrategy(VariantStrategy.NONE);
                productRepository.save(product);
            }

            responses.add(mapToCreateResponse(saved, product, ""));

        } else {
            // ── SP có biến thể: sinh từng tổ hợp thuộc tính ──────────────────
            if (request.getAttributeCombinations() == null || request.getAttributeCombinations().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Phải cung cấp danh sách attributeCombinations khi variantStrategy = ATTRIBUTE_BASED");
            }

            // Cập nhật strategy trên Product
            if (product.getVariantStrategy() != VariantStrategy.ATTRIBUTE_BASED) {
                product.setVariantStrategy(VariantStrategy.ATTRIBUTE_BASED);
                productRepository.save(product);
            }

            List<Branch> activeBranches = branchRepository.findAllBranchesList(null, true);
            List<ProductVariant> savedVariants = new ArrayList<>();
            List<VariantAttributeValue> allVavs = new ArrayList<>();

            for (CreateVariantRequest.VariantAttributeInput combo : request.getAttributeCombinations()) {
                // Load tất cả AttributeValue của tổ hợp này
                List<AttributeValue> attrValues = loadAttributeValues(combo.getAttributeValueIds());

                // Sinh SKU
                String sku = buildSku(request.getSkuPrefix(), product.getProductCode(), attrValues, combo.getCustomSku());
                if (productVariantRepository.existsBySkuAndIsDeletedFalse(sku)) {
                    throw new DuplicateResourceException("ProductVariant", "sku", sku);
                }

                // Kiểm tra barcode trùng
                if (combo.getBarcode() != null && !combo.getBarcode().isBlank()
                        && productVariantRepository.existsByBarcodeAndIsDeletedFalse(combo.getBarcode())) {
                    throw new DuplicateResourceException("ProductVariant", "barcode", combo.getBarcode());
                }

                String variantCode = generateVariantCode(); // TODO: Tối ưu sinh mã hàng loạt

                ProductVariant variant = ProductVariant.builder()
                        .product(product)
                        .variantCode(variantCode)
                        .sku(sku)
                        .barcode(combo.getBarcode())
                        .imageUrl(combo.getImageUrl())
                        .price(combo.getPrice())
                        .status(VariantStatus.ACTIVE)
                        .build();
                variant.setIsActive(true);
                variant.setIsDeleted(false);
                variant.setCreatedBy(username);

                savedVariants.add(variant);

                // Lưu tạm Vav để insert sau khi có variant ID (nếu cần batch)
                // Tuy nhiên do chưa có ID nên phải lưu variant trước, hoặc dùng saveAll cho variant
            }
            
            savedVariants = productVariantRepository.saveAll(savedVariants);

            // Ghi VariantAttributeValue và map response
            int i = 0;
            for (CreateVariantRequest.VariantAttributeInput combo : request.getAttributeCombinations()) {
                List<AttributeValue> attrValues = loadAttributeValues(combo.getAttributeValueIds());
                ProductVariant saved = savedVariants.get(i++);
                
                for (AttributeValue av : attrValues) {
                    VariantAttributeValue vav = VariantAttributeValue.builder()
                            .productVariant(saved)
                            .productAttribute(av.getProductAttribute())
                            .attributeValue(av)
                            .build();
                    vav.setIsDeleted(false);
                    vav.setCreatedBy(username);
                    allVavs.add(vav);
                }
                String desc = buildDescription(attrValues);
                responses.add(mapToCreateResponse(saved, product, desc));
            }
            variantAttributeValueRepository.saveAll(allVavs);
            initializeInventoryBalanceBatch(savedVariants, activeBranches, username);
        }

        return responses;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CẬP NHẬT
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "ProductVariant", entityClass = ProductVariant.class)
    public VariantResponse updateVariant(Long id, UpdateVariantRequest request) {
        ProductVariant variant = productVariantRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", id));

        // Kiểm tra barcode trùng (nếu thay đổi)
        if (request.getBarcode() != null && !request.getBarcode().isBlank()) {
            if (productVariantRepository.existsByBarcodeAndIdNotAndIsDeletedFalse(request.getBarcode(), id)) {
                throw new DuplicateResourceException("ProductVariant", "barcode", request.getBarcode());
            }
            variant.setBarcode(request.getBarcode());
        }

        if (request.getImageUrl() != null) {
            variant.setImageUrl(request.getImageUrl());
        }
        if (request.getPrice() != null) {
            variant.setPrice(request.getPrice());
        }

        variant.setUpdatedBy(getCurrentUsername());
        ProductVariant saved = productVariantRepository.save(variant);

        return buildVariantResponse(saved);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // XÓA MỀM
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    @LogActivity(actionType = "DELETE", entityName = "ProductVariant", entityClass = ProductVariant.class)
    public void deleteVariant(Long id) {
        ProductVariant variant = productVariantRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", id));

        if (Boolean.TRUE.equals(variant.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Không thể xóa biến thể đang hoạt động. Vui lòng tắt hoạt động trước.");
        }

        String username = getCurrentUsername();
        variant.setIsDeleted(true);
        variant.setIsActive(false);
        variant.setDeletedAt(LocalDateTime.now());
        variant.setDeletedBy(username);
        variant.setUpdatedBy(username);
        productVariantRepository.save(variant);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // TOGGLE STATUS
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "ProductVariant", entityClass = ProductVariant.class)
    public VariantResponse toggleStatus(Long id, Boolean isActive) {
        ProductVariant variant = productVariantRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", id));
        variant.setIsActive(isActive);
        variant.setUpdatedBy(getCurrentUsername());
        return buildVariantResponse(productVariantRepository.save(variant));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // XEM CHI TIẾT
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public VariantResponse getById(Long id) {
        ProductVariant variant = productVariantRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", id));
        return buildVariantResponse(variant);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DANH SÁCH THEO SẢN PHẨM
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<VariantResponse> getByProductId(Long productId) {
        productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return productVariantRepository.findByProductIdAndIsDeletedFalse(productId)
                .stream()
                .map(this::buildVariantResponse)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HÀM TIỆN ÍCH NỘI BỘ
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Sinh variantCode duy nhất theo định dạng PV000001.
     * Đơn giản hoá: dùng max(id)+1. Production nên dùng sequence DB.
     */
    private String generateVariantCode() {
        long count = productVariantRepository.count() + 1;
        return String.format("PV%06d", count);
    }

    /** SKU mặc định cho SP không có biến thể: <productCode>-DEFAULT */
    private String buildDefaultSku(String productCode) {
        return productCode + "-DEFAULT";
    }

    /**
     * Sinh SKU từ prefix + giá trị thuộc tính.
     * Ví dụ: prefix=POLO, attrValues=[M, Black] → POLO-M-BLACK
     */
    private String buildSku(String skuPrefix, String productCode,
                            List<AttributeValue> attrValues, String customSku) {
        if (customSku != null && !customSku.isBlank()) {
            return customSku.toUpperCase();
        }
        String base = (skuPrefix != null && !skuPrefix.isBlank()) ? skuPrefix : productCode;
        String valuePart = attrValues.stream()
                .map(av -> av.getValue().toUpperCase().replace(" ", ""))
                .collect(Collectors.joining("-"));
        return valuePart.isBlank() ? base : base + "-" + valuePart;
    }

    /**
     * Mô tả biến thể dạng "Size: M | Màu: Đen"
     */
    private String buildDescription(List<AttributeValue> attrValues) {
        return attrValues.stream()
                .map(av -> av.getProductAttribute().getAttributeName() + ": " + av.getValue())
                .collect(Collectors.joining(" | "));
    }

    /** Load và validate danh sách AttributeValue */
    private List<AttributeValue> loadAttributeValues(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return ids.stream()
                .map(id -> attributeValueRepository.findByIdAndIsDeletedFalse(id)
                        .orElseThrow(() -> new ResourceNotFoundException("AttributeValue", "id", id)))
                .collect(Collectors.toList());
    }

    /** Build VariantResponse đầy đủ kèm thuộc tính */
    private VariantResponse buildVariantResponse(ProductVariant variant) {
        List<VariantAttributeValue> vavList = variantAttributeValueRepository
                .findByProductVariantIdAndIsDeletedFalse(variant.getId());

        List<VariantResponse.VariantAttributeDetail> attrDetails = vavList.stream()
                .map(vav -> VariantResponse.VariantAttributeDetail.builder()
                        .attributeId(vav.getProductAttribute().getId())
                        .attributeCode(vav.getProductAttribute().getAttributeCode())
                        .attributeName(vav.getProductAttribute().getAttributeName())
                        .valueId(vav.getAttributeValue().getId())
                        .value(vav.getAttributeValue().getValue())
                        .build())
                .collect(Collectors.toList());

        String desc = buildDescription(vavList.stream()
                .map(VariantAttributeValue::getAttributeValue)
                .collect(Collectors.toList()));

        Product product = variant.getProduct();
        return VariantResponse.builder()
                .id(variant.getId())
                .variantCode(variant.getVariantCode())
                .sku(variant.getSku())
                .barcode(variant.getBarcode())
                .imageUrl(variant.getImageUrl())
                .price(variant.getPrice() != null ? variant.getPrice() : product.getBasePrice())
                .status(variant.getStatus().name())
                .productId(product.getId())
                .productCode(product.getProductCode())
                .productName(product.getName())
                .variantDescription(desc.isBlank() ? "Mặc định" : desc)
                .attributes(attrDetails)
                .isActive(variant.getIsActive())
                .isDeleted(variant.getIsDeleted())
                .createdAt(variant.getCreatedAt())
                .createdBy(variant.getCreatedBy())
                .updatedAt(variant.getUpdatedAt())
                .updatedBy(variant.getUpdatedBy())
                .build();
    }

    private CreateVariantResponse mapToCreateResponse(ProductVariant saved, Product product, String desc) {
        return CreateVariantResponse.builder()
                .id(saved.getId())
                .variantCode(saved.getVariantCode())
                .sku(saved.getSku())
                .barcode(saved.getBarcode())
                .price(saved.getPrice() != null ? saved.getPrice() : product.getBasePrice())
                .status(saved.getStatus().name())
                .productId(product.getId())
                .productCode(product.getProductCode())
                .variantDescription(desc.isBlank() ? "Mặc định" : desc)
                .createdAt(saved.getCreatedAt())
                .createdBy(saved.getCreatedBy())
                .build();
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Người dùng chưa đăng nhập hoặc token không hợp lệ");
    }

    private void initializeInventoryBalanceBatch(List<ProductVariant> variants, List<Branch> activeBranches, String username) {
        LocalDateTime now = LocalDateTime.now();
        List<InventoryBalance> balances = new ArrayList<>();
        
        for (ProductVariant variant : variants) {
            for (Branch branch : activeBranches) {
                InventoryBalance balance = InventoryBalance.builder()
                    .productVariant(variant)
                    .branch(branch)
                    .availableQuantity(BigDecimal.ZERO)
                    .reservedQuantity(BigDecimal.ZERO)
                    .damagedQuantity(BigDecimal.ZERO)
                    .minimumQuantity(BigDecimal.ZERO)
                    .reorderPoint(BigDecimal.ZERO)
                    .lastUpdated(now)
                    .build();
                balance.setCreatedBy(username);
                balance.setIsDeleted(false);
                balances.add(balance);
            }
        }
        
        inventoryBalanceRepository.saveAll(balances);
    }

    @Override
    @Transactional(readOnly = true)
    public VariantResponse getBySku(String sku) {
        ProductVariant variant = productVariantRepository.findBySkuAndIsDeletedFalse(sku)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "sku", sku));
        return buildVariantResponse(variant);
    }

    @Override
    @Transactional(readOnly = true)
    public VariantResponse getByBarcode(String barcode) {
        ProductVariant variant = productVariantRepository.findByBarcodeAndIsDeletedFalse(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "barcode", barcode));
        return buildVariantResponse(variant);
    }

    @Override
    @LogActivity(actionType = "CREATE", entityName = "ProductVariant", entityClass = ProductVariant.class)
    public VariantResponse createSingleVariant(Long productId, CreateSingleVariantRequest request) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (productVariantRepository.existsBySkuAndIsDeletedFalse(request.getSku())) {
            throw new DuplicateResourceException("ProductVariant", "sku", request.getSku());
        }

        if (request.getBarcode() != null && !request.getBarcode().isBlank()
                && productVariantRepository.existsByBarcodeAndIsDeletedFalse(request.getBarcode())) {
            throw new DuplicateResourceException("ProductVariant", "barcode", request.getBarcode());
        }

        String username = getCurrentUsername();
        String variantCode = generateVariantCode();

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .variantCode(variantCode)
                .sku(request.getSku())
                .barcode(request.getBarcode())
                .status(VariantStatus.ACTIVE)
                .build();
        variant.setIsActive(true);
        variant.setIsDeleted(false);
        variant.setCreatedBy(username);

        ProductVariant saved = productVariantRepository.save(variant);

        if (request.getAttributes() != null) {
            for (CreateSingleVariantRequest.AttributeInput attrInput : request.getAttributes()) {
                AttributeValue av = attributeValueRepository.findByIdAndIsDeletedFalse(attrInput.getValueId())
                        .orElseThrow(() -> new ResourceNotFoundException("AttributeValue", "id", attrInput.getValueId()));
                
                VariantAttributeValue vav = VariantAttributeValue.builder()
                        .productVariant(saved)
                        .productAttribute(av.getProductAttribute())
                        .attributeValue(av)
                        .build();
                vav.setIsDeleted(false);
                vav.setCreatedBy(username);
                variantAttributeValueRepository.save(vav);
            }
        }

        List<Branch> activeBranches = branchRepository.findAllBranchesList(null, true);
        initializeInventoryBalanceBatch(List.of(saved), activeBranches, username);

        return buildVariantResponse(saved);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<VariantResponse> getAllVariants() {
        return productVariantRepository.findByIsDeletedFalse().stream()
                .map(this::buildVariantResponse)
                .collect(java.util.stream.Collectors.toList());
    }
}
