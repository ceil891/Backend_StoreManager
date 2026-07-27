package org.example.storemanager.modules.catalog.service.impl;

import org.example.storemanager.shared.config.LogActivity;
import org.example.storemanager.modules.catalog.dto.request.size.CreateSizeRequest;
import org.example.storemanager.modules.catalog.dto.request.size.UpdateSizeRequest;
import org.example.storemanager.modules.catalog.dto.response.size.*;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.catalog.entity.Size;
import org.example.storemanager.shared.exception.DuplicateResourceException;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.catalog.repository.SizeRepository;
import org.example.storemanager.modules.catalog.service.SizeService;
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
public class SizeServiceImpl implements SizeService {

    private final SizeRepository sizeRepository;

    @Autowired
    public SizeServiceImpl(SizeRepository sizeRepository) {
        this.sizeRepository = sizeRepository;
    }

    @Override
    @LogActivity(actionType = "CREATE", entityName = "Size", entityClass = Size.class)
    public CreateSizeResponse createSize(CreateSizeRequest request) {
        if (sizeRepository.existsBySizeCodeAndIsDeletedFalse(request.getSizeCode())) {
            throw new DuplicateResourceException("Size", "sizeCode", request.getSizeCode());
        }

        Size size = Size.builder()
                .sizeCode(request.getSizeCode())
                .sizeName(request.getSizeName())
                .description(request.getDescription())
                .build();

        size.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        size.setIsDeleted(false);
        size.setCreatedBy(getCurrentUsername());

        Size saved = sizeRepository.save(size);
        return mapToCreateResponse(saved);
    }

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "Size", entityClass = Size.class)
    public UpdateSizeResponse updateSize(Long id, UpdateSizeRequest request) {
        Size size = sizeRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Size", "id", id));

        if (sizeRepository.existsBySizeCodeAndIdNotAndIsDeletedFalse(request.getSizeCode(), id)) {
            throw new DuplicateResourceException("Size", "sizeCode", request.getSizeCode());
        }

        size.setSizeCode(request.getSizeCode());
        size.setSizeName(request.getSizeName());
        size.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            size.setIsActive(request.getIsActive());
        }
        size.setUpdatedBy(getCurrentUsername());

        Size updated = sizeRepository.save(size);
        return mapToUpdateResponse(updated);
    }

    @Override
    @LogActivity(actionType = "DELETE", entityName = "Size", entityClass = Size.class)
    public DeleteSizeResponse deleteSize(Long id) {
        Size size = sizeRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Size", "id", id));

        if (Boolean.TRUE.equals(size.getIsActive())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Không thể xóa kích thước '" + size.getSizeCode() + "' vì kích thước này vẫn đang hoạt động. " +
                "Vui lòng tắt hoạt động trước, sau đó mới có thể xóa."
            );
        }

        String username = getCurrentUsername();
        size.setIsDeleted(true);
        size.setIsActive(false);
        size.setDeletedAt(LocalDateTime.now());
        size.setDeletedBy(username);
        size.setUpdatedBy(username);

        Size deleted = sizeRepository.save(size);
        return DeleteSizeResponse.builder()
                .id(deleted.getId())
                .sizeCode(deleted.getSizeCode())
                .isDeleted(deleted.getIsDeleted())
                .deletedAt(deleted.getDeletedAt())
                .deletedBy(deleted.getDeletedBy())
                .build();
    }

    @Override
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "Size", entityClass = Size.class)
    public UpdateSizeResponse updateStatus(Long id, Boolean isActive) {
        Size size = sizeRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Size", "id", id));

        size.setIsActive(isActive);
        size.setUpdatedBy(getCurrentUsername());

        Size updated = sizeRepository.save(size);
        return mapToUpdateResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public SizeResponse getSizeById(Long id) {
        Size size = sizeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Size", "id", id));
        return mapToResponse(size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MapSizeResponse> getAllSizes(String search, Boolean isActive, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sorting);
        Page<Size> page = sizeRepository.findAllSizesIncludeDeleted(search, isActive, includeDeleted, pageable);
        return page.getContent().stream()
                .map(this::mapToResponseAll)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MapSizeResponse> getSizesPaginated(
            String search,
            Boolean isActive,
            int page,
            int size,
            String sort,
            boolean includeDeleted) {

        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Size> pageResult =
                sizeRepository.findAllSizesIncludeDeleted(
                        search,
                        isActive,
                        includeDeleted,
                        pageable);

        List<MapSizeResponse> content = pageResult.getContent()
                .stream()
                .map(this::mapToResponseAll)
                .collect(Collectors.toList());

        return PageResponse.<MapSizeResponse>builder()
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
            property = "sizeCode";
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    private SizeResponse mapToResponse(Size size) {
        return SizeResponse.builder()
                .id(size.getId())
                .sizeCode(size.getSizeCode())
                .sizeName(size.getSizeName())
                .description(size.getDescription())
                .isActive(size.getIsActive())
                .createdAt(size.getCreatedAt())
                .createdBy(size.getCreatedBy())
                .updatedBy(size.getUpdatedBy())
                .updatedAt(size.getUpdatedAt())
                .build();
    }

    private MapSizeResponse mapToResponseAll(Size size) {
        return MapSizeResponse.builder()
                .id(size.getId())
                .sizeCode(size.getSizeCode())
                .sizeName(size.getSizeName())
                .description(size.getDescription())
                .isActive(size.getIsActive())
                .createdAt(size.getCreatedAt())
                .createdBy(size.getCreatedBy())
                .updatedBy(size.getUpdatedBy())
                .updatedAt(size.getUpdatedAt())
                .isDeleted(size.getIsDeleted())
                .build();
    }

    private CreateSizeResponse mapToCreateResponse(Size size) {
        return CreateSizeResponse.builder()
                .id(size.getId())
                .sizeCode(size.getSizeCode())
                .sizeName(size.getSizeName())
                .description(size.getDescription())
                .isActive(size.getIsActive())
                .createdAt(size.getCreatedAt())
                .createdBy(size.getCreatedBy())
                .build();
    }

    private UpdateSizeResponse mapToUpdateResponse(Size size) {
        return UpdateSizeResponse.builder()
                .id(size.getId())
                .sizeCode(size.getSizeCode())
                .sizeName(size.getSizeName())
                .description(size.getDescription())
                .isActive(size.getIsActive())
                .updatedAt(size.getUpdatedAt())
                .updatedBy(size.getUpdatedBy())
                .build();
    }
}
