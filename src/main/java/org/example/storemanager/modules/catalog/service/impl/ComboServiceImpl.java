package org.example.storemanager.modules.catalog.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.shared.config.LogActivity;
import org.example.storemanager.modules.catalog.dto.request.combo.ComboDeductStockRequest;
import org.example.storemanager.modules.catalog.dto.request.combo.ComboDetailRequest;
import org.example.storemanager.modules.catalog.dto.request.combo.CreateComboRequest;
import org.example.storemanager.modules.catalog.dto.request.combo.UpdateComboRequest;
import org.example.storemanager.modules.catalog.dto.response.combo.ComboDetailResponse;
import org.example.storemanager.modules.catalog.dto.response.combo.ComboResponse;
import org.example.storemanager.modules.catalog.dto.response.combo.ComboSaveResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.catalog.entity.Combo;
import org.example.storemanager.modules.catalog.entity.ComboDetail;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.shared.enums.ErrorCode;
import org.example.storemanager.shared.enums.catalog.ComboType;
import org.example.storemanager.shared.exception.DuplicateResourceException;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.catalog.repository.ComboDetailRepository;
import org.example.storemanager.modules.catalog.repository.ComboRepository;
import org.example.storemanager.modules.catalog.repository.ProductRepository;
import org.example.storemanager.modules.catalog.service.ComboService;
import org.example.storemanager.modules.inventory.service.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ComboServiceImpl implements ComboService {

    private final ComboRepository comboRepository;
    private final ComboDetailRepository comboDetailRepository;
    private final ProductRepository productRepository;
    private final org.example.storemanager.modules.catalog.repository.ProductVariantRepository productVariantRepository;
    private final InventoryService inventoryService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ComboResponse> search(String search, Boolean isActive, Pageable pageable) {
        if (search != null && search.trim().isEmpty()) {
            search = null;
        }
        Page<Combo> page = comboRepository.search(search, isActive, pageable);
        List<ComboResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PageResponse.<ComboResponse>builder()
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
    public ComboResponse getById(Long id) {
        return mapToResponse(findActiveEntity(id));
    }

    @Override
    @LogActivity(actionType = "CREATE", entityName = "Combo", entityClass = Combo.class)
    public ComboSaveResponse create(CreateComboRequest request) {
        if (comboRepository.existsByComboCodeAndIsDeletedFalse(request.getComboCode())) {
            throw new DuplicateResourceException("Combo", "comboCode", request.getComboCode());
        }
        if (request.getBarcode() != null && !request.getBarcode().isBlank()) {
            if (productVariantRepository.existsByBarcodeAndIsDeletedFalse(request.getBarcode().trim())) {
                throw new DuplicateResourceException("Combo", "barcode", request.getBarcode());
            }
        }
        validateDetails(request.getDetails());


        String username = getCurrentUsername();
        Combo combo = Combo.builder()
                .comboCode(request.getComboCode())
                .comboName(request.getComboName())
                .barcode(request.getBarcode())
                .description(request.getDescription())
                .comboType(request.getComboType() != null ? request.getComboType() : ComboType.DYNAMIC_VIRTUAL)
                .price(request.getPrice())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        combo.setIsDeleted(false);
        combo.setCreatedBy(username);

        Combo saved = comboRepository.save(combo);
        List<ComboDetail> details = saveDetails(saved, request.getDetails(), username);
        List<String> warnings = buildPriceWarnings(saved.getPrice(), details);

        return buildSaveResponse(saved, warnings);
    }

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "Combo", entityClass = Combo.class)
    public ComboSaveResponse update(Long id, UpdateComboRequest request) {
        Combo combo = findActiveEntity(id);

        if (comboRepository.existsByComboCodeAndIdNotAndIsDeletedFalse(request.getComboCode(), id)) {
            throw new DuplicateResourceException("Combo", "comboCode", request.getComboCode());
        }

        if (request.getDetails() != null) {
            validateDetails(request.getDetails());
        }

        String username = getCurrentUsername();
        combo.setComboCode(request.getComboCode());
        combo.setComboName(request.getComboName());
        combo.setBarcode(request.getBarcode());
        combo.setDescription(request.getDescription());
        if (request.getComboType() != null) {
            combo.setComboType(request.getComboType());
        }
        combo.setPrice(request.getPrice());
        combo.setStartDate(request.getStartDate());
        combo.setEndDate(request.getEndDate());
        if (request.getIsActive() != null) {
            combo.setIsActive(request.getIsActive());
        }
        combo.setUpdatedBy(username);

        List<ComboDetail> details;
        if (request.getDetails() != null) {
            comboDetailRepository.deleteByComboId(id);
            details = saveDetails(combo, request.getDetails(), username);
        } else {
            details = comboDetailRepository.findByComboIdAndIsDeletedFalse(id);
        }

        List<String> warnings = buildPriceWarnings(combo.getPrice(), details);
        return buildSaveResponse(combo, warnings);
    }

    @Override
    @LogActivity(actionType = "DELETE", entityName = "Combo", entityClass = Combo.class)
    public void delete(Long id) {
        Combo combo = findActiveEntity(id);
        combo.setIsDeleted(true);
        combo.setDeletedAt(LocalDateTime.now());
        combo.setDeletedBy(getCurrentUsername());
        comboRepository.save(combo);
    }

    @Override
    @LogActivity(actionType = "DEDUCT_STOCK", entityName = "Combo", entityClass = Combo.class)
    public void deductDynamicComboStock(Long comboId, ComboDeductStockRequest request) {
        Combo combo = findActiveEntity(comboId);

        if (combo.getComboType() != ComboType.DYNAMIC_VIRTUAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    ErrorCode.COMBO_TYPE_NOT_SUPPORTED.getDefaultMessage()
                            + " (Phase 2 chỉ hỗ trợ DYNAMIC_VIRTUAL)");
        }

        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity phải lớn hơn 0");
        }

        List<ComboDetail> details = comboDetailRepository.findByComboIdAndIsDeletedFalse(comboId);
        if (details.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Combo không có thành phần");
        }

        for (ComboDetail detail : details) {
            BigDecimal deductQty = detail.getQuantity().multiply(request.getQuantity());
            inventoryService.deductStock(
                    request.getWarehouseZoneId(),
                    request.getBranchId(),
                    detail.getProduct().getId(),
                    null,
                    null,
                    deductQty,
                    "COMBO_SALE",
                    request.getReferenceDocument(),
                    comboId);
        }
    }

    private void validateDetails(List<ComboDetailRequest> details) {
        if (details == null || details.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Combo phải có ít nhất một thành phần");
        }
        Set<Long> productIds = new HashSet<>();
        for (ComboDetailRequest req : details) {
            if (!productIds.add(req.getProductId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Không được trùng sản phẩm trong cùng combo");
            }
        }
    }

    private List<ComboDetail> saveDetails(Combo combo, List<ComboDetailRequest> requests, String username) {
        List<ComboDetail> entities = new ArrayList<>();
        for (ComboDetailRequest req : requests) {
            Product product = productRepository.findByIdAndIsDeletedFalse(req.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", req.getProductId()));

            BigDecimal unitPrice = req.getUnitPriceAtCreation() != null
                    ? req.getUnitPriceAtCreation()
                    : product.getBasePrice();

            ComboDetail detail = ComboDetail.builder()
                    .combo(combo)
                    .product(product)
                    .quantity(req.getQuantity())
                    .unitPriceAtCreation(unitPrice)
                    .build();
            detail.setIsDeleted(false);
            detail.setCreatedBy(username);
            entities.add(detail);
        }
        return comboDetailRepository.saveAll(entities);
    }

    private List<String> buildPriceWarnings(BigDecimal comboPrice, List<ComboDetail> details) {
        BigDecimal retailTotal = details.stream()
                .map(d -> {
                    BigDecimal unitPrice = d.getUnitPriceAtCreation() != null
                            ? d.getUnitPriceAtCreation()
                            : d.getProduct().getBasePrice();
                    return unitPrice.multiply(d.getQuantity());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<String> warnings = new ArrayList<>();
        if (comboPrice != null && comboPrice.compareTo(retailTotal) > 0) {
            warnings.add(String.format(
                    "Giá combo (%s) cao hơn tổng giá bán lẻ (%s). Vui lòng kiểm tra lại.",
                    comboPrice.toPlainString(), retailTotal.toPlainString()));
        }
        return warnings;
    }

    private ComboSaveResponse buildSaveResponse(Combo combo, List<String> warnings) {
        ComboSaveResponse.ComboSaveResponseBuilder builder = ComboSaveResponse.builder()
                .combo(mapToResponse(combo))
                .warnings(warnings);
        if (!warnings.isEmpty()) {
            builder.warningCode(ErrorCode.COMBO_PRICE_ABOVE_RETAIL.name());
        }
        return builder.build();
    }

    private Combo findActiveEntity(Long id) {
        return comboRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.COMBO_NOT_FOUND, "Combo", "id", id));
    }

    private ComboResponse mapToResponse(Combo combo) {
        List<ComboDetailResponse> items = comboDetailRepository.findByComboIdAndIsDeletedFalse(combo.getId())
                .stream()
                .map(this::mapDetailToResponse)
                .collect(Collectors.toList());

        return ComboResponse.builder()
                .id(combo.getId())
                .comboCode(combo.getComboCode())
                .comboName(combo.getComboName())
                .barcode(combo.getBarcode())
                .description(combo.getDescription())
                .comboType(combo.getComboType())
                .price(combo.getPrice())
                .startDate(combo.getStartDate())
                .endDate(combo.getEndDate())
                .isActive(combo.getIsActive())
                .items(items)
                .createdAt(combo.getCreatedAt())
                .build();
    }

    private ComboDetailResponse mapDetailToResponse(ComboDetail detail) {
        BigDecimal unitPrice = detail.getUnitPriceAtCreation() != null
                ? detail.getUnitPriceAtCreation()
                : detail.getProduct().getBasePrice();
        return ComboDetailResponse.builder()
                .id(detail.getId())
                .productId(detail.getProduct().getId())
                .productCode(detail.getProduct().getProductCode())
                .productName(detail.getProduct().getName())
                .quantity(detail.getQuantity())
                .unitPriceAtCreation(unitPrice)
                .price(unitPrice)
                .build();
    }

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
            return auth.getName();
        }
        return "system";
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComboDetailResponse> getItems(Long comboId) {
        findActiveEntity(comboId);
        return comboDetailRepository.findByComboIdAndIsDeletedFalse(comboId).stream()
                .map(this::mapDetailToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ComboDetailResponse addItem(Long comboId, ComboDetailRequest request) {
        Combo combo = findActiveEntity(comboId);
        
        List<ComboDetail> existing = comboDetailRepository.findByComboIdAndIsDeletedFalse(comboId);
        for (ComboDetail detail : existing) {
            if (detail.getProduct().getId().equals(request.getProductId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không được trùng sản phẩm trong cùng combo");
            }
        }
        
        Product product = productRepository.findByIdAndIsDeletedFalse(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));
        
        BigDecimal unitPrice = request.getUnitPriceAtCreation() != null
                ? request.getUnitPriceAtCreation()
                : product.getBasePrice();

        ComboDetail detail = ComboDetail.builder()
                .combo(combo)
                .product(product)
                .quantity(request.getQuantity())
                .unitPriceAtCreation(unitPrice)
                .build();
        detail.setIsDeleted(false);
        detail.setCreatedBy(getCurrentUsername());
        
        ComboDetail saved = comboDetailRepository.save(detail);
        return mapDetailToResponse(saved);
    }

    @Override
    @Transactional
    public ComboDetailResponse updateItem(Long id, ComboDetailRequest request) {
        ComboDetail detail = comboDetailRepository.findById(id)
                .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("ComboDetail", "id", id));
        
        if (request.getQuantity() != null) {
            detail.setQuantity(request.getQuantity());
        }
        if (request.getUnitPriceAtCreation() != null) {
            detail.setUnitPriceAtCreation(request.getUnitPriceAtCreation());
        }
        
        detail.setUpdatedBy(getCurrentUsername());
        ComboDetail saved = comboDetailRepository.save(detail);
        return mapDetailToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        ComboDetail detail = comboDetailRepository.findById(id)
                .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("ComboDetail", "id", id));
        detail.setIsDeleted(true);
        detail.setDeletedAt(LocalDateTime.now());
        detail.setDeletedBy(getCurrentUsername());
        comboDetailRepository.save(detail);
    }
}
