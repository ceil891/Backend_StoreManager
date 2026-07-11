package org.example.storemanager.service.catalog.impl;

import org.example.storemanager.config.LogActivity;
import org.example.storemanager.service.common.CloudinaryService;
import java.util.Arrays;
import org.example.storemanager.dto.request.catalog.product.CreateProductRequest;
import org.example.storemanager.dto.request.catalog.product.UpdateProductRequest;
import org.example.storemanager.dto.request.catalog.productunit.ProductUnitRequest;
import org.example.storemanager.dto.response.catalog.product.*;
import org.example.storemanager.dto.response.catalog.productunit.ProductUnitResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.entity.catalog.Product;
import org.example.storemanager.entity.catalog.ProductCategory;
import org.example.storemanager.entity.catalog.ProductUnit;
import org.example.storemanager.entity.catalog.Unit;
import org.example.storemanager.entity.inventory.SizeInventory;
import org.example.storemanager.exception.DuplicateResourceException;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.catalog.CategoriesRepository;
import org.example.storemanager.repository.catalog.ProductRepository;
import org.example.storemanager.repository.catalog.ProductUnitRepository;
import org.example.storemanager.repository.catalog.UnitRepository;
import org.example.storemanager.repository.inventory.SizeInventoryRepository;
import org.example.storemanager.service.catalog.ProductService;
import org.example.storemanager.service.catalog.ProductUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoriesRepository categoriesRepository;
    private final UnitRepository unitRepository;
    private final ProductUnitRepository productUnitRepository;
    private final SizeInventoryRepository sizeInventoryRepository;
    private final ProductUnitService productUnitService;
    private final CloudinaryService cloudinaryService;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoriesRepository categoriesRepository,
                              UnitRepository unitRepository,
                              ProductUnitRepository productUnitRepository,
                              SizeInventoryRepository sizeInventoryRepository,
                              ProductUnitService productUnitService,
                              CloudinaryService cloudinaryService,
                              org.springframework.context.ApplicationEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.categoriesRepository = categoriesRepository;
        this.unitRepository = unitRepository;
        this.productUnitRepository = productUnitRepository;
        this.sizeInventoryRepository = sizeInventoryRepository;
        this.productUnitService = productUnitService;
        this.cloudinaryService = cloudinaryService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @LogActivity(actionType = "CREATE", entityName = "Product", entityClass = Product.class)
    public CreateProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsByProductCodeAndIsDeletedFalse(request.getProductCode())) {
            throw new DuplicateResourceException("Product", "productCode", request.getProductCode());
        }

        ProductCategory category = categoriesRepository.findByIdAndIsDeletedFalse(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", request.getCategoryId()));

        Unit baseUnit = unitRepository.findByIdAndIsDeletedFalse(request.getBaseUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", request.getBaseUnitId()));

        String username = getCurrentUsername();

        productUnitService.validateBarcode(request.getBarcode(), null, null);

        Product product = Product.builder()
                .productCode(request.getProductCode())
                .name(request.getName())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .costPrice(request.getCostPrice())
                .brand(request.getBrand())
                .mainImageUrl(request.getMainImageUrl())
                .barcode(request.getBarcode())
                .weight(request.getWeight())
                .reorderPoint(request.getReorderPoint())
                .minStock(request.getMinStock())
                .maxStock(request.getMaxStock())
                .galleryImages(request.getGalleryImages())
                .variants(request.getVariants())
                .category(category)
                .baseUnit(baseUnit)
                .build();

        product.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        product.setIsDeleted(false);
        product.setCreatedBy(username);

        Product savedProduct = productRepository.save(product);

        productUnitService.createBaseProductUnit(savedProduct, baseUnit, username);

        if (request.getConversionUnits() != null) {
            for (ProductUnitRequest uReq : request.getConversionUnits()) {
                if (uReq.getUnitId().equals(request.getBaseUnitId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Đơn vị quy đổi không được trùng với đơn vị gốc.");
                }

                Unit conversionUnit = unitRepository.findByIdAndIsDeletedFalse(uReq.getUnitId())
                        .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", uReq.getUnitId()));

                productUnitService.validateBarcode(uReq.getBarcode(), null, savedProduct.getId());

                ProductUnit pu = ProductUnit.builder()
                        .product(savedProduct)
                        .unit(conversionUnit)
                        .conversionRate(uReq.getConversionRate())
                        .price(uReq.getPrice())
                        .barcode(uReq.getBarcode())
                        .isBaseUnit(false)
                        .build();
                pu.setIsDeleted(false);
                pu.setIsActive(true);
                pu.setCreatedBy(username);
                productUnitRepository.save(pu);
            }
        }

        return CreateProductResponse.builder()
                .id(savedProduct.getId())
                .productCode(savedProduct.getProductCode())
                .name(savedProduct.getName())
                .categoryId(savedProduct.getCategory().getId())
                .baseUnitId(savedProduct.getBaseUnit().getId())
                .basePrice(savedProduct.getBasePrice())
                .costPrice(savedProduct.getCostPrice())
                .mainImageUrl(savedProduct.getMainImageUrl())
                .galleryImages(savedProduct.getGalleryImages())
                .isActive(savedProduct.getIsActive())
                .createdAt(savedProduct.getCreatedAt())
                .createdBy(savedProduct.getCreatedBy())
                .build();
    }

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "Product", entityClass = Product.class)
    public UpdateProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (productRepository.existsByProductCodeAndIdNotAndIsDeletedFalse(request.getProductCode(), id)) {
            throw new DuplicateResourceException("Product", "productCode", request.getProductCode());
        }

        ProductCategory category = categoriesRepository.findByIdAndIsDeletedFalse(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", request.getCategoryId()));

        Unit baseUnit = unitRepository.findByIdAndIsDeletedFalse(request.getBaseUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", request.getBaseUnitId()));

        productUnitService.validateBarcode(request.getBarcode(), null, id);

        // --- DỌN DẸP ẢNH CLOUDINARY NẾU CÓ THAY ĐỔI ---
        String oldMainImage = product.getMainImageUrl();
        String newMainImage = request.getMainImageUrl();
        if (oldMainImage != null && !oldMainImage.equals(newMainImage)) {
            eventPublisher.publishEvent(new org.example.storemanager.event.CloudinaryDeleteEvent(this, oldMainImage));
        }

        String oldGallery = product.getGalleryImages();
        String newGallery = request.getGalleryImages();
        if (oldGallery != null && !oldGallery.trim().isEmpty()) {
            List<String> oldUrls = Arrays.asList(oldGallery.split(","));
            List<String> newUrls = newGallery != null ? Arrays.asList(newGallery.split(",")) : Collections.emptyList();
            List<String> urlsToDelete = new java.util.ArrayList<>();
            for (String oldUrl : oldUrls) {
                if (!newUrls.contains(oldUrl.trim())) {
                    urlsToDelete.add(oldUrl.trim());
                }
            }
            if (!urlsToDelete.isEmpty()) {
                eventPublisher.publishEvent(new org.example.storemanager.event.CloudinaryDeleteEvent(this, urlsToDelete));
            }
        }
        // ---------------------------------------------

        product.setProductCode(request.getProductCode());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setCostPrice(request.getCostPrice());
        product.setBrand(request.getBrand());
        product.setMainImageUrl(request.getMainImageUrl());
        product.setBarcode(request.getBarcode());
        product.setWeight(request.getWeight());
        product.setReorderPoint(request.getReorderPoint());
        product.setMinStock(request.getMinStock());
        product.setMaxStock(request.getMaxStock());
        product.setGalleryImages(request.getGalleryImages());
        product.setVariants(request.getVariants());
        product.setCategory(category);
        product.setBaseUnit(baseUnit);

        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }
        product.setUpdatedBy(getCurrentUsername());

        Product updatedProduct = productRepository.save(product);

        productUnitService.syncBaseProductUnit(updatedProduct, getCurrentUsername());

        return UpdateProductResponse.builder()
                .id(updatedProduct.getId())
                .productCode(updatedProduct.getProductCode())
                .name(updatedProduct.getName())
                .categoryId(updatedProduct.getCategory().getId())
                .baseUnitId(updatedProduct.getBaseUnit().getId())
                .basePrice(updatedProduct.getBasePrice())
                .costPrice(updatedProduct.getCostPrice())
                .mainImageUrl(updatedProduct.getMainImageUrl())
                .galleryImages(updatedProduct.getGalleryImages())
                .isActive(updatedProduct.getIsActive())
                .updatedAt(updatedProduct.getUpdatedAt())
                .updatedBy(updatedProduct.getUpdatedBy())
                .build();
    }

    @Override
    @LogActivity(actionType = "DELETE", entityName = "Product", entityClass = Product.class)
    public DeleteProductResponse deleteProduct(Long id) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Ràng buộc xóa mềm: Chỉ được xóa khi không hoạt động
        if (Boolean.TRUE.equals(product.getIsActive())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Không thể xóa sản phẩm '" + product.getProductCode() + "' vì vẫn đang hoạt động. " +
                "Vui lòng tắt hoạt động trước, sau đó mới có thể xóa."
            );
        }

        // Kiểm tra tồn kho của sản phẩm ở tất cả các chi nhánh
        List<SizeInventory> stocks = sizeInventoryRepository.findByProductIdAndIsDeletedFalse(id);
        BigDecimal totalStock = stocks.stream()
                .map(SizeInventory::getQuantityPhysical)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalStock.compareTo(BigDecimal.ZERO) > 0) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Không thể xóa sản phẩm '" + product.getProductCode() + "' vì vẫn còn tồn kho thực tế (" + totalStock + ")."
            );
        }

        // --- XÓA TOÀN BỘ ẢNH TRÊN CLOUDINARY KHI SẢN PHẨM BỊ XÓA ---
        if (product.getMainImageUrl() != null) {
            eventPublisher.publishEvent(new org.example.storemanager.event.CloudinaryDeleteEvent(this, product.getMainImageUrl()));
        }
        if (product.getGalleryImages() != null && !product.getGalleryImages().trim().isEmpty()) {
            List<String> galleryUrls = new java.util.ArrayList<>();
            for (String url : product.getGalleryImages().split(",")) {
                galleryUrls.add(url.trim());
            }
            if (!galleryUrls.isEmpty()) {
                eventPublisher.publishEvent(new org.example.storemanager.event.CloudinaryDeleteEvent(this, galleryUrls));
            }
        }
        // ---------------------------------------------------------

        String username = getCurrentUsername();
        product.setIsDeleted(true);
        product.setIsActive(false);
        product.setDeletedAt(LocalDateTime.now());
        product.setDeletedBy(username);
        product.setUpdatedBy(username);

        Product deleted = productRepository.save(product);

        // Cũng soft delete các conversion units liên quan
        List<ProductUnit> pUnits = productUnitRepository.findByProductIdAndIsDeletedFalse(id);
        for (ProductUnit pu : pUnits) {
            pu.setIsDeleted(true);
            pu.setDeletedAt(LocalDateTime.now());
            pu.setDeletedBy(username);
            productUnitRepository.save(pu);
        }

        return DeleteProductResponse.builder()
                .id(deleted.getId())
                .productCode(deleted.getProductCode())
                .isDeleted(deleted.getIsDeleted())
                .deletedAt(deleted.getDeletedAt())
                .deletedBy(deleted.getDeletedBy())
                .build();
    }

    @Override
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "Product", entityClass = Product.class)
    public UpdateProductResponse updateStatus(Long id, Boolean isActive) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setIsActive(isActive);
        product.setUpdatedBy(getCurrentUsername());

        Product updated = productRepository.save(product);
        return UpdateProductResponse.builder()
                .id(updated.getId())
                .productCode(updated.getProductCode())
                .name(updated.getName())
                .categoryId(updated.getCategory().getId())
                .baseUnitId(updated.getBaseUnit().getId())
                .basePrice(updated.getBasePrice())
                .costPrice(updated.getCostPrice())
                .mainImageUrl(updated.getMainImageUrl())
                .galleryImages(updated.getGalleryImages())
                .isActive(updated.getIsActive())
                .updatedAt(updated.getUpdatedAt())
                .updatedBy(updated.getUpdatedBy())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        List<ProductUnitResponse> units = productUnitRepository.findByProductIdAndIsDeletedFalse(id)
                .stream()
                .map(this::mapToProductUnitResponse)
                .collect(Collectors.toList());

        return mapToProductResponse(product, units);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MapProductResponse> getAllProducts(String search, Long categoryId, Boolean isActive, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sorting);
        Page<Product> pageResult = productRepository.findAllProductsIncludeDeleted(search, categoryId, isActive, includeDeleted, pageable);
        return pageResult.getContent().stream()
                .map(this::mapToMapProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MapProductResponse> getProductsPaginated(String search, Long categoryId, Boolean isActive, int page, int size, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<Product> pageResult = productRepository.findAllProductsIncludeDeleted(search, categoryId, isActive, includeDeleted, pageable);

        List<MapProductResponse> content = pageResult.getContent().stream()
                .map(this::mapToMapProductResponse)
                .collect(Collectors.toList());

        return PageResponse.<MapProductResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Người dùng chưa đăng nhập hoặc token không hợp lệ");
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isEmpty()) {
            return Sort.by("id").descending();
        }
        String[] parts = sortParam.split(",");
        String property = parts[0];
        if ("code".equalsIgnoreCase(property)) {
            property = "productCode";
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    private ProductUnitResponse mapToProductUnitResponse(ProductUnit pu) {
        return ProductUnitResponse.builder()
                .id(pu.getId())
                .productId(pu.getProduct().getId())
                .unitId(pu.getUnit().getId())
                .unitCode(pu.getUnit().getUnitCode())
                .unitName(pu.getUnit().getUnitName())
                .conversionRate(pu.getConversionRate())
                .price(pu.getPrice())
                .barcode(pu.getBarcode())
                .isActive(pu.getIsActive())
                .isBaseUnit(pu.getIsBaseUnit())
                .createdAt(pu.getCreatedAt())
                .createdBy(pu.getCreatedBy())
                .isDeleted(pu.getIsDeleted())
                .build();
    }

    private ProductResponse mapToProductResponse(Product product, List<ProductUnitResponse> units) {
        return ProductResponse.builder()
                .id(product.getId())
                .productCode(product.getProductCode())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .costPrice(product.getCostPrice())
                .brand(product.getBrand())
                .mainImageUrl(product.getMainImageUrl())
                .barcode(product.getBarcode())
                .isActive(product.getIsActive())
                .weight(product.getWeight())
                .reorderPoint(product.getReorderPoint())
                .minStock(product.getMinStock())
                .maxStock(product.getMaxStock())
                .galleryImages(product.getGalleryImages())
                .variants(product.getVariants())
                .createdAt(product.getCreatedAt())
                .categoryId(product.getCategory().getId())
                .categoryCode(product.getCategory().getCategoryCode())
                .categoryName(product.getCategory().getCategoryName())
                .baseUnitId(product.getBaseUnit().getId())
                .baseUnitCode(product.getBaseUnit().getUnitCode())
                .baseUnitName(product.getBaseUnit().getUnitName())
                .units(units)
                .build();
    }

    private MapProductResponse mapToMapProductResponse(Product product) {
        return MapProductResponse.builder()
                .id(product.getId())
                .productCode(product.getProductCode())
                .name(product.getName())
                .basePrice(product.getBasePrice())
                .costPrice(product.getCostPrice())
                .brand(product.getBrand())
                .mainImageUrl(product.getMainImageUrl())
                .barcode(product.getBarcode())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .isDeleted(product.getIsDeleted())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getCategoryName())
                .baseUnitId(product.getBaseUnit().getId())
                .baseUnitCode(product.getBaseUnit().getUnitCode())
                .baseUnitName(product.getBaseUnit().getUnitName())
                .build();
    }
}
