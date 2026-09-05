package org.example.storemanager.modules.catalog.service.impl;

import org.example.storemanager.shared.config.LogActivity;
import org.example.storemanager.modules.common.service.CloudinaryService;
import java.util.Arrays;
import org.example.storemanager.modules.catalog.dto.request.product.CreateProductRequest;
import org.example.storemanager.modules.catalog.dto.request.product.UpdateProductRequest;
import org.example.storemanager.modules.catalog.dto.request.productunit.ProductUnitRequest;
import org.example.storemanager.modules.catalog.dto.response.product.*;
import org.example.storemanager.modules.catalog.dto.response.productunit.ProductUnitResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.catalog.entity.ProductCategory;
import org.example.storemanager.modules.catalog.entity.ProductUnit;
import org.example.storemanager.modules.catalog.entity.ProductVariant;
import org.example.storemanager.modules.catalog.entity.Unit;
import org.example.storemanager.modules.inventory.entity.InventoryBalance;
import org.example.storemanager.modules.inventory.entity.SizeInventory;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.shared.exception.DuplicateResourceException;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.catalog.repository.CategoriesRepository;
import org.example.storemanager.modules.catalog.repository.ProductRepository;
import org.example.storemanager.modules.catalog.repository.ProductUnitRepository;
import org.example.storemanager.modules.catalog.repository.UnitRepository;
import org.example.storemanager.modules.catalog.repository.ProductVariantRepository;
import org.example.storemanager.modules.inventory.repository.InventoryBalanceRepository;
import org.example.storemanager.modules.inventory.repository.SizeInventoryRepository;
import org.example.storemanager.modules.system.repository.BranchRepository;
import org.example.storemanager.modules.catalog.service.ProductService;
import org.example.storemanager.modules.catalog.service.ProductUnitService;
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

    @Autowired private BranchRepository branchRepository;
    @Autowired private ProductVariantRepository productVariantRepository;
    @Autowired private InventoryBalanceRepository inventoryBalanceRepository;
    @Autowired private org.example.storemanager.modules.catalog.service.ProductVariantService productVariantService;
    @Autowired private org.example.storemanager.modules.inventory.repository.StockLedgerRepository stockLedgerRepository;
    @Autowired private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

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

    private String generateProductCode() {
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        for (int i = 0; i < 10; i++) {
            String code = String.format("PRD-%s-%06d", dateStr, java.util.concurrent.ThreadLocalRandom.current().nextInt(1, 999999));
            if (!productRepository.existsByProductCodeAndIsDeletedFalse(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Không thể tự động sinh mã sản phẩm (productCode) duy nhất sau 10 lần thử.");
    }

    private String generateInternalEan13() {
        for (int i = 0; i < 10; i++) {
            StringBuilder sb = new StringBuilder("8938");
            for (int j = 0; j < 8; j++) {
                sb.append(java.util.concurrent.ThreadLocalRandom.current().nextInt(0, 10));
            }
            String data12 = sb.toString();
            int sum = 0;
            for (int k = 0; k < 12; k++) {
                int digit = Character.getNumericValue(data12.charAt(k));
                sum += (k % 2 == 0) ? digit : digit * 3;
            }
            int checkDigit = (10 - (sum % 10)) % 10;
            String barcode = data12 + checkDigit;

            if (!productVariantRepository.existsByBarcodeAndIsDeletedFalse(barcode)) {
                return barcode;
            }
        }
        throw new IllegalStateException("Không thể tự động sinh mã Barcode duy nhất sau 10 lần thử.");
    }

    @Override
    @LogActivity(actionType = "CREATE", entityName = "Product", entityClass = Product.class)
    public CreateProductResponse createProduct(CreateProductRequest request) {
        String productCode = request.getProductCode();
        if (productCode == null || productCode.isBlank()) {
            productCode = generateProductCode();
        } else {
            productCode = productCode.trim();
            if (productRepository.existsByProductCodeAndIsDeletedFalse(productCode)) {
                throw new DuplicateResourceException("Product", "productCode", productCode);
            }
        }

        String barcode = request.getBarcode();
        if (barcode == null || barcode.isBlank()) {
            barcode = generateInternalEan13();
        } else {
            barcode = barcode.trim();
            productUnitService.validateBarcode(barcode, null, null);
        }

        ProductCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoriesRepository.findByIdAndIsDeletedFalse(request.getCategoryId()).orElse(null);
        }
        if (category == null) {
            category = categoriesRepository.findAllForTree().stream().findFirst().orElse(null);
            if (category == null) {
                ProductCategory newCat = new ProductCategory();
                newCat.setCategoryCode("CAT-GEN");
                newCat.setCategoryName("Chung");
                newCat.setIsActive(true);
                newCat.setIsDeleted(false);
                newCat.setCreatedBy("SYSTEM");
                category = categoriesRepository.save(newCat);
            }
        }

        Unit baseUnit = null;
        if (request.getBaseUnitId() != null) {
            baseUnit = unitRepository.findByIdAndIsDeletedFalse(request.getBaseUnitId()).orElse(null);
        }
        if (baseUnit == null) {
            baseUnit = unitRepository.findAllUnitsList("", true).stream().findFirst().orElse(null);
            if (baseUnit == null) {
                Unit newUnit = new Unit();
                newUnit.setUnitCode("CAI");
                newUnit.setUnitName("Cái");
                newUnit.setIsActive(true);
                newUnit.setIsDeleted(false);
                newUnit.setCreatedBy("SYSTEM");
                baseUnit = unitRepository.save(newUnit);
            }
        }

        String username = getCurrentUsername();

        // Resolve structured variants
        List<CreateProductRequest.CreateVariantInput> variantInputs = request.getVariants();
        if ((variantInputs == null || variantInputs.isEmpty()) && request.getVariantsRaw() != null && !request.getVariantsRaw().isBlank()) {
            try {
                variantInputs = objectMapper.readValue(request.getVariantsRaw(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, CreateProductRequest.CreateVariantInput.class));
            } catch (Exception ignored) {}
        }

        boolean hasVariants = variantInputs != null && !variantInputs.isEmpty();

        Product product = Product.builder()
                .productCode(productCode)
                .name(request.getName().trim())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .costPrice(request.getCostPrice())
                .brand(request.getBrand())
                .mainImageUrl(request.getMainImageUrl())
                .barcode(barcode)
                .weight(request.getWeight())
                .reorderPoint(request.getReorderPoint())
                .minStock(request.getMinStock())
                .maxStock(request.getMaxStock())
                .galleryImages(request.getGalleryImages())
                .variantStrategy(hasVariants ? org.example.storemanager.shared.enums.catalog.VariantStrategy.ATTRIBUTE_BASED : org.example.storemanager.shared.enums.catalog.VariantStrategy.NONE)
                .category(category)
                .baseUnit(baseUnit)
                .isSerialTracked(Boolean.TRUE.equals(request.getIsSerialTracked()))
                .warrantyPeriodMonths(request.getWarrantyPeriodMonths())
                .originCountry(request.getOriginCountry())
                .dimensions(request.getDimensions())
                .allowNegativeStock(Boolean.TRUE.equals(request.getAllowNegativeStock()))
                .taxClass(request.getTaxClass())
                .build();

        product.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        product.setIsDeleted(false);
        product.setCreatedBy(username);

        Product savedProduct = productRepository.save(product);

        productUnitService.createBaseProductUnit(savedProduct, baseUnit, username);

        if (request.getConversionUnits() != null) {
            for (ProductUnitRequest uReq : request.getConversionUnits()) {
                if (uReq == null || uReq.getUnitId() == null) continue;
                if (uReq.getUnitId().equals(baseUnit.getId())) {
                    continue;
                }

                Unit conversionUnit = unitRepository.findByIdAndIsDeletedFalse(uReq.getUnitId()).orElse(null);
                if (conversionUnit == null) continue;

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

        // ----------------------------------------------------
        // ERP WMS AUTOMATIC INVENTORY INITIALIZATION & STOCK LEDGER
        // ----------------------------------------------------
        List<Branch> activeBranches = branchRepository.findByIsDeletedFalse();
        java.util.Map<Long, Branch> branchMap = activeBranches.stream()
                .collect(Collectors.toMap(Branch::getId, b -> b));

        if (hasVariants) {
            List<ProductVariant> createdVariants = new java.util.ArrayList<>();
            for (CreateProductRequest.CreateVariantInput vInput : variantInputs) {
                ProductVariant variant = productVariantService.buildVariantFromInput(savedProduct, vInput);
                productVariantService.createAttributeMappings(variant, vInput.getAttributeValueIds(), username);
                createdVariants.add(variant);
            }

            // Bulk initialize balances for all created variants across active branches (Zero N+1)
            productVariantService.bulkInitializeBalances(createdVariants, activeBranches, username);

            // Apply initial stocks per variant
            for (int i = 0; i < createdVariants.size(); i++) {
                ProductVariant variant = createdVariants.get(i);
                CreateProductRequest.CreateVariantInput vInput = variantInputs.get(i);

                if (vInput.getInitialStocks() != null && !vInput.getInitialStocks().isEmpty()) {
                    applyInitialStockEntries(savedProduct, variant, vInput.getInitialStocks(), branchMap, username);
                }
            }
        } else {
            // Strategy NONE: Luôn đảm bảo có Default Variant và khởi tạo balances ở tất cả chi nhánh
            ProductVariant defVariant = productVariantService.ensureDefaultVariant(savedProduct, username);
            productVariantService.bulkInitializeBalances(List.of(defVariant), activeBranches, username);
            if (request.getInitialStocks() != null && !request.getInitialStocks().isEmpty()) {
                applyInitialStockEntries(savedProduct, defVariant, request.getInitialStocks(), branchMap, username);
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

    private void applyInitialStockEntries(Product product, ProductVariant variant,
                                           List<CreateProductRequest.InitialStockInput> initialStocks,
                                           java.util.Map<Long, Branch> branchMap,
                                           String username) {
        if (initialStocks == null || initialStocks.isEmpty()) return;

        for (CreateProductRequest.InitialStockInput stockInput : initialStocks) {
            if (stockInput.getQuantity() == null || stockInput.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            Branch branch = branchMap.get(stockInput.getBranchId());
            if (branch == null) {
                throw new ResourceNotFoundException("Branch", "id", stockInput.getBranchId());
            }

            InventoryBalance balance = inventoryBalanceRepository
                    .findByProductVariantIdAndBranchId(variant.getId(), branch.getId())
                    .orElseGet(() -> {
                        InventoryBalance newBal = InventoryBalance.builder()
                                .productVariant(variant)
                                .branch(branch)
                                .availableQuantity(BigDecimal.ZERO)
                                .reservedQuantity(BigDecimal.ZERO)
                                .damagedQuantity(BigDecimal.ZERO)
                                .minimumQuantity(BigDecimal.ZERO)
                                .reorderPoint(BigDecimal.ZERO)
                                .lastUpdated(LocalDateTime.now())
                                .build();
                        newBal.setIsDeleted(false);
                        newBal.setCreatedBy(username);
                        return newBal;
                    });

            balance.setAvailableQuantity(balance.getAvailableQuantity().add(stockInput.getQuantity()));
            balance.setLastUpdated(LocalDateTime.now());
            balance.setUpdatedBy(username);
            inventoryBalanceRepository.save(balance);

            org.example.storemanager.modules.inventory.entity.StockLedger ledger =
                    org.example.storemanager.modules.inventory.entity.StockLedger.builder()
                            .transactionType("OPENING_BALANCE")
                            .product(product)
                            .productVariant(variant)
                            .branch(branch)
                            .changeQty(stockInput.getQuantity())
                            .balanceAfter(balance.getAvailableQuantity())
                            .build();
            ledger.setIsDeleted(false);
            ledger.setCreatedBy(username);
            stockLedgerRepository.save(ledger);
        }
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
            eventPublisher.publishEvent(new org.example.storemanager.shared.event.CloudinaryDeleteEvent(this, oldMainImage));
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
                eventPublisher.publishEvent(new org.example.storemanager.shared.event.CloudinaryDeleteEvent(this, urlsToDelete));
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
        if (request.getIsSerialTracked() != null) {
            product.setIsSerialTracked(request.getIsSerialTracked());
        }
        if (request.getWarrantyPeriodMonths() != null) {
            product.setWarrantyPeriodMonths(request.getWarrantyPeriodMonths());
        }
        if (request.getOriginCountry() != null) {
            product.setOriginCountry(request.getOriginCountry());
        }
        if (request.getDimensions() != null) {
            product.setDimensions(request.getDimensions());
        }
        if (request.getAllowNegativeStock() != null) {
            product.setAllowNegativeStock(request.getAllowNegativeStock());
        }

        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }
        product.setTaxClass(request.getTaxClass());
        product.setUpdatedBy(getCurrentUsername());

        Product updatedProduct = productRepository.save(product);

        // Đồng bộ thông tin sang ProductVariant mặc định (nếu có)
        List<ProductVariant> existingVariants = productVariantRepository.findByProductIdAndIsDeletedFalse(id);
        if (existingVariants.isEmpty() && updatedProduct.getVariantStrategy() != org.example.storemanager.shared.enums.catalog.VariantStrategy.ATTRIBUTE_BASED) {
            ProductVariant defVariant = productVariantService.ensureDefaultVariant(updatedProduct, getCurrentUsername());
            List<Branch> activeBranches = branchRepository.findByIsDeletedFalse();
            productVariantService.bulkInitializeBalances(List.of(defVariant), activeBranches, getCurrentUsername());
        } else {
            for (ProductVariant v : existingVariants) {
                if (v.getSku() != null && (v.getSku().endsWith("-DEF") || v.getSku().endsWith("-DEFAULT"))) {
                    v.setPrice(updatedProduct.getBasePrice());
                    if (updatedProduct.getBarcode() != null && !updatedProduct.getBarcode().isBlank()) {
                        v.setBarcode(updatedProduct.getBarcode());
                    }
                    v.setImageUrl(updatedProduct.getMainImageUrl());
                    v.setUpdatedBy(getCurrentUsername());
                    productVariantRepository.save(v);
                }
            }
        }

        productUnitService.syncBaseProductUnit(updatedProduct, getCurrentUsername());

        UpdateProductResponse resp = new UpdateProductResponse();
        resp.setId(updatedProduct.getId());
        resp.setProductCode(updatedProduct.getProductCode());
        resp.setName(updatedProduct.getName());
        resp.setCategoryId(updatedProduct.getCategory() != null ? updatedProduct.getCategory().getId() : null);
        resp.setBaseUnitId(updatedProduct.getBaseUnit() != null ? updatedProduct.getBaseUnit().getId() : null);
        resp.setBasePrice(updatedProduct.getBasePrice());
        resp.setCostPrice(updatedProduct.getCostPrice());
        resp.setMainImageUrl(updatedProduct.getMainImageUrl());
        resp.setGalleryImages(updatedProduct.getGalleryImages());
        resp.setIsActive(updatedProduct.getIsActive());
        resp.setUpdatedAt(updatedProduct.getUpdatedAt());
        resp.setUpdatedBy(updatedProduct.getUpdatedBy());
        return resp;
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

        List<ProductVariant> variants = productVariantRepository.findByProductIdAndIsDeletedFalse(id);
        for (ProductVariant v : variants) {
            List<InventoryBalance> balances = inventoryBalanceRepository.findByProductVariantIdAndIsDeletedFalse(v.getId());
            for (InventoryBalance b : balances) {
                if (b.getAvailableQuantity() != null && b.getAvailableQuantity().compareTo(BigDecimal.ZERO) > 0) {
                    totalStock = totalStock.add(b.getAvailableQuantity());
                }
            }
        }

        if (totalStock.compareTo(BigDecimal.ZERO) > 0) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Không thể xóa sản phẩm '" + product.getProductCode() + "' vì vẫn còn tồn kho thực tế (" + totalStock + ")."
            );
        }

        // --- XÓA TOÀN BỘ ẢNH TRÊN CLOUDINARY KHI SẢN PHẨM BỊ XÓA ---
        if (product.getMainImageUrl() != null) {
            eventPublisher.publishEvent(new org.example.storemanager.shared.event.CloudinaryDeleteEvent(this, product.getMainImageUrl()));
        }
        if (product.getGalleryImages() != null && !product.getGalleryImages().trim().isEmpty()) {
            List<String> galleryUrls = new java.util.ArrayList<>();
            for (String url : product.getGalleryImages().split(",")) {
                galleryUrls.add(url.trim());
            }
            if (!galleryUrls.isEmpty()) {
                eventPublisher.publishEvent(new org.example.storemanager.shared.event.CloudinaryDeleteEvent(this, galleryUrls));
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

        // Soft delete các variants liên quan
        for (ProductVariant v : variants) {
            v.setIsDeleted(true);
            v.setIsActive(false);
            v.setDeletedAt(LocalDateTime.now());
            v.setDeletedBy(username);
            productVariantRepository.save(v);
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
        UpdateProductResponse resp = new UpdateProductResponse();
        resp.setId(updated.getId());
        resp.setProductCode(updated.getProductCode());
        resp.setName(updated.getName());
        resp.setCategoryId(updated.getCategory() != null ? updated.getCategory().getId() : null);
        resp.setBaseUnitId(updated.getBaseUnit() != null ? updated.getBaseUnit().getId() : null);
        resp.setBasePrice(updated.getBasePrice());
        resp.setCostPrice(updated.getCostPrice());
        resp.setMainImageUrl(updated.getMainImageUrl());
        resp.setGalleryImages(updated.getGalleryImages());
        resp.setIsActive(updated.getIsActive());
        resp.setUpdatedAt(updated.getUpdatedAt());
        resp.setUpdatedBy(updated.getUpdatedBy());
        return resp;
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
        List<Product> products = productRepository.findAllProductsList(search, categoryId, isActive, includeDeleted, sorting);
        
        List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
        List<Object[]> stockSummaries = productIds.isEmpty() ? java.util.Collections.emptyList() :
                sizeInventoryRepository.sumOnHandByProductIds(productIds);
        java.util.Map<Long, java.math.BigDecimal> stockMap = stockSummaries.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (java.math.BigDecimal) row[1],
                        (v1, v2) -> v1
                ));

        return products.stream()
                .map(p -> mapToMapProductResponse(p, stockMap.getOrDefault(p.getId(), java.math.BigDecimal.ZERO)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MapProductResponse> getProductsPaginated(String search, Long categoryId, Boolean isActive, int page, int size, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<Product> pageResult = productRepository.findAllProductsIncludeDeleted(search, categoryId, isActive, includeDeleted, pageable);

        List<Product> products = pageResult.getContent();
        List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
        List<Object[]> stockSummaries = productIds.isEmpty() ? java.util.Collections.emptyList() :
                sizeInventoryRepository.sumOnHandByProductIds(productIds);
        java.util.Map<Long, java.math.BigDecimal> stockMap = stockSummaries.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (java.math.BigDecimal) row[1],
                        (v1, v2) -> v1
                ));

        List<MapProductResponse> content = products.stream()
                .map(p -> mapToMapProductResponse(p, stockMap.getOrDefault(p.getId(), java.math.BigDecimal.ZERO)))
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
            return Sort.by(Sort.Direction.DESC, "updatedAt", "id");
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
        BigDecimal onHand = sizeInventoryRepository.sumOnHandByProductId(product.getId());
        List<org.example.storemanager.modules.catalog.dto.response.variant.VariantResponse> variantList = java.util.Collections.emptyList();
        try {
            if (productVariantService != null) {
                variantList = productVariantService.getByProductId(product.getId());
            }
        } catch (Exception ignored) {}

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
                .dimensions(product.getDimensions())
                .allowNegativeStock(product.getAllowNegativeStock())
                .galleryImages(product.getGalleryImages())
                .variants(product.getVariants())
                .createdAt(product.getCreatedAt())
                .createdBy(product.getCreatedBy())
                .updatedBy(product.getUpdatedBy())
                .updatedAt(product.getUpdatedAt())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryCode(product.getCategory() != null ? product.getCategory().getCategoryCode() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)
                .baseUnitId(product.getBaseUnit() != null ? product.getBaseUnit().getId() : null)
                .baseUnitCode(product.getBaseUnit() != null ? product.getBaseUnit().getUnitCode() : null)
                .baseUnitName(product.getBaseUnit() != null ? product.getBaseUnit().getUnitName() : null)
                .units(units)
                .variantList(variantList)
                .onHand(onHand != null ? onHand : BigDecimal.ZERO)
                .taxClass(product.getEffectiveTaxClass())
                .vatRate(product.getEffectiveVatRate())
                .build();
    }

    private MapProductResponse mapToMapProductResponse(Product product, java.math.BigDecimal onHand) {
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
                .createdBy(product.getCreatedBy())
                .updatedBy(product.getUpdatedBy())
                .updatedAt(product.getUpdatedAt())
                .isDeleted(product.getIsDeleted())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)
                .baseUnitId(product.getBaseUnit() != null ? product.getBaseUnit().getId() : null)
                .baseUnitCode(product.getBaseUnit() != null ? product.getBaseUnit().getUnitCode() : null)
                .baseUnitName(product.getBaseUnit() != null ? product.getBaseUnit().getUnitName() : null)
                .onHand(onHand != null ? onHand : java.math.BigDecimal.ZERO)
                .taxClass(product.getEffectiveTaxClass())
                .vatRate(product.getEffectiveVatRate())
                .build();
    }

    @Override
    public BulkProductImportResponse bulkCreateProducts(List<CreateProductRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return BulkProductImportResponse.builder()
                    .totalSubmitted(0)
                    .successCount(0)
                    .failedCount(0)
                    .createdProductIds(Collections.emptyList())
                    .errors(Collections.emptyList())
                    .build();
        }

        List<Long> createdIds = new java.util.ArrayList<>();
        List<BulkProductImportResponse.BulkImportError> errors = new java.util.ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            CreateProductRequest req = requests.get(i);
            try {
                CreateProductResponse response = createProduct(req);
                createdIds.add(response.getId());
            } catch (Exception ex) {
                String errMsg = ex.getMessage();
                if (ex instanceof org.springframework.web.server.ResponseStatusException rse) {
                    errMsg = rse.getReason() != null ? rse.getReason() : rse.getMessage();
                }
                errors.add(BulkProductImportResponse.BulkImportError.builder()
                        .rowIndex(i + 1)
                        .productCode(req.getProductCode())
                        .productName(req.getName())
                        .errorMessage(errMsg != null ? errMsg : "Lỗi không xác định khi tạo sản phẩm")
                        .build());
            }
        }

        return BulkProductImportResponse.builder()
                .totalSubmitted(requests.size())
                .successCount(createdIds.size())
                .failedCount(errors.size())
                .createdProductIds(createdIds)
                .errors(errors)
                .build();
    }
}

