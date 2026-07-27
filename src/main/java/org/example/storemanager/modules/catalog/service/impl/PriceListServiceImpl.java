package org.example.storemanager.modules.catalog.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.shared.config.LogActivity;
import org.example.storemanager.modules.catalog.dto.request.pricelist.CreatePriceListRequest;
import org.example.storemanager.modules.catalog.dto.request.pricelist.PriceListDetailRequest;
import org.example.storemanager.modules.catalog.dto.request.pricelist.UpdatePriceListRequest;
import org.example.storemanager.modules.catalog.dto.response.pricelist.ActivePriceResponse;
import org.example.storemanager.modules.catalog.dto.response.pricelist.PriceListDetailResponse;
import org.example.storemanager.modules.catalog.dto.response.pricelist.PriceListResponse;
import org.example.storemanager.modules.catalog.entity.PriceList;
import org.example.storemanager.modules.catalog.entity.PriceListDetail;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.catalog.entity.ProductUnit;
import org.example.storemanager.modules.catalog.entity.ProductVariant;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.shared.enums.ErrorCode;
import org.example.storemanager.shared.exception.DuplicateResourceException;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.catalog.repository.PriceListDetailRepository;
import org.example.storemanager.modules.catalog.repository.PriceListRepository;
import org.example.storemanager.modules.catalog.repository.ProductRepository;
import org.example.storemanager.modules.catalog.repository.ProductUnitRepository;
import org.example.storemanager.modules.system.repository.BranchRepository;
import org.example.storemanager.modules.catalog.service.PriceListService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PriceListServiceImpl implements PriceListService {

    private final PriceListRepository priceListRepository;
    private final PriceListDetailRepository priceListDetailRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final ProductUnitRepository productUnitRepository;
    private final org.example.storemanager.modules.catalog.repository.ProductVariantRepository productVariantRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PriceListResponse> getAll() {
        return priceListRepository.findByIsDeletedFalseOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PriceListResponse getById(Long id) {
        PriceList priceList = findActiveEntity(id);
        return mapToResponse(priceList);
    }

    @Override
    @LogActivity(actionType = "CREATE", entityName = "PriceList", entityClass = PriceList.class)
    public PriceListResponse create(CreatePriceListRequest request) {
        if (priceListRepository.existsByListCodeAndIsDeletedFalse(request.getListCode())) {
            throw new DuplicateResourceException("PriceList", "listCode", request.getListCode());
        }

        boolean isActive = request.getIsActive() != null ? request.getIsActive() : false;
        validateDates(request.getStartDate(), request.getEndDate());
        validateNoOverlap(request.getBranchId(), request.getStartDate(), request.getEndDate(), null, isActive);

        Branch branch = resolveBranch(request.getBranchId());
        String username = getCurrentUsername();

        PriceList priceList = PriceList.builder()
                .listCode(request.getListCode())
                .listName(request.getListName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .branch(branch)
                .isActive(isActive)
                .build();
        priceList.setIsDeleted(false);
        priceList.setCreatedBy(username);

        PriceList saved = priceListRepository.save(priceList);
        saveDetails(saved, request.getDetails(), username);

        return mapToResponse(saved);
    }

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "PriceList", entityClass = PriceList.class)
    public PriceListResponse update(Long id, UpdatePriceListRequest request) {
        PriceList priceList = findActiveEntity(id);

        if (priceListRepository.existsByListCodeAndIdNotAndIsDeletedFalse(request.getListCode(), id)) {
            throw new DuplicateResourceException("PriceList", "listCode", request.getListCode());
        }

        boolean isActive = request.getIsActive() != null ? request.getIsActive() : priceList.getIsActive();
        validateDates(request.getStartDate(), request.getEndDate());
        validateNoOverlap(request.getBranchId(), request.getStartDate(), request.getEndDate(), id, isActive);

        Branch branch = resolveBranch(request.getBranchId());
        String username = getCurrentUsername();

        priceList.setListCode(request.getListCode());
        priceList.setListName(request.getListName());
        priceList.setStartDate(request.getStartDate());
        priceList.setEndDate(request.getEndDate());
        priceList.setBranch(branch);
        priceList.setIsActive(isActive);
        priceList.setUpdatedBy(username);

        priceListDetailRepository.deleteByPriceListId(id);
        saveDetails(priceList, request.getDetails(), username);

        return mapToResponse(priceList);
    }

    @Override
    @LogActivity(actionType = "DELETE", entityName = "PriceList", entityClass = PriceList.class)
    public void delete(Long id) {
        PriceList priceList = findActiveEntity(id);
        priceList.setIsDeleted(true);
        priceList.setDeletedAt(LocalDateTime.now());
        priceList.setDeletedBy(getCurrentUsername());
        priceListRepository.save(priceList);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivePriceResponse resolveActivePrice(Long branchId, Long productId, Long productUnitId) {
        ProductUnit productUnit = resolveProductUnit(productId, productUnitId);
        LocalDateTime now = LocalDateTime.now();

        List<PriceList> activeLists = priceListRepository.findActiveForBranch(branchId, now);
        for (PriceList list : activeLists) {
            List<PriceListDetail> details = priceListDetailRepository.findByPriceListIdAndIsDeletedFalse(list.getId());
            for (PriceListDetail detail : details) {
                if (detail.getProductUnit().getId().equals(productUnit.getId())) {
                    return ActivePriceResponse.builder()
                            .priceListId(list.getId())
                            .listCode(list.getListCode())
                            .listName(list.getListName())
                            .productId(productId)
                            .productUnitId(productUnit.getId())
                            .price(detail.getPrice())
                            .build();
                }
            }
        }
        throw new ResourceNotFoundException(ErrorCode.PRICE_LIST_NOT_FOUND, "PriceListDetail",
                "productUnitId", productUnit.getId());
    }

    private void saveDetails(PriceList priceList, List<PriceListDetailRequest> details, String username) {
        if (details == null || details.isEmpty()) {
            return;
        }

        Set<String> uniqueKeys = new HashSet<>();
        List<PriceListDetail> entities = new ArrayList<>();

        for (PriceListDetailRequest req : details) {
            Product product = productRepository.findByIdAndIsDeletedFalse(req.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", req.getProductId()));

            ProductUnit productUnit = resolveProductUnit(product.getId(), req.getProductUnitId());
            if (!productUnit.getProduct().getId().equals(product.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "ProductUnit không thuộc sản phẩm đã chọn");
            }

            String key = priceList.getId() + ":" + product.getId() + ":" + productUnit.getId();
            if (!uniqueKeys.add(key)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Trùng cấu hình giá cho cùng sản phẩm và đơn vị quy đổi");
            }

            PriceListDetail detail = PriceListDetail.builder()
                    .priceList(priceList)
                    .product(product)
                    .productUnit(productUnit)
                    .price(req.getPrice())
                    .build();
            detail.setIsDeleted(false);
            detail.setCreatedBy(username);
            entities.add(detail);
        }

        priceListDetailRepository.saveAll(entities);
    }

    private ProductUnit resolveProductUnit(Long productId, Long productUnitId) {
        if (productUnitId != null) {
            ProductUnit unit = productUnitRepository.findByIdAndIsDeletedFalse(productUnitId)
                    .orElseThrow(() -> new ResourceNotFoundException("ProductUnit", "id", productUnitId));
            if (!unit.getProduct().getId().equals(productId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "ProductUnit không khớp productId");
            }
            return unit;
        }
        return productUnitRepository.findByProductIdAndIsBaseUnitTrueAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Sản phẩm chưa có đơn vị gốc (ProductUnit isBaseUnit). Vui lòng truyền productUnitId."));
    }

    private void validateDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "endDate bắt buộc phải lớn hơn startDate");
        }
    }

    private void validateNoOverlap(Long branchId, LocalDateTime startDate, LocalDateTime endDate,
                                   Long excludeId, boolean isActive) {
        if (!isActive || startDate == null || endDate == null) {
            return;
        }
        if (priceListRepository.existsOverlappingActive(branchId, startDate, endDate, excludeId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    ErrorCode.PRICE_LIST_DATE_OVERLAP.getDefaultMessage());
        }
    }

    private Branch resolveBranch(Long branchId) {
        if (branchId == null) {
            return null;
        }
        return branchRepository.findByIdAndIsDeletedFalse(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", branchId));
    }

    private PriceList findActiveEntity(Long id) {
        return priceListRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRICE_LIST_NOT_FOUND,
                        "PriceList", "id", id));
    }

    private PriceListResponse mapToResponse(PriceList priceList) {
        List<PriceListDetailResponse> details = priceListDetailRepository
                .findByPriceListIdAndIsDeletedFalse(priceList.getId()).stream()
                .map(this::mapDetailToResponse)
                .collect(Collectors.toList());

        return PriceListResponse.builder()
                .id(priceList.getId())
                .listCode(priceList.getListCode())
                .listName(priceList.getListName())
                .startDate(priceList.getStartDate())
                .endDate(priceList.getEndDate())
                .isActive(priceList.getIsActive())
                .branchId(priceList.getBranch() != null ? priceList.getBranch().getId() : null)
                .branchName(priceList.getBranch() != null ? priceList.getBranch().getBranchName() : null)
                .details(details)
                .createdAt(priceList.getCreatedAt())
                .build();
    }

    private PriceListDetailResponse mapDetailToResponse(PriceListDetail detail) {
        ProductUnit pu = detail.getProductUnit();
        return PriceListDetailResponse.builder()
                .id(detail.getId())
                .productId(detail.getProduct().getId())
                .productCode(detail.getProduct().getProductCode())
                .productName(detail.getProduct().getName())
                .productUnitId(pu.getId())
                .unitCode(pu.getUnit().getUnitCode())
                .unitName(pu.getUnit().getUnitName())
                .isBaseUnit(pu.getIsBaseUnit())
                .price(detail.getPrice())
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
    public List<PriceListDetailResponse> getItems(Long priceListId) {
        findActiveEntity(priceListId);
        return priceListDetailRepository.findByPriceListIdAndIsDeletedFalse(priceListId).stream()
                .map(this::mapDetailToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PriceListDetailResponse addItem(Long priceListId, PriceListDetailRequest request) {
        PriceList priceList = findActiveEntity(priceListId);
        Product product = productRepository.findByIdAndIsDeletedFalse(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));
        ProductUnit productUnit = resolveProductUnit(product.getId(), request.getProductUnitId());

        List<PriceListDetail> existing = priceListDetailRepository.findByPriceListIdAndIsDeletedFalse(priceListId);
        for (PriceListDetail detail : existing) {
            if (detail.getProduct().getId().equals(product.getId()) &&
                detail.getProductUnit().getId().equals(productUnit.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trùng cấu hình giá cho cùng sản phẩm và đơn vị quy đổi");
            }
        }

        PriceListDetail detail = PriceListDetail.builder()
                .priceList(priceList)
                .product(product)
                .productUnit(productUnit)
                .price(request.getPrice())
                .build();
        detail.setIsDeleted(false);
        detail.setCreatedBy(getCurrentUsername());
        PriceListDetail saved = priceListDetailRepository.save(detail);
        return mapDetailToResponse(saved);
    }

    @Override
    @Transactional
    public PriceListDetailResponse updateItem(Long id, java.math.BigDecimal price) {
        PriceListDetail detail = priceListDetailRepository.findById(id)
                .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("PriceListDetail", "id", id));
        detail.setPrice(price);
        detail.setUpdatedBy(getCurrentUsername());
        PriceListDetail saved = priceListDetailRepository.save(detail);
        return mapDetailToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        PriceListDetail detail = priceListDetailRepository.findById(id)
                .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("PriceListDetail", "id", id));
        detail.setIsDeleted(true);
        detail.setDeletedAt(LocalDateTime.now());
        detail.setDeletedBy(getCurrentUsername());
        priceListDetailRepository.save(detail);
    }

    @Override
    @Transactional(readOnly = true)
    public java.math.BigDecimal getVariantPrice(Long variantId, Long branchId) {
        ProductVariant variant = productVariantRepository.findByIdAndIsDeletedFalse(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", variantId));
        
        Product product = variant.getProduct();
        
        LocalDateTime now = LocalDateTime.now();
        List<PriceList> activeLists = priceListRepository.findActiveForBranch(branchId, now);
        for (PriceList list : activeLists) {
            List<PriceListDetail> details = priceListDetailRepository.findByPriceListIdAndIsDeletedFalse(list.getId());
            for (PriceListDetail detail : details) {
                if (detail.getProduct().getId().equals(product.getId())) {
                    if (detail.getProductUnit() != null && Boolean.TRUE.equals(detail.getProductUnit().getIsBaseUnit())) {
                        return detail.getPrice();
                    }
                }
            }
        }
        
        if (variant.getPrice() != null) {
            return variant.getPrice();
        }
        
        return product.getBasePrice();
    }

    @Override
    public org.example.storemanager.modules.catalog.dto.response.pricelist.ActualPriceResponse resolveActualPrice(Long variantId, Long branchId) {
        ProductVariant variant = productVariantRepository.findByIdAndIsDeletedFalse(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", variantId));
        
        Product product = variant.getProduct();
        LocalDateTime now = LocalDateTime.now();
        List<Branch> activeBranches = branchRepository.findAllBranchesList(null, true);
        
        List<PriceList> activeLists = priceListRepository.findActiveForBranch(branchId, now);
        for (PriceList list : activeLists) {
            List<PriceListDetail> details = priceListDetailRepository.findByPriceListIdAndIsDeletedFalse(list.getId());
            for (PriceListDetail detail : details) {
                if (detail.getProduct().getId().equals(product.getId())) {
                    if (detail.getProductUnit() != null && Boolean.TRUE.equals(detail.getProductUnit().getIsBaseUnit())) {
                        return org.example.storemanager.modules.catalog.dto.response.pricelist.ActualPriceResponse.builder()
                                .variantId(variantId)
                                .productId(product.getId())
                                .finalPrice(detail.getPrice())
                                .source("PRICE_LIST")
                                .priceListId(list.getId())
                                .priceListName(list.getListName())
                                .priceSource("PRICE_LIST")
                                .resolvedAt(LocalDateTime.now())
                                .build();
                    }
                }
            }
        }
        
        if (variant.getPrice() != null) {
            return org.example.storemanager.modules.catalog.dto.response.pricelist.ActualPriceResponse.builder()
                    .variantId(variantId)
                    .productId(product.getId())
                    .finalPrice(variant.getPrice())
                    .source("VARIANT_OVERRIDE")
                    .priceSource("VARIANT_OVERRIDE")
                    .resolvedAt(LocalDateTime.now())
                    .build();
        }
        
        return org.example.storemanager.modules.catalog.dto.response.pricelist.ActualPriceResponse.builder()
                .variantId(variantId)
                .productId(product.getId())
                .finalPrice(product.getBasePrice())
                .source("PRODUCT_BASE")
                .priceSource("PRODUCT_BASE")
                .resolvedAt(LocalDateTime.now())
                .build();
    }
}
