package org.example.storemanager.service.system.impl;

import org.example.storemanager.config.LogActivity;
import org.example.storemanager.dto.request.system.branch.CreateBranchRequest;
import org.example.storemanager.dto.request.system.branch.UpdateBranchRequest;
import org.example.storemanager.dto.response.system.branch.*;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.entity.system.Branch;
import org.example.storemanager.entity.system.User;
import org.example.storemanager.exception.DuplicateResourceException;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.system.BranchRepository;
import org.example.storemanager.repository.system.UserRepository;
import org.example.storemanager.service.system.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

    @Autowired
    public BranchServiceImpl(BranchRepository branchRepository, UserRepository userRepository) {
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
    }

    @Override
    @LogActivity(actionType = "CREATE", entityName = "Branch", entityClass = Branch.class)
    public CreateBranchResponse createBranch(CreateBranchRequest request) {
        if (branchRepository.existsByBranchCodeAndIsDeletedFalse(request.getBranchCode())) {
            throw new DuplicateResourceException("Branch", "branchCode", request.getBranchCode());
        }

        User manager = null;
        if (request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getManagerId()));
        }

        Branch branch = Branch.builder()
                .branchCode(request.getBranchCode())
                .branchName(request.getBranchName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .manager(manager)
                .build();

        branch.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        branch.setIsDeleted(false);
        branch.setCreatedBy(getCurrentUsername());

        Branch saved = branchRepository.save(branch);
        return mapToCreateResponse(saved);
    }

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "Branch", entityClass = Branch.class)
    public UpdateBranchResponse updateBranch(Long id, UpdateBranchRequest request) {
        Branch branch = branchRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));

        if (branchRepository.existsByBranchCodeAndIdNotAndIsDeletedFalse(request.getBranchCode(), id)) {
            throw new DuplicateResourceException("Branch", "branchCode", request.getBranchCode());
        }

        User manager = null;
        if (request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getManagerId()));
        }

        branch.setBranchCode(request.getBranchCode());
        branch.setBranchName(request.getBranchName());
        branch.setAddress(request.getAddress());
        branch.setPhone(request.getPhone());
        branch.setManager(manager);
        if (request.getIsActive() != null) {
            branch.setIsActive(request.getIsActive());
        }
        branch.setUpdatedBy(getCurrentUsername());

        Branch updated = branchRepository.save(branch);
        return mapToUpdateResponse(updated);
    }

    @Override
    @LogActivity(actionType = "DELETE", entityName = "Branch", entityClass = Branch.class)
    public DeleteBranchResponse deleteBranch(Long id) {
        Branch branch = branchRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));

        if (Boolean.TRUE.equals(branch.getIsActive())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Không thể xóa chi nhánh '" + branch.getBranchCode() + "' vì chi nhánh này vẫn đang hoạt động. " +
                "Vui lòng tắt hoạt động trước, sau đó mới có thể xóa."
            );
        }

        String username = getCurrentUsername();
        branch.setIsDeleted(true);
        branch.setIsActive(false);
        branch.setDeletedAt(LocalDateTime.now());
        branch.setDeletedBy(username);
        branch.setUpdatedBy(username);

        Branch deleted = branchRepository.save(branch);
        return DeleteBranchResponse.builder()
                .id(deleted.getId())
                .branchCode(deleted.getBranchCode())
                .isDeleted(deleted.getIsDeleted())
                .deletedAt(deleted.getDeletedAt())
                .deletedBy(deleted.getDeletedBy())
                .build();
    }

    @Override
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "Branch", entityClass = Branch.class)
    public UpdateBranchResponse updateStatus(Long id, Boolean isActive) {
        Branch branch = branchRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));

        branch.setIsActive(isActive);
        branch.setUpdatedBy(getCurrentUsername());

        Branch updated = branchRepository.save(branch);
        return mapToUpdateResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponse getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
        return mapToResponse(branch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MapBranchResponse> getAllBranches(String search, Boolean isActive, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sorting);
        Page<Branch> page = branchRepository.findAllBranchesIncludeDeleted(search, isActive, includeDeleted, pageable);
        return page.getContent().stream()
                .map(this::mapToResponseAll)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MapBranchResponse> getBranchesPaginated(
            String search,
            Boolean isActive,
            int page,
            int size,
            String sort,
            boolean includeDeleted) {

        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Branch> pageResult =
                branchRepository.findAllBranchesIncludeDeleted(
                        search,
                        isActive,
                        includeDeleted,
                        pageable);

        List<MapBranchResponse> content = pageResult.getContent()
                .stream()
                .map(this::mapToResponseAll)
                .collect(Collectors.toList());

        return PageResponse.<MapBranchResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Người dùng chưa đăng nhập hoặc token không hợp lệ");
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isEmpty()) {
            return Sort.by("id").descending();
        }
        String[] parts = sortParam.split(",");
        String property = parts[0];
        if ("code".equalsIgnoreCase(property)) {
            property = "branchCode";
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    private BranchResponse mapToResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .branchCode(branch.getBranchCode())
                .branchName(branch.getBranchName())
                .address(branch.getAddress())
                .phone(branch.getPhone())
                .isActive(branch.getIsActive())
                .createdAt(branch.getCreatedAt())
                .manager(mapToManagerResponse(branch.getManager()))
                .build();
    }

    private MapBranchResponse mapToResponseAll(Branch branch) {
        return MapBranchResponse.builder()
                .id(branch.getId())
                .branchCode(branch.getBranchCode())
                .branchName(branch.getBranchName())
                .address(branch.getAddress())
                .phone(branch.getPhone())
                .isActive(branch.getIsActive())
                .createdAt(branch.getCreatedAt())
                .isDeleted(branch.getIsDeleted())
                .manager(mapToManagerResponse(branch.getManager()))
                .build();
    }

    private CreateBranchResponse mapToCreateResponse(Branch branch) {
        return CreateBranchResponse.builder()
                .id(branch.getId())
                .branchCode(branch.getBranchCode())
                .branchName(branch.getBranchName())
                .address(branch.getAddress())
                .phone(branch.getPhone())
                .isActive(branch.getIsActive())
                .managerId(branch.getManager() != null ? branch.getManager().getId() : null)
                .managerName(branch.getManager() != null ? branch.getManager().getFullName() : null)
                .createdAt(branch.getCreatedAt())
                .createdBy(branch.getCreatedBy())
                .build();
    }

    private UpdateBranchResponse mapToUpdateResponse(Branch branch) {
        return UpdateBranchResponse.builder()
                .id(branch.getId())
                .branchCode(branch.getBranchCode())
                .branchName(branch.getBranchName())
                .address(branch.getAddress())
                .phone(branch.getPhone())
                .isActive(branch.getIsActive())
                .managerId(branch.getManager() != null ? branch.getManager().getId() : null)
                .managerName(branch.getManager() != null ? branch.getManager().getFullName() : null)
                .updatedAt(branch.getUpdatedAt())
                .updatedBy(branch.getUpdatedBy())
                .build();
    }

    private BranchResponse.BranchManagerResponse mapToManagerResponse(User manager) {
        if (manager == null) {
            return null;
        }
        return BranchResponse.BranchManagerResponse.builder()
                .id(manager.getId())
                .username(manager.getUsername())
                .fullName(manager.getFullName())
                .build();
    }
}
