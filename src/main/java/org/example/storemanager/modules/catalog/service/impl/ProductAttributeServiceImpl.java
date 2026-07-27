package org.example.storemanager.modules.catalog.service.impl;

import org.example.storemanager.shared.config.LogActivity;
import org.example.storemanager.modules.catalog.dto.request.attribute.CreateAttributeRequest;
import org.example.storemanager.modules.catalog.dto.request.attribute.CreateAttributeValueRequest;
import org.example.storemanager.modules.catalog.dto.request.attribute.UpdateAttributeRequest;
import org.example.storemanager.modules.catalog.dto.request.attribute.UpdateAttributeValueRequest;
import org.example.storemanager.modules.catalog.dto.response.attribute.AttributeResponse;
import org.example.storemanager.modules.catalog.dto.response.attribute.AttributeValueResponse;
import org.example.storemanager.modules.catalog.entity.AttributeValue;
import org.example.storemanager.modules.catalog.entity.ProductAttribute;
import org.example.storemanager.shared.exception.DuplicateResourceException;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.catalog.repository.AttributeValueRepository;
import org.example.storemanager.modules.catalog.repository.ProductAttributeRepository;
import org.example.storemanager.modules.catalog.service.ProductAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductAttributeServiceImpl implements ProductAttributeService {

    private final ProductAttributeRepository productAttributeRepository;
    private final AttributeValueRepository attributeValueRepository;

    @Autowired
    public ProductAttributeServiceImpl(ProductAttributeRepository productAttributeRepository,
                                       AttributeValueRepository attributeValueRepository) {
        this.productAttributeRepository = productAttributeRepository;
        this.attributeValueRepository = attributeValueRepository;
    }

    // ===================== ATTRIBUTE CRUD =====================

    @Override
    @LogActivity(actionType = "CREATE", entityName = "ProductAttribute", entityClass = ProductAttribute.class)
    public AttributeResponse createAttribute(CreateAttributeRequest request) {
        if (productAttributeRepository.existsByAttributeCodeAndIsDeletedFalse(request.getAttributeCode())) {
            throw new DuplicateResourceException("ProductAttribute", "attributeCode", request.getAttributeCode());
        }

        ProductAttribute attribute = ProductAttribute.builder()
                .attributeName(request.getAttributeName())
                .attributeCode(request.getAttributeCode())
                .attributeType(request.getAttributeType())
                .build();

        attribute.setIsActive(true);
        attribute.setIsDeleted(false);
        attribute.setCreatedBy(getCurrentUsername());

        ProductAttribute saved = productAttributeRepository.save(attribute);
        return mapToAttributeResponse(saved, null);
    }

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "ProductAttribute", entityClass = ProductAttribute.class)
    public AttributeResponse updateAttribute(Long id, UpdateAttributeRequest request) {
        ProductAttribute attribute = productAttributeRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductAttribute", "id", id));

        attribute.setAttributeName(request.getAttributeName());
        attribute.setAttributeType(request.getAttributeType());
        attribute.setUpdatedBy(getCurrentUsername());

        ProductAttribute updated = productAttributeRepository.save(attribute);
        return mapToAttributeResponse(updated, null);
    }

    @Override
    @LogActivity(actionType = "DELETE", entityName = "ProductAttribute", entityClass = ProductAttribute.class)
    public void deleteAttribute(Long id) {
        ProductAttribute attribute = productAttributeRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductAttribute", "id", id));

        String username = getCurrentUsername();
        LocalDateTime now = LocalDateTime.now();

        // Soft delete all attribute values first
        List<AttributeValue> values = attributeValueRepository.findByProductAttributeIdAndIsDeletedFalse(id);
        for (AttributeValue val : values) {
            val.setIsDeleted(true);
            val.setIsActive(false);
            val.setDeletedAt(now);
            val.setDeletedBy(username);
            val.setUpdatedBy(username);
        }
        attributeValueRepository.saveAll(values);

        // Soft delete the attribute itself
        attribute.setIsDeleted(true);
        attribute.setIsActive(false);
        attribute.setDeletedAt(now);
        attribute.setDeletedBy(username);
        attribute.setUpdatedBy(username);
        productAttributeRepository.save(attribute);
    }

    @Override
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "ProductAttribute", entityClass = ProductAttribute.class)
    public AttributeResponse toggleStatus(Long id, Boolean isActive) {
        ProductAttribute attribute = productAttributeRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductAttribute", "id", id));

        attribute.setIsActive(isActive);
        attribute.setUpdatedBy(getCurrentUsername());

        ProductAttribute updated = productAttributeRepository.save(attribute);
        return mapToAttributeResponse(updated, null);
    }

    @Override
    @Transactional(readOnly = true)
    public AttributeResponse getById(Long id) {
        ProductAttribute attribute = productAttributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductAttribute", "id", id));

        List<AttributeValue> values = attributeValueRepository.findByProductAttributeIdAndIsDeletedFalse(id);
        List<AttributeValueResponse> valueResponses = values.stream()
                .map(this::mapToValueResponse)
                .collect(Collectors.toList());

        return mapToAttributeResponse(attribute, valueResponses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeResponse> getAll(String search, Boolean isActive, boolean includeDeleted) {
        List<ProductAttribute> all = includeDeleted
                ? productAttributeRepository.findAll()
                : productAttributeRepository.findAllByIsDeletedFalse();

        return all.stream()
                .filter(a -> {
                    if (isActive != null && !isActive.equals(a.getIsActive())) return false;
                    if (search != null && !search.isBlank()) {
                        String q = search.toLowerCase();
                        boolean matchName = a.getAttributeName() != null && a.getAttributeName().toLowerCase().contains(q);
                        boolean matchCode = a.getAttributeCode() != null && a.getAttributeCode().toLowerCase().contains(q);
                        return matchName || matchCode;
                    }
                    return true;
                })
                .map(a -> mapToAttributeResponse(a, null))
                .collect(Collectors.toList());
    }

    // ===================== ATTRIBUTE VALUE CRUD =====================

    @Override
    @LogActivity(actionType = "CREATE", entityName = "AttributeValue", entityClass = AttributeValue.class)
    public AttributeValueResponse createAttributeValue(CreateAttributeValueRequest request) {
        ProductAttribute attribute = productAttributeRepository.findByIdAndIsDeletedFalse(request.getAttributeId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductAttribute", "id", request.getAttributeId()));

        AttributeValue value = AttributeValue.builder()
                .productAttribute(attribute)
                .value(request.getValue())
                .build();

        value.setIsActive(true);
        value.setIsDeleted(false);
        value.setCreatedBy(getCurrentUsername());

        AttributeValue saved = attributeValueRepository.save(value);
        return mapToValueResponse(saved);
    }

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "AttributeValue", entityClass = AttributeValue.class)
    public AttributeValueResponse updateAttributeValue(Long id, UpdateAttributeValueRequest request) {
        AttributeValue value = attributeValueRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttributeValue", "id", id));

        value.setValue(request.getValue());
        value.setUpdatedBy(getCurrentUsername());

        AttributeValue updated = attributeValueRepository.save(value);
        return mapToValueResponse(updated);
    }

    @Override
    @LogActivity(actionType = "DELETE", entityName = "AttributeValue", entityClass = AttributeValue.class)
    public void deleteAttributeValue(Long id) {
        AttributeValue value = attributeValueRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttributeValue", "id", id));

        String username = getCurrentUsername();
        value.setIsDeleted(true);
        value.setIsActive(false);
        value.setDeletedAt(LocalDateTime.now());
        value.setDeletedBy(username);
        value.setUpdatedBy(username);
        attributeValueRepository.save(value);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeValueResponse> getValuesByAttributeId(Long attributeId) {
        // Verify attribute exists
        productAttributeRepository.findByIdAndIsDeletedFalse(attributeId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductAttribute", "id", attributeId));

        return attributeValueRepository.findByProductAttributeIdAndIsDeletedFalse(attributeId)
                .stream()
                .map(this::mapToValueResponse)
                .collect(Collectors.toList());
    }

    // ===================== HELPERS =====================

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Người dùng chưa đăng nhập hoặc token không hợp lệ");
    }

    private AttributeResponse mapToAttributeResponse(ProductAttribute attribute, List<AttributeValueResponse> values) {
        return AttributeResponse.builder()
                .id(attribute.getId())
                .attributeName(attribute.getAttributeName())
                .attributeCode(attribute.getAttributeCode())
                .attributeType(attribute.getAttributeType())
                .isActive(attribute.getIsActive())
                .isDeleted(attribute.getIsDeleted())
                .createdAt(attribute.getCreatedAt())
                .createdBy(attribute.getCreatedBy())
                .values(values)
                .build();
    }

    private AttributeValueResponse mapToValueResponse(AttributeValue value) {
        return AttributeValueResponse.builder()
                .id(value.getId())
                .attributeId(value.getProductAttribute().getId())
                .attributeCode(value.getProductAttribute().getAttributeCode())
                .value(value.getValue())
                .isActive(value.getIsActive())
                .createdAt(value.getCreatedAt())
                .createdBy(value.getCreatedBy())
                .build();
    }
}
