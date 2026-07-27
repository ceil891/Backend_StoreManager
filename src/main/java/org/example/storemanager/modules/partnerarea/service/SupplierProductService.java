package org.example.storemanager.modules.partnerarea.service;

import org.example.storemanager.modules.partnerarea.dto.request.SupplierProductRequest;
import org.example.storemanager.modules.partnerarea.dto.response.SupplierProductResponse;
import java.util.List;

public interface SupplierProductService {
    List<SupplierProductResponse> getAll();
    List<SupplierProductResponse> getBySupplierId(Long supplierId);
    List<SupplierProductResponse> getByProductId(Long productId);
    SupplierProductResponse getById(Long id);
    SupplierProductResponse create(SupplierProductRequest request);
    SupplierProductResponse update(Long id, SupplierProductRequest request);
    void delete(Long id);
    SupplierProductResponse toggleStatus(Long id);
    SupplierProductResponse setPreferred(Long id, boolean isPreferred);
}
