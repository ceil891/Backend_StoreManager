package org.example.storemanager.service.advancedaccounting;

import org.example.storemanager.dto.request.advancedaccounting.CreateAccountRequest;
import org.example.storemanager.dto.response.advancedaccounting.AccountResponse; // PHẢI LÀ DÒNG NÀY
import org.springframework.data.domain.Page;

public interface ChartOfAccountService {
    Page<AccountResponse> getAll(int page, int size);
    AccountResponse create(CreateAccountRequest req);
    AccountResponse update(Long id, CreateAccountRequest req);
    void delete(Long id);
}