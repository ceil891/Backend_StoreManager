package org.example.storemanager.service.partnerarea;

import org.example.storemanager.dto.request.partnerarea.SupplierProductRequest;
import org.example.storemanager.dto.response.partnerarea.SupplierProductResponse;
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
