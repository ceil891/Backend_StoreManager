package org.example.storemanager.service.catalog;

import org.example.storemanager.dto.request.catalog.attribute.CreateAttributeRequest;
import org.example.storemanager.dto.request.catalog.attribute.CreateAttributeValueRequest;
import org.example.storemanager.dto.request.catalog.attribute.UpdateAttributeRequest;
import org.example.storemanager.dto.request.catalog.attribute.UpdateAttributeValueRequest;
import org.example.storemanager.dto.response.catalog.attribute.AttributeResponse;
import org.example.storemanager.dto.response.catalog.attribute.AttributeValueResponse;

import java.util.List;

public interface ProductAttributeService {

    AttributeResponse createAttribute(CreateAttributeRequest request);

    AttributeResponse updateAttribute(Long id, UpdateAttributeRequest request);

    void deleteAttribute(Long id);

    AttributeResponse toggleStatus(Long id, Boolean isActive);

    AttributeResponse getById(Long id);

    List<AttributeResponse> getAll(String search, Boolean isActive, boolean includeDeleted);

    AttributeValueResponse createAttributeValue(CreateAttributeValueRequest request);

    AttributeValueResponse updateAttributeValue(Long id, UpdateAttributeValueRequest request);

    void deleteAttributeValue(Long id);

    List<AttributeValueResponse> getValuesByAttributeId(Long attributeId);
}
