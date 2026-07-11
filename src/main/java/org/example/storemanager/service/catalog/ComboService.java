package org.example.storemanager.service.catalog;

import org.example.storemanager.dto.request.catalog.combo.ComboDeductStockRequest;
import org.example.storemanager.dto.request.catalog.combo.CreateComboRequest;
import org.example.storemanager.dto.request.catalog.combo.UpdateComboRequest;
import org.example.storemanager.dto.response.catalog.combo.ComboResponse;
import org.example.storemanager.dto.response.catalog.combo.ComboSaveResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ComboService {

    PageResponse<ComboResponse> search(String search, Boolean isActive, Pageable pageable);

    ComboResponse getById(Long id);

    ComboSaveResponse create(CreateComboRequest request);

    ComboSaveResponse update(Long id, UpdateComboRequest request);

    void delete(Long id);

    void deductDynamicComboStock(Long comboId, ComboDeductStockRequest request);
}
