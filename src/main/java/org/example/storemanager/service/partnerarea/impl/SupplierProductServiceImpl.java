package org.example.storemanager.service.partnerarea.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.partnerarea.SupplierProductRequest;
import org.example.storemanager.dto.response.partnerarea.SupplierProductResponse;
import org.example.storemanager.entity.catalog.Product;
import org.example.storemanager.entity.partnerarea.Supplier;
import org.example.storemanager.entity.partnerarea.SupplierProduct;
import org.example.storemanager.exception.BusinessException;
import org.example.storemanager.exception.DuplicateResourceException;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.catalog.ProductRepository;
import org.example.storemanager.repository.partnerarea.SupplierProductRepository;
import org.example.storemanager.repository.partnerarea.SupplierRepository;
import org.example.storemanager.service.partnerarea.SupplierProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierProductServiceImpl implements SupplierProductService {

    private final SupplierProductRepository supplierProductRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SupplierProductResponse> getAll() {
        return supplierProductRepository.findAllActive().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierProductResponse> getBySupplierId(Long supplierId) {
        return supplierProductRepository.findBySupplier_IdAndIsDeletedFalse(supplierId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierProductResponse> getByProductId(Long productId) {
        return supplierProductRepository.findByProduct_IdAndIsDeletedFalse(productId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierProductResponse getById(Long id) {
        SupplierProduct sp = supplierProductRepository.findById(id)
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("SupplierProduct", "id", id));
        return toResponse(sp);
    }

    @Override
    public SupplierProductResponse create(SupplierProductRequest request) {
        if (supplierProductRepository.existsBySupplier_IdAndProduct_Id(request.getSupplierId(), request.getProductId())) {
            throw new DuplicateResourceException("SupplierProduct", "supplierId + productId",
                    request.getSupplierId() + "+" + request.getProductId());
        }

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        // Nếu isPreferred=true, reset tất cả NCC khác của product này thành false
        if (Boolean.TRUE.equals(request.getIsPreferred())) {
            resetOtherPreferred(request.getProductId(), null);
        }

        SupplierProduct sp = SupplierProduct.builder()
                .supplier(supplier)
                .product(product)
                .supplierSku(request.getSupplierSku())
                .unitPrice(request.getUnitPrice())
                .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                .moq(request.getMoq())
                .leadTimeDays(request.getLeadTimeDays())
                .isPreferred(Boolean.TRUE.equals(request.getIsPreferred()))
                .isActive(Boolean.TRUE.equals(request.getIsActive()))
                .build();
        sp.setIsDeleted(false);

        return toResponse(supplierProductRepository.save(sp));
    }

    @Override
    public SupplierProductResponse update(Long id, SupplierProductRequest request) {
        SupplierProduct sp = supplierProductRepository.findById(id)
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("SupplierProduct", "id", id));

        if (Boolean.TRUE.equals(request.getIsPreferred()) && !Boolean.TRUE.equals(sp.getIsPreferred())) {
            resetOtherPreferred(sp.getProduct().getId(), id);
        }

        sp.setSupplierSku(request.getSupplierSku());
        sp.setUnitPrice(request.getUnitPrice());
        if (request.getCurrency() != null) sp.setCurrency(request.getCurrency());
        sp.setMoq(request.getMoq());
        sp.setLeadTimeDays(request.getLeadTimeDays());
        if (request.getIsPreferred() != null) sp.setIsPreferred(request.getIsPreferred());
        if (request.getIsActive() != null) sp.setIsActive(request.getIsActive());

        return toResponse(supplierProductRepository.save(sp));
    }

    @Override
    public void delete(Long id) {
        SupplierProduct sp = supplierProductRepository.findById(id)
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("SupplierProduct", "id", id));
        sp.setIsDeleted(true);
        supplierProductRepository.save(sp);
    }

    @Override
    public SupplierProductResponse toggleStatus(Long id) {
        SupplierProduct sp = supplierProductRepository.findById(id)
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("SupplierProduct", "id", id));
        sp.setIsActive(!Boolean.TRUE.equals(sp.getIsActive()));
        return toResponse(supplierProductRepository.save(sp));
    }

    @Override
    public SupplierProductResponse setPreferred(Long id, boolean isPreferred) {
        SupplierProduct sp = supplierProductRepository.findById(id)
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("SupplierProduct", "id", id));
        if (isPreferred) {
            resetOtherPreferred(sp.getProduct().getId(), id);
        }
        sp.setIsPreferred(isPreferred);
        return toResponse(supplierProductRepository.save(sp));
    }

    /** Reset tất cả SupplierProduct của cùng product (trừ excludeId) về isPreferred=false */
    private void resetOtherPreferred(Long productId, Long excludeId) {
        supplierProductRepository.findByProduct_IdAndIsDeletedFalse(productId).forEach(other -> {
            if (!other.getId().equals(excludeId) && Boolean.TRUE.equals(other.getIsPreferred())) {
                other.setIsPreferred(false);
                supplierProductRepository.save(other);
            }
        });
    }

    private SupplierProductResponse toResponse(SupplierProduct sp) {
        return SupplierProductResponse.builder()
                .id(sp.getId())
                .supplierSku(sp.getSupplierSku())
                .unitPrice(sp.getUnitPrice())
                .currency(sp.getCurrency())
                .moq(sp.getMoq())
                .leadTimeDays(sp.getLeadTimeDays())
                .isPreferred(sp.getIsPreferred())
                .isActive(sp.getIsActive())
                .supplierId(sp.getSupplier() != null ? sp.getSupplier().getId() : null)
                .supplierName(sp.getSupplier() != null ? sp.getSupplier().getName() : null)
                .supplierCode(sp.getSupplier() != null ? sp.getSupplier().getSupplierCode() : null)
                .productId(sp.getProduct() != null ? sp.getProduct().getId() : null)
                .productName(sp.getProduct() != null ? sp.getProduct().getName() : null)
                .productCode(sp.getProduct() != null ? sp.getProduct().getProductCode() : null)
                .mainImageUrl(sp.getProduct() != null ? sp.getProduct().getMainImageUrl() : null)
                .build();
    }
}
