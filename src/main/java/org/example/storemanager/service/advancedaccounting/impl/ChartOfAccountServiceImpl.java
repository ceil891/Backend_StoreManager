package org.example.storemanager.service.advancedaccounting.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.advancedaccounting.CreateAccountRequest;
import org.example.storemanager.dto.response.advancedaccounting.AccountResponse;
import org.example.storemanager.entity.advancedaccounting.ChartOfAccount;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.advancedaccounting.ChartOfAccountRepository;
import org.example.storemanager.service.advancedaccounting.ChartOfAccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChartOfAccountServiceImpl implements ChartOfAccountService {

    private final ChartOfAccountRepository repository;

    @Override
    public Page<org.example.storemanager.dto.response.advancedaccounting.AccountResponse> getAll(int page, int size) {
        return repository.findAll(PageRequest.of(page, size)).map(this::mapToResponse);
    }

    @Override
    public AccountResponse create(CreateAccountRequest req) {
        ChartOfAccount account = ChartOfAccount.builder()
                .accountCode(req.getAccountCode())
                .accountName(req.getAccountName())
                .type(req.getType())
                .isActive(req.getIsActive())
                .build();

        if (req.getParentId() != null) {
            ChartOfAccount parent = repository.findById(req.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("ChartOfAccount", "id", req.getParentId()));
            account.setParent(parent);
        }

        return mapToResponse(repository.save(account));
    }

    @Override
    public AccountResponse update(Long id, CreateAccountRequest req) {
        ChartOfAccount account = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChartOfAccount", "id", id));

        account.setAccountCode(req.getAccountCode());
        account.setAccountName(req.getAccountName());
        account.setType(req.getType());
        account.setIsActive(req.getIsActive());

        if (req.getParentId() != null) {
            ChartOfAccount parent = repository.findById(req.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("ChartOfAccount", "id", req.getParentId()));
            account.setParent(parent);
        } else {
            account.setParent(null);
        }

        return mapToResponse(repository.save(account));
    }

    @Override
    public void delete(Long id) {
        ChartOfAccount account = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChartOfAccount", "id", id));

        // Thực hiện xóa mềm
        account.setIsActive(false);
        repository.save(account);
    }

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
}