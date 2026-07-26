package org.example.storemanager.service.catalog.impl;

import org.example.storemanager.config.LogActivity;
import org.example.storemanager.dto.request.catalog.color.CreateColorRequest;
import org.example.storemanager.dto.request.catalog.color.UpdateColorRequest;
import org.example.storemanager.dto.response.catalog.color.*;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.entity.catalog.Color;
import org.example.storemanager.exception.DuplicateResourceException;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.catalog.ColorRepository;
import org.example.storemanager.service.catalog.ColorService;
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
public class ColorServiceImpl implements ColorService {

    private final ColorRepository colorRepository;

    @Autowired
    public ColorServiceImpl(ColorRepository colorRepository) {
        this.colorRepository = colorRepository;
    }

    @Override
    @LogActivity(actionType = "CREATE", entityName = "Color", entityClass = Color.class)
    public CreateColorResponse createColor(CreateColorRequest request) {
        if (colorRepository.existsByColorCodeAndIsDeletedFalse(request.getColorCode())) {
            throw new DuplicateResourceException("Color", "colorCode", request.getColorCode());
        }

        Color color = Color.builder()
                .colorCode(request.getColorCode())
                .colorName(request.getColorName())
                .hexValue(request.getHexValue())
                .description(request.getDescription())
                .build();

        color.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        color.setIsDeleted(false);
        color.setCreatedBy(getCurrentUsername());

        Color saved = colorRepository.save(color);
        return mapToCreateResponse(saved);
    }

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "Color", entityClass = Color.class)
    public UpdateColorResponse updateColor(Long id, UpdateColorRequest request) {
        Color color = colorRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Color", "id", id));

        if (colorRepository.existsByColorCodeAndIdNotAndIsDeletedFalse(request.getColorCode(), id)) {
            throw new DuplicateResourceException("Color", "colorCode", request.getColorCode());
        }

        color.setColorCode(request.getColorCode());
        color.setColorName(request.getColorName());
        color.setHexValue(request.getHexValue());
        color.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            color.setIsActive(request.getIsActive());
        }
        color.setUpdatedBy(getCurrentUsername());

        Color updated = colorRepository.save(color);
        return mapToUpdateResponse(updated);
    }

    @Override
    @LogActivity(actionType = "DELETE", entityName = "Color", entityClass = Color.class)
    public DeleteColorResponse deleteColor(Long id) {
        Color color = colorRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Color", "id", id));

        if (Boolean.TRUE.equals(color.getIsActive())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Không thể xóa màu sắc '" + color.getColorCode() + "' vì màu sắc này vẫn đang hoạt động. " +
                "Vui lòng tắt hoạt động trước, sau đó mới có thể xóa."
            );
        }

        String username = getCurrentUsername();
        color.setIsDeleted(true);
        color.setIsActive(false);
        color.setDeletedAt(LocalDateTime.now());
        color.setDeletedBy(username);
        color.setUpdatedBy(username);

        Color deleted = colorRepository.save(color);
        return DeleteColorResponse.builder()
                .id(deleted.getId())
                .colorCode(deleted.getColorCode())
                .isDeleted(deleted.getIsDeleted())
                .deletedAt(deleted.getDeletedAt())
                .deletedBy(deleted.getDeletedBy())
                .build();
    }

    @Override
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "Color", entityClass = Color.class)
    public UpdateColorResponse updateStatus(Long id, Boolean isActive) {
        Color color = colorRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Color", "id", id));

        color.setIsActive(isActive);
        color.setUpdatedBy(getCurrentUsername());

        Color updated = colorRepository.save(color);
        return mapToUpdateResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ColorResponse getColorById(Long id) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Color", "id", id));
        return mapToResponse(color);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MapColorResponse> getAllColors(String search, Boolean isActive, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sorting);
        Page<Color> page = colorRepository.findAllColorsIncludeDeleted(search, isActive, includeDeleted, pageable);
        return page.getContent().stream()
                .map(this::mapToResponseAll)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MapColorResponse> getColorsPaginated(
            String search,
            Boolean isActive,
            int page,
            int size,
            String sort,
            boolean includeDeleted) {

        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Color> pageResult =
                colorRepository.findAllColorsIncludeDeleted(
                        search,
                        isActive,
                        includeDeleted,
                        pageable);

        List<MapColorResponse> content = pageResult.getContent()
                .stream()
                .map(this::mapToResponseAll)
                .collect(Collectors.toList());

        return PageResponse.<MapColorResponse>builder()
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
            property = "colorCode";
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    private ColorResponse mapToResponse(Color color) {
        return ColorResponse.builder()
                .id(color.getId())
                .colorCode(color.getColorCode())
                .colorName(color.getColorName())
                .hexValue(color.getHexValue())
                .description(color.getDescription())
                .isActive(color.getIsActive())
                .createdAt(color.getCreatedAt())
                .createdBy(color.getCreatedBy())
                .updatedBy(color.getUpdatedBy())
                .updatedAt(color.getUpdatedAt())
                .build();
    }

    private MapColorResponse mapToResponseAll(Color color) {
        return MapColorResponse.builder()
                .id(color.getId())
                .colorCode(color.getColorCode())
                .colorName(color.getColorName())
                .hexValue(color.getHexValue())
                .description(color.getDescription())
                .isActive(color.getIsActive())
                .createdAt(color.getCreatedAt())
                .createdBy(color.getCreatedBy())
                .updatedBy(color.getUpdatedBy())
                .updatedAt(color.getUpdatedAt())
                .isDeleted(color.getIsDeleted())
                .build();
    }

    private CreateColorResponse mapToCreateResponse(Color color) {
        return CreateColorResponse.builder()
                .id(color.getId())
                .colorCode(color.getColorCode())
                .colorName(color.getColorName())
                .hexValue(color.getHexValue())
                .description(color.getDescription())
                .isActive(color.getIsActive())
                .createdAt(color.getCreatedAt())
                .createdBy(color.getCreatedBy())
                .build();
    }

    private UpdateColorResponse mapToUpdateResponse(Color color) {
        return UpdateColorResponse.builder()
                .id(color.getId())
                .colorCode(color.getColorCode())
                .colorName(color.getColorName())
                .hexValue(color.getHexValue())
                .description(color.getDescription())
                .isActive(color.getIsActive())
                .updatedAt(color.getUpdatedAt())
                .updatedBy(color.getUpdatedBy())
                .build();
    }
}
