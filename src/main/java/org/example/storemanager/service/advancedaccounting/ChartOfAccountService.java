package org.example.storemanager.service.advancedaccounting;

import org.example.storemanager.dto.request.advancedaccounting.CreateAccountRequest;
import org.example.storemanager.dto.response.advancedaccounting.ChartOfAccount.AccountDropdownResponse;
import org.example.storemanager.dto.response.advancedaccounting.ChartOfAccount.AccountResponse;
import org.example.storemanager.dto.response.advancedaccounting.ChartOfAccount.AccountDetailResponse; // Import mới
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChartOfAccountService {

    Page<AccountResponse> getAll(int page, int size);
    Page<AccountResponse> getAll(Boolean isActive, Pageable pageable);

    // 2 hàm này trả về DetailResponse (Có isDeleted)
    AccountDetailResponse getById(Long id);
    AccountDetailResponse delete(Long id);

    // Các hàm còn lại trả về AccountResponse thường (Không có isDeleted)
    AccountResponse create(CreateAccountRequest req);
    AccountResponse update(Long id, CreateAccountRequest req);
    AccountResponse toggleActive(Long id);

    List<AccountResponse> getTree();
    List<AccountDropdownResponse> getDropdown();
}