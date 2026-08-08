package org.example.storemanager.modules.catalog.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.shared.config.LogActivity;
import org.example.storemanager.modules.catalog.dto.request.productunit.CreateProductUnitRequest;
import org.example.storemanager.modules.catalog.dto.request.productunit.UpdateProductUnitRequest;
import org.example.storemanager.modules.catalog.dto.response.productunit.ProductUnitResponse;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.catalog.entity.ProductUnit;
import org.example.storemanager.modules.catalog.entity.Unit;
import org.example.storemanager.shared.exception.DuplicateResourceException;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.catalog.repository.ProductRepository;
import org.example.storemanager.modules.catalog.repository.ProductUnitRepository;
import org.example.storemanager.modules.catalog.repository.UnitRepository;
import org.example.storemanager.modules.catalog.service.ProductUnitService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductUnitServiceImpl implements ProductUnitService {

    private final ProductUnitRepository productUnitRepository;
    private final ProductRepository productRepository;
    private final UnitRepository unitRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductUnitResponse> getProductUnits(Long productId) {
        productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return productUnitRepository.findByProductIdAndIsDeletedFalse(productId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @LogActivity(actionType = "CREATE", entityName = "ProductUnit", entityClass = ProductUnit.class)
    public ProductUnitResponse createProductUnit(Long productId, CreateProductUnitRequest request) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (product.getBaseUnit().getId().equals(request.getUnitId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Đơn vị gốc đã được tạo tự động. Không thể thêm trùng đơn vị cơ bản.");
        }

        Unit unit = unitRepository.findByIdAndIsDeletedFalse(request.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", request.getUnitId()));

        if (productUnitRepository.existsByProductIdAndUnitIdAndIsDeletedFalse(productId, request.getUnitId())) {
            throw new DuplicateResourceException("ProductUnit", "unitId", request.getUnitId());
        }

        validateBarcode(request.getBarcode(), null, productId);

        String username = getCurrentUsername();
        ProductUnit productUnit = ProductUnit.builder()
                .product(product)
                .unit(unit)
                .conversionRate(request.getConversionRate())
                .price(request.getPrice())
                .barcode(request.getBarcode())
                .isBaseUnit(false)
                .build();
        productUnit.setIsDeleted(false);
        productUnit.setIsActive(true);
        productUnit.setCreatedBy(username);

        return mapToResponse(productUnitRepository.save(productUnit));
    }

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "ProductUnit", entityClass = ProductUnit.class)
    public ProductUnitResponse updateProductUnit(Long productId, Long id, UpdateProductUnitRequest request) {
        ProductUnit productUnit = productUnitRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductUnit", "id", id));

        if (!productUnit.getProduct().getId().equals(productId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Product Unit does not belong to this Product");
        }

        if (request.getBarcode() != null) {
            validateBarcode(request.getBarcode(), id, productId);
            productUnit.setBarcode(request.getBarcode());
        }
        if (request.getConversionRate() != null) {
            if (Boolean.TRUE.equals(productUnit.getIsBaseUnit())
                    && request.getConversionRate().compareTo(BigDecimal.ONE) != 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Không thể thay đổi tỷ lệ quy đổi của đơn vị gốc (luôn bằng 1).");
            }
            productUnit.setConversionRate(request.getConversionRate());
        }
        if (request.getPrice() != null) {
            productUnit.setPrice(request.getPrice());
        }
        if (request.getIsActive() != null) {
            productUnit.setIsActive(request.getIsActive());
        }
        productUnit.setUpdatedBy(getCurrentUsername());

        return mapToResponse(productUnitRepository.save(productUnit));
    }

    @Override
    @LogActivity(actionType = "DELETE", entityName = "ProductUnit", entityClass = ProductUnit.class)
    public void deleteProductUnit(Long productId, Long id) {
        ProductUnit productUnit = productUnitRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductUnit", "id", id));

        if (!productUnit.getProduct().getId().equals(productId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Product Unit does not belong to this Product");
        }

        if (Boolean.TRUE.equals(productUnit.getIsBaseUnit())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Không thể xóa đơn vị gốc của sản phẩm.");
        }

        String username = getCurrentUsername();
        productUnit.setIsDeleted(true);
        productUnit.setIsActive(false);
        productUnit.setDeletedAt(LocalDateTime.now());
        productUnit.setDeletedBy(username);
        productUnit.setUpdatedBy(username);
        productUnitRepository.save(productUnit);
    }

    @Override
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "ProductUnit", entityClass = ProductUnit.class)
    public ProductUnitResponse updateStatus(Long productId, Long id, Boolean isActive) {
        ProductUnit productUnit = productUnitRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductUnit", "id", id));

        if (!productUnit.getProduct().getId().equals(productId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Product Unit does not belong to this Product");
        }

        productUnit.setIsActive(isActive);
        productUnit.setUpdatedBy(getCurrentUsername());
        return mapToResponse(productUnitRepository.save(productUnit));
    }

    @Override
    public void createBaseProductUnit(Product product, Unit baseUnit, String username) {
        if (productUnitRepository.existsByProductIdAndUnitIdAndIsDeletedFalse(product.getId(), baseUnit.getId())) {
            productUnitRepository.findByProductIdAndUnitIdAndIsDeletedFalse(product.getId(), baseUnit.getId())
                    .ifPresent(existing -> {
                        existing.setIsBaseUnit(true);
                        existing.setConversionRate(BigDecimal.ONE);
                        existing.setPrice(product.getBasePrice());
                        existing.setBarcode(product.getBarcode());
                        existing.setUpdatedBy(username);
                        productUnitRepository.save(existing);
                    });
            return;
        }

        validateBarcode(product.getBarcode(), null, product.getId());

        ProductUnit baseProductUnit = ProductUnit.builder()
                .product(product)
                .unit(baseUnit)
                .conversionRate(BigDecimal.ONE)
                .price(product.getBasePrice())
                .barcode(product.getBarcode())
                .isBaseUnit(true)
                .build();
        baseProductUnit.setIsDeleted(false);
        baseProductUnit.setIsActive(true);
        baseProductUnit.setCreatedBy(username);
        productUnitRepository.save(baseProductUnit);
    }

    @Override
    public void syncBaseProductUnit(Product product, String username) {
        productUnitRepository.findByProductIdAndIsBaseUnitTrueAndIsDeletedFalse(product.getId())
                .ifPresentOrElse(baseUnit -> {
                    baseUnit.setUnit(product.getBaseUnit());
                    baseUnit.setConversionRate(BigDecimal.ONE);
                    baseUnit.setPrice(product.getBasePrice());
                    baseUnit.setBarcode(product.getBarcode());
                    baseUnit.setUpdatedBy(username);
                    productUnitRepository.save(baseUnit);
                }, () -> createBaseProductUnit(product, product.getBaseUnit(), username));
    }

    @Override
    public void validateBarcode(String barcode, Long excludeProductUnitId, Long excludeProductId) {
        if (barcode == null || barcode.isBlank()) {
            return;
        }
        if (excludeProductUnitId != null) {
            if (productUnitRepository.existsByBarcodeAndIdNotAndIsDeletedFalse(barcode, excludeProductUnitId)) {
                throw new DuplicateResourceException("ProductUnit", "barcode", barcode);
            }
        } else if (excludeProductId != null) {
            List<ProductUnit> existingUnits = productUnitRepository.findByProductIdAndIsDeletedFalse(excludeProductId);
            boolean belongsToCurrentProductUnit = existingUnits.stream().anyMatch(u -> u.getBarcode() != null && u.getBarcode().trim().equalsIgnoreCase(barcode.trim()));
            boolean belongsToCurrentProductMain = productRepository.findByIdAndIsDeletedFalse(excludeProductId)
                    .map(p -> p.getBarcode() != null && p.getBarcode().trim().equalsIgnoreCase(barcode.trim()))
                    .orElse(false);
            if (!belongsToCurrentProductUnit && !belongsToCurrentProductMain && productUnitRepository.existsByBarcodeAndIsDeletedFalse(barcode)) {
                throw new DuplicateResourceException("ProductUnit", "barcode", barcode);
            }
        } else if (productUnitRepository.existsByBarcodeAndIsDeletedFalse(barcode)) {

            throw new DuplicateResourceException("ProductUnit", "barcode", barcode);
        }

        if (excludeProductId != null) {
            if (productRepository.existsByBarcodeAndIdNotAndIsDeletedFalse(barcode, excludeProductId)) {
                throw new DuplicateResourceException("Product", "barcode", barcode);
            }
        } else if (productRepository.existsByBarcodeAndIsDeletedFalse(barcode)) {
            throw new DuplicateResourceException("Product", "barcode", barcode);
        }
    }


    private ProductUnitResponse mapToResponse(ProductUnit productUnit) {
        return ProductUnitResponse.builder()
                .id(productUnit.getId())
                .productId(productUnit.getProduct().getId())
                .unitId(productUnit.getUnit().getId())
                .unitCode(productUnit.getUnit().getUnitCode())
                .unitName(productUnit.getUnit().getUnitName())
                .conversionRate(productUnit.getConversionRate())
                .price(productUnit.getPrice())
                .barcode(productUnit.getBarcode())
                .isActive(productUnit.getIsActive())
                .isBaseUnit(productUnit.getIsBaseUnit())
                .createdAt(productUnit.getCreatedAt())
                .createdBy(productUnit.getCreatedBy())
                .isDeleted(productUnit.getIsDeleted())
                .build();
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "system";
    }
}
