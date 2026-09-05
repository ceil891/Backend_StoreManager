package org.example.storemanager.modules.catalog.service.impl;

import org.example.storemanager.shared.config.LogActivity;
import org.example.storemanager.modules.catalog.dto.request.categories.CreateCategoriesRequest;
import org.example.storemanager.modules.catalog.dto.request.categories.UpdateCategoriesRequest;
import org.example.storemanager.modules.catalog.dto.response.categories.*;
import org.example.storemanager.modules.catalog.dto.response.department.DepartmentResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.common.service.CloudinaryService;
import org.example.storemanager.modules.catalog.entity.Department;
import org.example.storemanager.modules.catalog.entity.ProductCategory;
import org.example.storemanager.shared.exception.DuplicateResourceException;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.catalog.repository.CategoriesRepository;
import org.example.storemanager.modules.catalog.repository.DepartmentRepository;
import org.example.storemanager.modules.catalog.service.CategoriesService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CategoriesServiceImpl implements CategoriesService {

    private final CategoriesRepository categoriesRepository;
    private final DepartmentRepository departmentRepository;
    private final CloudinaryService cloudinaryService;
    private final org.example.storemanager.modules.catalog.repository.ProductRepository productRepository;

    @Autowired
    public CategoriesServiceImpl(CategoriesRepository categoriesRepository, DepartmentRepository departmentRepository, CloudinaryService cloudinaryService, org.example.storemanager.modules.catalog.repository.ProductRepository productRepository) {
        this.categoriesRepository = categoriesRepository;
        this.departmentRepository = departmentRepository;
        this.cloudinaryService = cloudinaryService;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    @LogActivity(actionType = "CREATE", entityName = "ProductCategory", entityClass = ProductCategory.class)
    public CreateCategoriesResponse create(CreateCategoriesRequest request) {
        if (categoriesRepository.existsByCategoryCodeAndIsDeletedFalse(request.getCategoryCode())) {
            throw new DuplicateResourceException("Danh mục", "categoryCode", request.getCategoryCode());
        }

        ProductCategory parent = null;
        if (request.getParentId() != null) {
            parent = categoriesRepository.findByIdAndIsDeletedFalse(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục cha", "id", request.getParentId()));
        }

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ngành hàng", "id", request.getDepartmentId()));
        }

        ProductCategory category = new ProductCategory();
        category.setCategoryCode(request.getCategoryCode());
        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        category.setIsDeleted(false);
        category.setDepartment(department);
        category.setParent(parent);
        category.setImageUrl(request.getImageUrl());
        category.setManager(request.getManager());
        category.setInventoryGlCode(request.getInventoryGlCode());
        category.setCogsGlCode(request.getCogsGlCode());
        category.setTaxClass(request.getTaxClass());
        category.setCreatedBy(getCurrentUsername());

        ProductCategory saved = categoriesRepository.save(category);
        return mapToCreateResponse(saved);
    }

    @Override
    @Transactional
    @LogActivity(actionType = "UPDATE", entityName = "ProductCategory", entityClass = ProductCategory.class)
    public UpdateCategoriesResponse update(Long id, UpdateCategoriesRequest request) {
        ProductCategory category = categoriesRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", id));

        if (categoriesRepository.existsByCategoryCodeAndIdNotAndIsDeletedFalse(request.getCategoryCode(), id)) {
            throw new DuplicateResourceException("Danh mục", "categoryCode", request.getCategoryCode());
        }

        ProductCategory parent = null;
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh mục không thể là cha của chính nó");
            }
            parent = categoriesRepository.findByIdAndIsDeletedFalse(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục cha", "id", request.getParentId()));
        }

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ngành hàng", "id", request.getDepartmentId()));
        }

        // --- DỌN DẸP ẢNH DANH MỤC CŨ NẾU CÓ THAY ĐỔI ---
        String oldImage = category.getImageUrl();
        String newImage = request.getImageUrl();
        if (oldImage != null && !oldImage.equals(newImage)) {
            cloudinaryService.deleteFileByUrl(oldImage);
        }
        // ----------------------------------------------

        category.setCategoryCode(request.getCategoryCode());
        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }
        category.setDepartment(department);
        category.setParent(parent);
        category.setImageUrl(request.getImageUrl());
        category.setManager(request.getManager());
        category.setInventoryGlCode(request.getInventoryGlCode());
        category.setCogsGlCode(request.getCogsGlCode());
        category.setTaxClass(request.getTaxClass());
        category.setUpdatedBy(getCurrentUsername());

        ProductCategory updated = categoriesRepository.save(category);
        return mapToUpdateResponse(updated);
    }

    @Override
    @Transactional
    @LogActivity(actionType = "DELETE", entityName = "ProductCategory", entityClass = ProductCategory.class)
    public DeleteCategoriesResponse softDelete(Long id) {
        ProductCategory category = categoriesRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", id));

        if (Boolean.TRUE.equals(category.getIsActive())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Không thể xóa danh mục '" + category.getCategoryCode() + "' vì danh mục này vẫn đang hoạt động. " +
                "Vui lòng tắt hoạt động trước, sau đó mới có thể xóa."
            );
        }

        if (categoriesRepository.existsByParentIdAndIsDeletedFalse(id)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Không thể xóa danh mục '" + category.getCategoryCode() + "' vì đang có danh mục con trực thuộc."
            );
        }

        if (productRepository.existsByCategoryIdAndIsDeletedFalse(id)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Không thể xóa danh mục '" + category.getCategoryCode() + "' vì vẫn còn sản phẩm trực thuộc."
            );
        }

        // --- XÓA ẢNH TRÊN CLOUDINARY KHI XÓA DANH MỤC ---
        if (category.getImageUrl() != null) {
            cloudinaryService.deleteFileByUrl(category.getImageUrl());
        }
        // ------------------------------------------------

        String username = getCurrentUsername();
        category.setIsDeleted(true);
        category.setIsActive(false);
        category.setDeletedAt(LocalDateTime.now());
        category.setDeletedBy(username);
        category.setUpdatedBy(username);

        ProductCategory deleted = categoriesRepository.save(category);
        return DeleteCategoriesResponse.builder()
                .id(deleted.getId())
                .categoryCode(deleted.getCategoryCode())
                .isDeleted(deleted.getIsDeleted())
                .deletedAt(deleted.getDeletedAt())
                .deletedBy(deleted.getDeletedBy())
                .build();
    }

    @Override
    @Transactional
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "ProductCategory", entityClass = ProductCategory.class)
    public UpdateCategoriesResponse toggleStatus(Long id, Boolean isActive) {
        ProductCategory category = categoriesRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", id));

        category.setIsActive(isActive);
        category.setUpdatedBy(getCurrentUsername());

        ProductCategory updated = categoriesRepository.save(category);
        return mapToUpdateResponse(updated);
    }

    @Override
    @Transactional
    @LogActivity(actionType = "RESTORE", entityName = "ProductCategory", entityClass = ProductCategory.class)
    public UpdateCategoriesResponse restore(Long id) {
        ProductCategory category = categoriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", id));

        category.setIsDeleted(false);
        category.setDeletedAt(null);
        category.setDeletedBy(null);
        category.setUpdatedBy(getCurrentUsername());

        ProductCategory updated = categoriesRepository.save(category);
        return mapToUpdateResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriesResponse getById(Long id) {
        ProductCategory category = categoriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", id));
        return mapToResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MapCategoriesResponse> getAllCategories(String search, Boolean isActive, String sort, boolean includeDeleted) {
        if (search != null && search.trim().isEmpty()) {
            search = null;
        }
        Sort sorting = parseSort(sort);
        List<ProductCategory> list;
        if (search == null && !includeDeleted && (isActive == null || isActive)) {
            list = categoriesRepository.findAllForTree();
        } else {
            list = categoriesRepository.findAllCategoriesList(search, isActive, includeDeleted, sorting);
        }
        return list.stream()
                .map(this::mapToMapResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MapCategoriesResponse> getCategoriesPaginated(String search, Boolean isActive, int page, int size, String sort, boolean includeDeleted) {
        if (search != null && search.trim().isEmpty()) {
            search = null;
        }
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<ProductCategory> pageResult = categoriesRepository.findAllCategoriesIncludeDeleted(search, isActive, includeDeleted, pageable);

        List<MapCategoriesResponse> content = pageResult.getContent().stream()
                .map(this::mapToMapResponse)
                .collect(Collectors.toList());

        return PageResponse.<MapCategoriesResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MapCategoriesResponse> getTree() {
        List<ProductCategory> allCategories = categoriesRepository.findAllForTree();

        Map<Long, MapCategoriesResponse> responseMap = allCategories.stream()
                .collect(Collectors.toMap(ProductCategory::getId, this::mapToMapResponse));

        List<MapCategoriesResponse> roots = new ArrayList<>();

        for (ProductCategory category : allCategories) {
            MapCategoriesResponse current = responseMap.get(category.getId());
            if (category.getParent() == null) {
                roots.add(current);
            } else {
                MapCategoriesResponse parentResponse = responseMap.get(category.getParent().getId());
                if (parentResponse != null) {
                    if (parentResponse.getChildren() == null) {
                        parentResponse.setChildren(new ArrayList<>());
                    }
                    parentResponse.getChildren().add(current);
                }
            }
        }

        return roots;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MapCategoriesResponse> getChildren(Long id) {
        if (!categoriesRepository.existsById(id)) {
            throw new ResourceNotFoundException("Danh mục", "id", id);
        }
        return categoriesRepository.findByParentIdAndIsDeletedFalse(id).stream()
                .map(this::mapToMapResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriesResponse getParent(Long id) {
        ProductCategory category = categoriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", id));

        if (category.getParent() != null && !Boolean.TRUE.equals(category.getParent().getIsDeleted())) {
            return mapToResponse(category.getParent());
        }
        return null;
    }

    // ===== Helper Methods =====

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
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    private DepartmentResponse mapToDepartmentResponse(Department dept) {
        if (dept == null) return null;
        return DepartmentResponse.builder()
                .id(dept.getId())
                .deptCode(dept.getDeptCode())
                .deptName(dept.getDeptName())
                .description(dept.getDescription())
                .isActive(dept.getIsActive())
                .createdAt(dept.getCreatedAt())
                .build();
    }

    private CategoriesResponse mapToResponse(ProductCategory entity) {
        if (entity == null) return null;
        CategoriesResponse response = new CategoriesResponse();
        response.setId(entity.getId());
        response.setCategoryCode(entity.getCategoryCode());
        response.setCategoryName(entity.getCategoryName());
        response.setDescription(entity.getDescription());
        response.setIsActive(entity.getIsActive());
        response.setDepartment(mapToDepartmentResponse(entity.getDepartment()));
        if (entity.getParent() != null) {
            response.setParentId(entity.getParent().getId());
        }
        response.setImageUrl(entity.getImageUrl());
        response.setManager(entity.getManager());
        response.setInventoryGlCode(entity.getInventoryGlCode());
        response.setCogsGlCode(entity.getCogsGlCode());
        response.setTaxClass(entity.getTaxClass());
        response.setIsDeleted(entity.getIsDeleted());
        response.setCreatedAt(entity.getCreatedAt());
        response.setCreatedBy(entity.getCreatedBy());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    private MapCategoriesResponse mapToMapResponse(ProductCategory entity) {
        return MapCategoriesResponse.builder()
                .id(entity.getId())
                .categoryCode(entity.getCategoryCode())
                .categoryName(entity.getCategoryName())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .department(mapToDepartmentResponse(entity.getDepartment()))
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .imageUrl(entity.getImageUrl())
                .isDeleted(entity.getIsDeleted())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private CreateCategoriesResponse mapToCreateResponse(ProductCategory entity) {
        return CreateCategoriesResponse.builder()
                .id(entity.getId())
                .categoryCode(entity.getCategoryCode())
                .categoryName(entity.getCategoryName())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .department(mapToDepartmentResponse(entity.getDepartment()))
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .imageUrl(entity.getImageUrl())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }

    private UpdateCategoriesResponse mapToUpdateResponse(ProductCategory entity) {
        return UpdateCategoriesResponse.builder()
                .id(entity.getId())
                .categoryCode(entity.getCategoryCode())
                .categoryName(entity.getCategoryName())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .department(mapToDepartmentResponse(entity.getDepartment()))
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .imageUrl(entity.getImageUrl())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
