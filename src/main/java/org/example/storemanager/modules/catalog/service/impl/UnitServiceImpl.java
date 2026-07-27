package org.example.storemanager.modules.catalog.service.impl;

import org.example.storemanager.modules.catalog.dto.request.unit.CreateUnitRequest;
import org.example.storemanager.modules.catalog.dto.request.unit.UpdateUnitRequest;
import org.example.storemanager.modules.catalog.dto.response.unit.CreateUnitResponse;
import org.example.storemanager.modules.catalog.dto.response.unit.DeleteUnitResponse;
import org.example.storemanager.modules.catalog.dto.response.unit.UpdateUnitResponse;
import org.example.storemanager.modules.catalog.dto.response.unit.UnitResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.catalog.entity.Unit;
import org.example.storemanager.shared.exception.DuplicateResourceException;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.catalog.repository.UnitRepository;
import org.example.storemanager.modules.catalog.service.UnitService;
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
public class UnitServiceImpl implements UnitService {

    private final UnitRepository unitRepository;

    @Autowired
    public UnitServiceImpl(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    @Override
    public CreateUnitResponse createUnit(CreateUnitRequest request) {
        if (unitRepository.existsByUnitCodeAndIsDeletedFalse(request.getUnitCode())) {
            throw new DuplicateResourceException("Unit", "unitCode", request.getUnitCode());
        }

        Unit unit = Unit.builder()
                .unitCode(request.getUnitCode())
                .unitName(request.getUnitName())
                .description(request.getDescription())
                .unitType(request.getUnitType())
                .conversionFactor(request.getConversionFactor())
                .baseUnitCode(request.getBaseUnitCode())
                .precisionDecimals(request.getPrecisionDecimals() != null ? request.getPrecisionDecimals() : 0)
                .build();

        unit.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        unit.setIsDeleted(false);
        unit.setCreatedBy(getCurrentUsername());

        Unit savedUnit = unitRepository.save(unit);
        return mapToCreateResponse(savedUnit);
    }

    @Override
    public UpdateUnitResponse updateUnit(Long id, UpdateUnitRequest request) {
        Unit unit = unitRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", id));

        if (unitRepository.existsByUnitCodeAndIdNotAndIsDeletedFalse(request.getUnitCode(), id)) {
            throw new DuplicateResourceException("Unit", "unitCode", request.getUnitCode());
        }

        unit.setUnitCode(request.getUnitCode());
        unit.setUnitName(request.getUnitName());
        unit.setDescription(request.getDescription());
        unit.setUnitType(request.getUnitType());
        unit.setConversionFactor(request.getConversionFactor());
        unit.setBaseUnitCode(request.getBaseUnitCode());
        if (request.getPrecisionDecimals() != null) {
            unit.setPrecisionDecimals(request.getPrecisionDecimals());
        }
        if (request.getIsActive() != null) {
            unit.setIsActive(request.getIsActive());
        }
        unit.setUpdatedBy(getCurrentUsername());

        Unit updatedUnit = unitRepository.save(unit);
        return mapToUpdateResponse(updatedUnit);
    }

    @Override
    public DeleteUnitResponse deleteUnit(Long id) {
        Unit unit = unitRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", id));

        if (Boolean.TRUE.equals(unit.getIsActive())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Không thể xóa đơn vị '" + unit.getUnitCode() + "' vì đơn vị này vẫn đang HOẠT ĐỘNG. " +
                "Vui lòng tắt hoạt động trước, sau đó mới có thể xóa."
            );
        }

        String username = getCurrentUsername();
        unit.setIsDeleted(true);
        unit.setIsActive(false);
        unit.setDeletedAt(LocalDateTime.now());
        unit.setDeletedBy(username);
        unit.setUpdatedBy(username);

        Unit deletedUnit = unitRepository.save(unit);
        return DeleteUnitResponse.builder()
                .id(deletedUnit.getId())
                .unitCode(deletedUnit.getUnitCode())
                .isDeleted(deletedUnit.getIsDeleted())
                .deletedAt(deletedUnit.getDeletedAt())
                .deletedBy(deletedUnit.getDeletedBy())
                .build();
    }

    @Override
    public UpdateUnitResponse updateStatus(Long id, Boolean isActive) {
        Unit unit = unitRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", id));

        unit.setIsActive(isActive);
        unit.setUpdatedBy(getCurrentUsername());

        Unit updatedUnit = unitRepository.save(unit);
        return mapToUpdateResponse(updatedUnit);
    }

    @Override
    @Transactional(readOnly = true)
    public UnitResponse getUnitById(Long id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", id));
        return mapToResponse(unit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitResponse> getAllUnits(String search, Boolean isActive, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sorting);
        Page<Unit> page = unitRepository.findAllUnitsIncludeDeleted(search, isActive, includeDeleted, pageable);
        return page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UnitResponse> getUnitsPaginated(String search, Boolean isActive, int page, int size, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<Unit> pageResult = unitRepository.findAllUnitsIncludeDeleted(search, isActive, includeDeleted, pageable);

        List<UnitResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<UnitResponse>builder()
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
        if ("code".equalsIgnoreCase(property) || "abbreviation".equalsIgnoreCase(property)) {
            property = "unitCode";
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    private UnitResponse mapToResponse(Unit unit) {
        return UnitResponse.builder()
                .id(unit.getId())
                .unitCode(unit.getUnitCode())
                .unitName(unit.getUnitName())
                .description(unit.getDescription())
                .isActive(unit.getIsActive())
                .abbreviation(unit.getUnitCode())
                .unitType(unit.getUnitType())
                .conversionFactor(unit.getConversionFactor())
                .baseUnitCode(unit.getBaseUnitCode())
                .precisionDecimals(unit.getPrecisionDecimals())
                .createdAt(unit.getCreatedAt())
                .createdBy(unit.getCreatedBy())
                .updatedBy(unit.getUpdatedBy())
                .updatedAt(unit.getUpdatedAt())
                // Thêm thông tin xóa mềm
                .isDeleted(unit.getIsDeleted())
                .build();
    }

    private CreateUnitResponse mapToCreateResponse(Unit unit) {
        return CreateUnitResponse.builder()
                .id(unit.getId())
                .unitCode(unit.getUnitCode())
                .unitName(unit.getUnitName())
                .description(unit.getDescription())
                .isActive(unit.getIsActive())
                .abbreviation(unit.getUnitCode())
                .unitType(unit.getUnitType())
                .conversionFactor(unit.getConversionFactor())
                .baseUnitCode(unit.getBaseUnitCode())
                .precisionDecimals(unit.getPrecisionDecimals())
                .createdAt(unit.getCreatedAt())
                .createdBy(unit.getCreatedBy())
                .build();
    }

    private UpdateUnitResponse mapToUpdateResponse(Unit unit) {
        return UpdateUnitResponse.builder()
                .id(unit.getId())
                .unitCode(unit.getUnitCode())
                .unitName(unit.getUnitName())
                .description(unit.getDescription())
                .isActive(unit.getIsActive())
                .abbreviation(unit.getUnitCode())
                .unitType(unit.getUnitType())
                .conversionFactor(unit.getConversionFactor())
                .baseUnitCode(unit.getBaseUnitCode())
                .precisionDecimals(unit.getPrecisionDecimals())
                .updatedAt(unit.getUpdatedAt())
                .updatedBy(unit.getUpdatedBy())
                .build();
    }
}
