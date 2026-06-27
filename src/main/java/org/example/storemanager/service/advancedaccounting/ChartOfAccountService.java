package org.example.storemanager.service.advancedaccounting;

import org.example.storemanager.dto.request.advancedaccounting.CreateAccountRequest;
import org.example.storemanager.dto.response.advancedaccounting.AccountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChartOfAccountService {

    // Hàm cũ (theo yêu cầu cũ của cậu)
    Page<AccountResponse> getAll(int page, int size);

    // Hàm mới (hỗ trợ lọc và phân trang chuyên nghiệp)
    Page<AccountResponse> getAll(Boolean isActive, Pageable pageable);

    AccountResponse create(CreateAccountRequest req);

    AccountResponse update(Long id, CreateAccountRequest req);

    void delete(Long id);
}