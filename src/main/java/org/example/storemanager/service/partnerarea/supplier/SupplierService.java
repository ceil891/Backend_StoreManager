package org.example.storemanager.service.partnerarea.supplier;

import org.example.storemanager.dto.request.partnerarea.supplier.CreateSupplierRequest;
import org.example.storemanager.dto.response.partnerarea.supplier.*;
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