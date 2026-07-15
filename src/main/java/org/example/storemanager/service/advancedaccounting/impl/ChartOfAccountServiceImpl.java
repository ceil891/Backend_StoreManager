package org.example.storemanager.service.advancedaccounting.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.advancedaccounting.CreateAccountRequest;
import org.example.storemanager.dto.response.advancedaccounting.ChartOfAccount.AccountDropdownResponse;
import org.example.storemanager.dto.response.advancedaccounting.ChartOfAccount.AccountResponse;
import org.example.storemanager.dto.response.advancedaccounting.ChartOfAccount.AccountDetailResponse;
import org.example.storemanager.entity.advancedaccounting.ChartOfAccount;
import org.example.storemanager.enums.ErrorCode;
import org.example.storemanager.exception.BusinessException;
import org.example.storemanager.exception.DuplicateResourceException;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.advancedaccounting.ChartOfAccountRepository;
import org.example.storemanager.service.advancedaccounting.ChartOfAccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ChartOfAccountServiceImpl implements ChartOfAccountService {

    private final ChartOfAccountRepository repository;

    // --- Mapper Helpers ---
    // 1. Map không có isDeleted
    private AccountResponse mapToResponse(ChartOfAccount acc) {
        return AccountResponse.builder()
                .id(acc.getId())
                .accountCode(acc.getAccountCode())
                .accountName(acc.getAccountName())
                .type(acc.getType())
                .isActive(acc.getIsActive())
                .parentId(acc.getParent() != null ? acc.getParent().getId() : null)
                .build();
    }

    // 2. Map CÓ isDeleted (Dùng cho getById và delete)
    private AccountDetailResponse mapToDetailResponse(ChartOfAccount acc) {
        return AccountDetailResponse.builder()
                .id(acc.getId())
                .accountCode(acc.getAccountCode())
                .accountName(acc.getAccountName())
                .type(acc.getType()) // Không cần .name() nữa, vì đã khớp kiểu Enum                .isActive(acc.getIsActive())
                .isDeleted(acc.getIsDeleted())
                .isActive(acc.getIsActive())
                .parentId(acc.getParent() != null ? acc.getParent().getId() : null)
                .build();
    }

    @Override
    public Page<AccountResponse> getAll(int page, int size) {
        return getAll(null, PageRequest.of(page, size));
    }

    @Override
    public Page<AccountResponse> getAll(Boolean isActive, Pageable pageable) {
        Page<ChartOfAccount> accounts = (isActive == null)
                ? repository.findAll(pageable)
                : repository.findByIsActive(isActive, pageable);
        return accounts.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDetailResponse getById(Long id) {
        ChartOfAccount account = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChartOfAccount", "id", id));
        return mapToDetailResponse(account); // Trả về Detail có isDeleted
    }

    @Override
    public AccountResponse create(CreateAccountRequest req) {
        if (repository.existsByAccountCode(req.getAccountCode())) {
            throw new DuplicateResourceException("ChartOfAccount", "accountCode", req.getAccountCode());
        }
        ChartOfAccount parent = (req.getParentId() != null)
                ? repository.findById(req.getParentId()).orElse(null) : null;

        ChartOfAccount account = ChartOfAccount.builder()
                .accountCode(req.getAccountCode())
                .accountName(req.getAccountName())
                .type(req.getType())
                .isActive(true)
                .parent(parent)
                .build();
        return mapToResponse(repository.save(account)); // Trả về Response thường
    }

    @Override
    public AccountResponse update(Long id, CreateAccountRequest req) {
        ChartOfAccount account = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChartOfAccount", "id", id));

        account.setAccountCode(req.getAccountCode());
        account.setAccountName(req.getAccountName());
        account.setType(req.getType());
        account.setIsActive(req.getIsActive() != null ? req.getIsActive() : account.getIsActive());

        return mapToResponse(repository.save(account)); // Trả về Response thường
    }

    @Override
    @Transactional
    public AccountDetailResponse delete(Long id) {
        ChartOfAccount account = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        // Yêu cầu: Nếu đang là true (Active) thì không được xóa
        if (Boolean.TRUE.equals(account.getIsActive())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Không thể xóa tài khoản đang ở trạng thái hoạt động (Active).");
        }

        // Yêu cầu: Kiểm tra con
        if (repository.existsByParentId(id)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Không thể xóa tài khoản đang có tài khoản con.");
        }

        // Xóa mềm
        account.setIsActive(false); // Đảm bảo gán false
        account.setIsDeleted(true);

        return mapToDetailResponse(repository.save(account));
    }

    @Override
    public AccountResponse toggleActive(Long id) {
        ChartOfAccount account = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        // Đảm bảo không bao giờ là null
        boolean currentActive = (account.getIsActive() != null) && account.getIsActive();
        account.setIsActive(!currentActive);

        return mapToResponse(repository.save(account));
    }

    @Override
    public List<AccountResponse> getTree() {
        return repository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<AccountDropdownResponse> getDropdown() {
        return repository.findAll().stream().map(acc -> AccountDropdownResponse.builder()
                .id(acc.getId())
                .label(acc.getAccountCode() + " - " + acc.getAccountName())
                .value(acc.getAccountCode())
                .build()).collect(Collectors.toList());
    }
}