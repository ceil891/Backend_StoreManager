package org.example.storemanager.service.hrm.impl;

import org.example.storemanager.config.LogActivity;
import org.example.storemanager.dto.request.hrm.contract.*;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.hrm.contract.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.hrm.EmployeeContract;
import org.example.storemanager.entity.hrm.Position;
import org.example.storemanager.entity.system.User;
import org.example.storemanager.enums.hrm.ContractStatus;
import org.example.storemanager.exception.DuplicateResourceException;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.hrm.EmployeeContractRepository;
import org.example.storemanager.repository.hrm.PositionRepository;
import org.example.storemanager.repository.system.UserRepository;
import org.example.storemanager.service.hrm.EmployeeContractService;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeContractServiceImpl implements EmployeeContractService {

    private final EmployeeContractRepository contractRepository;
    private final UserRepository userRepository;
    private final PositionRepository positionRepository;

    @Autowired
    public EmployeeContractServiceImpl(EmployeeContractRepository contractRepository,
                                       UserRepository userRepository,
                                       PositionRepository positionRepository) {
        this.contractRepository = contractRepository;
        this.userRepository = userRepository;
        this.positionRepository = positionRepository;
    }

    @Override
    @LogActivity(actionType = "CREATE", entityName = "EmployeeContract", entityClass = EmployeeContract.class)
    public CreateEmployeeContractResponse create(CreateEmployeeContractRequest request) {
        if (contractRepository.existsByContractNumberAndIsDeletedFalse(request.getContractNumber())) {
            throw new DuplicateResourceException("EmployeeContract", "contractNumber", request.getContractNumber());
        }

        EmployeeContract contract = EmployeeContract.builder()
                .contractNumber(request.getContractNumber())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .contractType(request.getContractType())
                .status(requireEnumName(request.getStatus(), ContractStatus.class, "Trạng thái hợp đồng"))
                .user(resolveUser(request.getUserId()))
                .position(resolvePosition(request.getPositionId()))
                .salary(request.getSalary())
                .allowance(request.getAllowance())
                .socialInsuranceSalary(request.getSocialInsuranceSalary())
                .contractUrl(request.getContractUrl())
                .signingDate(request.getSigningDate())
                .workingHours(request.getWorkingHours())
                .renewalDate(request.getRenewalDate())
                .terminationDate(request.getTerminationDate())
                .terminationReason(request.getTerminationReason())
                .build();

        contract.setIsLocked(Boolean.FALSE.equals(request.getIsActive()));
        contract.setIsDeleted(false);
        contract.setCreatedBy(getCurrentUsername());

        return mapToCreateResponse(contractRepository.save(contract));
    }

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "EmployeeContract", entityClass = EmployeeContract.class)
    public UpdateEmployeeContractResponse update(Long id, UpdateEmployeeContractRequest request) {
        EmployeeContract contract = contractRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeContract", "id", id));

        // Only validate and set contractNumber if provided and changed
        if (request.getContractNumber() != null && !request.getContractNumber().isBlank() && !request.getContractNumber().equals(contract.getContractNumber())) {
            if (contractRepository.existsByContractNumberAndIdNotAndIsDeletedFalse(request.getContractNumber(), id)) {
                throw new DuplicateResourceException("EmployeeContract", "contractNumber", request.getContractNumber());
            }
            contract.setContractNumber(request.getContractNumber());
        }

        if (request.getStartDate() != null) {
            contract.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            contract.setEndDate(request.getEndDate());
        }
        if (request.getContractType() != null) {
            contract.setContractType(request.getContractType());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            contract.setStatus(requireEnumName(request.getStatus(), ContractStatus.class, "Trạng thái hợp đồng"));
        }
        if (request.getUserId() != null) {
            contract.setUser(resolveUser(request.getUserId()));
        }
        if (request.getPositionId() != null) {
            contract.setPosition(resolvePosition(request.getPositionId()));
        }

        // Optional numeric/string fields
        if (request.getSalary() != null) {
            contract.setSalary(request.getSalary());
        }
        if (request.getAllowance() != null) {
            contract.setAllowance(request.getAllowance());
        }
        if (request.getSocialInsuranceSalary() != null) {
            contract.setSocialInsuranceSalary(request.getSocialInsuranceSalary());
        }
        if (request.getContractUrl() != null) {
            contract.setContractUrl(request.getContractUrl());
        }
        if (request.getSigningDate() != null) {
            contract.setSigningDate(request.getSigningDate());
        }
        if (request.getWorkingHours() != null) {
            contract.setWorkingHours(request.getWorkingHours());
        }
        if (request.getRenewalDate() != null) {
            contract.setRenewalDate(request.getRenewalDate());
        }
        if (request.getTerminationDate() != null) {
            contract.setTerminationDate(request.getTerminationDate());
        }
        if (request.getTerminationReason() != null) {
            contract.setTerminationReason(request.getTerminationReason());
        }

        if (request.getIsActive() != null) {
            contract.setIsLocked(!request.getIsActive());
        }
        contract.setUpdatedBy(getCurrentUsername());

        return mapToUpdateResponse(contractRepository.save(contract));
    }

    @Override
    @LogActivity(actionType = "DELETE", entityName = "EmployeeContract", entityClass = EmployeeContract.class)
    public DeleteEmployeeContractResponse delete(Long id) {
        EmployeeContract contract = contractRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeContract", "id", id));

        requireInactiveBeforeDelete(contract, contract.getContractNumber());
        applySoftDelete(contract);
        EmployeeContract deleted = contractRepository.save(contract);

        return DeleteEmployeeContractResponse.builder()
                .id(deleted.getId())
                .contractNumber(deleted.getContractNumber())
                .isDeleted(deleted.getIsDeleted())
                .deletedAt(deleted.getDeletedAt())
                .deletedBy(deleted.getDeletedBy())
                .build();
    }

    @Override
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "EmployeeContract", entityClass = EmployeeContract.class)
    public UpdateEmployeeContractResponse updateStatus(Long id, Boolean isActive) {
        EmployeeContract contract = contractRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeContract", "id", id));

        contract.setIsLocked(!isActive);
        contract.setUpdatedBy(getCurrentUsername());
        return mapToUpdateResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeContractResponse getById(Long id) {
        EmployeeContract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeContract", "id", id));
        return mapToResponse(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeContractResponse> getAll(String search, Boolean isActive, Long userId, String status, String sort, boolean includeDeleted) {
        String normalizedStatus = parseOptionalEnumName(status, ContractStatus.class, "Trạng thái hợp đồng");
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, parseSort(sort, "startDate"));
        return contractRepository.findAllFiltered(search, isActive, userId, normalizedStatus, includeDeleted, pageable)
                .getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeContractResponse> getPaginated(String search, Boolean isActive, Long userId, String status, int page, int size, String sort, boolean includeDeleted) {
        String normalizedStatus = parseOptionalEnumName(status, ContractStatus.class, "Trạng thái hợp đồng");
        Pageable pageable = PageRequest.of(page, size, parseSort(sort, "startDate"));
        Page<EmployeeContract> pageResult = contractRepository.findAllFiltered(search, isActive, userId, normalizedStatus, includeDeleted, pageable);
        List<EmployeeContractResponse> content = pageResult.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());

        return PageResponse.<EmployeeContractResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    private User resolveUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private Position resolvePosition(Long positionId) {
        return positionRepository.findByIdAndIsDeletedFalse(positionId)
                .orElseThrow(() -> new ResourceNotFoundException("Position", "id", positionId));
    }

    private EmployeeContractResponse mapToResponse(EmployeeContract contract) {
        return EmployeeContractResponse.builder()
                .id(contract.getId())
                .contractNumber(contract.getContractNumber())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .contractType(contract.getContractType())
                .status(contract.getStatus())
                .userId(contract.getUser().getId())
                .userName(contract.getUser().getFullName())
                .positionId(contract.getPosition().getId())
                .positionName(contract.getPosition().getPositionName())
                .salary(contract.getSalary())
                .allowance(contract.getAllowance())
                .renewalDate(contract.getRenewalDate())
                .terminationDate(contract.getTerminationDate())
                .terminationReason(contract.getTerminationReason())
                .contractUrl(contract.getContractUrl())
                .isActive(isActive(contract.getIsLocked()))
                .isDeleted(contract.getIsDeleted())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .build();
    }

    private CreateEmployeeContractResponse mapToCreateResponse(EmployeeContract contract) {
        return CreateEmployeeContractResponse.builder()
                .id(contract.getId())
                .contractNumber(contract.getContractNumber())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .contractType(contract.getContractType())
                .status(contract.getStatus())
                .userId(contract.getUser().getId())
                .positionId(contract.getPosition().getId())
                .salary(contract.getSalary())
                .allowance(contract.getAllowance())
                .socialInsuranceSalary(contract.getSocialInsuranceSalary())
                .contractUrl(contract.getContractUrl())
                .signingDate(contract.getSigningDate())
                .workingHours(contract.getWorkingHours())
                .isActive(isActive(contract.getIsLocked()))
                .createdAt(contract.getCreatedAt())
                .createdBy(contract.getCreatedBy())
                .build();
    }

    private UpdateEmployeeContractResponse mapToUpdateResponse(EmployeeContract contract) {
        return UpdateEmployeeContractResponse.builder()
                .id(contract.getId())
                .contractNumber(contract.getContractNumber())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .contractType(contract.getContractType())
                .status(contract.getStatus())
                .userId(contract.getUser().getId())
                .positionId(contract.getPosition().getId())
                .salary(contract.getSalary())
                .allowance(contract.getAllowance())
                .socialInsuranceSalary(contract.getSocialInsuranceSalary())
                .contractUrl(contract.getContractUrl())
                .signingDate(contract.getSigningDate())
                .workingHours(contract.getWorkingHours())
                .renewalDate(contract.getRenewalDate())
                .terminationDate(contract.getTerminationDate())
                .terminationReason(contract.getTerminationReason())
                .isActive(isActive(contract.getIsLocked()))
                .updatedAt(contract.getUpdatedAt())
                .updatedBy(contract.getUpdatedBy())
                .build();
    }

    // ---- Inlined Hrm support methods ----
    private static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Người dùng chưa đăng nhập hoặc token không hợp lệ");
    }

    private static Sort parseSort(String sortParam, String defaultProperty) {
        if (sortParam == null || sortParam.isEmpty()) {
            return Sort.by(defaultProperty).descending();
        }
        String[] parts = sortParam.split(",");
        String property = parts[0];
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    private static boolean isActive(Boolean isLocked) {
        return !Boolean.TRUE.equals(isLocked);
    }

    private static void applySoftDelete(BaseEntity entity) {
        String username = getCurrentUsername();
        entity.setIsDeleted(true);
        entity.setIsLocked(true);
        entity.setDeletedAt(LocalDateTime.now());
        entity.setDeletedBy(username);
        entity.setUpdatedBy(username);
    }

    private static void requireInactiveBeforeDelete(BaseEntity entity, String label) {
        if (isActive(entity.getIsLocked())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa '" + label + "' vì bản ghi vẫn đang HOẠT ĐỘNG. Vui lòng tắt hoạt động trước."
            );
        }
    }

    private static <E extends Enum<E>> String requireEnumName(String value, Class<E> enumClass, String fieldLabel) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldLabel + " không được để trống");
        }
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldLabel + " không hợp lệ. Giá trị cho phép: " + formatAllowedEnumValues(enumClass)
            );
        }
    }

    private static <E extends Enum<E>> String parseOptionalEnumName(String value, Class<E> enumClass, String fieldLabel) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return requireEnumName(value, enumClass, fieldLabel);
    }

    private static <E extends Enum<E>> String formatAllowedEnumValues(Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    // ---- New methods for contract management ----

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeContractResponse> getByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return contractRepository.findByUserIdAndIsDeletedFalse(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeContractResponse getCurrentContract(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return contractRepository.findByUserIdAndStatusAndIsDeletedFalse(userId, ContractStatus.ACTIVE.name())
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeContract", "status", ContractStatus.ACTIVE.name()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractHistoryResponse> getContractHistory(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return contractRepository.findContractHistoryByUserId(userId)
                .stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @LogActivity(actionType = "RENEW", entityName = "EmployeeContract", entityClass = EmployeeContract.class)
    public RenewContractResponse renewContract(Long id, RenewContractRequest request) {
        EmployeeContract contract = contractRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeContract", "id", id));

        if (request.getRenewalDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày gia hạn không được để trống");
        }

        if (request.getNewEndDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày kết thúc mới không được để trống");
        }

        contract.setRenewalDate(request.getRenewalDate());
        contract.setEndDate(request.getNewEndDate());
        contract.setStatus(ContractStatus.ACTIVE.name());
        contract.setUpdatedBy(getCurrentUsername());

        EmployeeContract renewed = contractRepository.save(contract);

        return RenewContractResponse.builder()
                .id(renewed.getId())
                .contractNumber(renewed.getContractNumber())
                .startDate(renewed.getStartDate())
                .endDate(renewed.getEndDate())
                .renewalDate(renewed.getRenewalDate())
                .status(renewed.getStatus())
                .userId(renewed.getUser().getId())
                .positionId(renewed.getPosition().getId())
                .isActive(isActive(renewed.getIsLocked()))
                .updatedAt(renewed.getUpdatedAt())
                .updatedBy(renewed.getUpdatedBy())
                .build();
    }

    @Override
    @LogActivity(actionType = "TERMINATE", entityName = "EmployeeContract", entityClass = EmployeeContract.class)
    public TerminateContractResponse terminateContract(Long id, TerminateContractRequest request) {
        EmployeeContract contract = contractRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeContract", "id", id));

        if (request.getTerminationDate() == null || request.getTerminationReason() == null || request.getTerminationReason().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày chấm dứt và lý do không được để trống");
        }

        contract.setTerminationDate(request.getTerminationDate());
        contract.setTerminationReason(request.getTerminationReason());
        contract.setStatus(ContractStatus.TERMINATED.name());
        contract.setIsLocked(true);
        contract.setUpdatedBy(getCurrentUsername());

        EmployeeContract terminated = contractRepository.save(contract);

        return TerminateContractResponse.builder()
                .id(terminated.getId())
                .contractNumber(terminated.getContractNumber())
                .startDate(terminated.getStartDate())
                .endDate(terminated.getEndDate())
                .terminationDate(terminated.getTerminationDate())
                .terminationReason(terminated.getTerminationReason())
                .status(terminated.getStatus())
                .userId(terminated.getUser().getId())
                .positionId(terminated.getPosition().getId())
                .isActive(isActive(terminated.getIsLocked()))
                .updatedAt(terminated.getUpdatedAt())
                .updatedBy(terminated.getUpdatedBy())
                .build();
    }

    @Override
    @LogActivity(actionType = "UPLOAD_FILE", entityName = "EmployeeContract", entityClass = EmployeeContract.class)
    public EmployeeContractResponse uploadContractFile(Long id, UploadContractFileRequest request) {
        EmployeeContract contract = contractRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeContract", "id", id));

        if (request.getContractUrl() == null || request.getContractUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL file hợp đồng không được để trống");
        }

        contract.setContractUrl(request.getContractUrl());
        contract.setUpdatedBy(getCurrentUsername());

        contractRepository.save(contract);

        return mapToResponse(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpiringContractResponse> getExpiringContracts(int daysThreshold) {
        java.time.LocalDate thresholdDate = java.time.LocalDate.now().plusDays(daysThreshold);
        return contractRepository.findExpiringContracts(thresholdDate)
                .stream()
                .map(this::mapToExpiringResponse)
                .collect(Collectors.toList());
    }

    // ---- Helper mapping methods ----

    private ContractHistoryResponse mapToHistoryResponse(EmployeeContract contract) {
        return ContractHistoryResponse.builder()
                .id(contract.getId())
                .contractNumber(contract.getContractNumber())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .contractType(contract.getContractType())
                .status(contract.getStatus())
                .userId(contract.getUser().getId())
                .userName(contract.getUser().getFullName())
                .positionId(contract.getPosition().getId())
                .positionName(contract.getPosition().getPositionName())
                .salary(contract.getSalary())
                .allowance(contract.getAllowance())
                .terminationDate(contract.getTerminationDate())
                .terminationReason(contract.getTerminationReason())
                .renewalDate(contract.getRenewalDate())
                .contractUrl(contract.getContractUrl())
                .createdAt(contract.getCreatedAt())
                .createdBy(contract.getCreatedBy())
                .updatedAt(contract.getUpdatedAt())
                .build();
    }

    private ExpiringContractResponse mapToExpiringResponse(EmployeeContract contract) {
        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.now(),
                contract.getEndDate()
        );

        return ExpiringContractResponse.builder()
                .id(contract.getId())
                .contractNumber(contract.getContractNumber())
                .endDate(contract.getEndDate())
                .daysRemaining(daysRemaining)
                .userId(contract.getUser().getId())
                .userName(contract.getUser().getFullName())
                .positionId(contract.getPosition().getId())
                .positionName(contract.getPosition().getPositionName())
                .status(contract.getStatus())
                .contractType(contract.getContractType())
                .build();
    }
}