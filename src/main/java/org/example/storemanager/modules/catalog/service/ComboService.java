package org.example.storemanager.modules.catalog.service;

import org.example.storemanager.modules.catalog.dto.request.combo.ComboDeductStockRequest;
import org.example.storemanager.modules.catalog.dto.request.combo.CreateComboRequest;
import org.example.storemanager.modules.catalog.dto.request.combo.UpdateComboRequest;
import org.example.storemanager.modules.catalog.dto.response.combo.ComboResponse;
import org.example.storemanager.modules.catalog.dto.response.combo.ComboSaveResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ComboService {

    PageResponse<ComboResponse> search(String search, Boolean isActive, Pageable pageable);

    ComboResponse getById(Long id);

    ComboSaveResponse create(CreateComboRequest request);

    ComboSaveResponse update(Long id, UpdateComboRequest request);

    void delete(Long id);

    void deductDynamicComboStock(Long comboId, ComboDeductStockRequest request);

    java.util.List<org.example.storemanager.modules.catalog.dto.response.combo.ComboDetailResponse> getItems(Long comboId);

    org.example.storemanager.modules.catalog.dto.response.combo.ComboDetailResponse addItem(Long comboId, org.example.storemanager.modules.catalog.dto.request.combo.ComboDetailRequest request);

    org.example.storemanager.modules.catalog.dto.response.combo.ComboDetailResponse updateItem(Long id, org.example.storemanager.modules.catalog.dto.request.combo.ComboDetailRequest request);

    void deleteItem(Long id);
}
