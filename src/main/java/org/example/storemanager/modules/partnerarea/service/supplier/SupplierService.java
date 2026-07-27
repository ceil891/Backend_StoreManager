package org.example.storemanager.modules.partnerarea.service.supplier;

import org.example.storemanager.modules.partnerarea.dto.request.supplier.CreateSupplierRequest;
import org.example.storemanager.modules.partnerarea.dto.response.supplier.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupplierService {
    Page<SupplierListResponse> getAll(Boolean isActive, Pageable pageable);
    SupplierDetailResponse getById(Long id);
    CreateSupplierResponse create(CreateSupplierRequest req);
    UpdateSupplierResponse update(Long id, CreateSupplierRequest req);
    void delete(Long id);

    void updateStatus(Long id);
}