package org.example.storemanager.service.partnerarea.partnergroup.impl;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.partnerarea.partnergroup.PartnerGroupRequest;
import org.example.storemanager.dto.response.partnerarea.partnergroup.*;
import org.example.storemanager.entity.partnerarea.PartnerGroup;
import org.example.storemanager.enums.ErrorCode;
import org.example.storemanager.enums.PartnerType;
import org.example.storemanager.exception.AppException;
import org.example.storemanager.repository.partnerarea.PartnerGroupRepository;
import org.example.storemanager.service.partnerarea.partnergroup.PartnerGroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import org.example.storemanager.dto.response.partnerarea.supplier.SupplierInfo;
import org.example.storemanager.dto.response.partnerarea.customer.CustomerInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PartnerGroupServiceImpl implements PartnerGroupService {

    private final PartnerGroupRepository repository;

    private String getCurrentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private void validatePartnerType(String type) {
        try {
            PartnerType.valueOf(type.toUpperCase()); // Kiểm tra xem type có nằm trong Enum không
        } catch (Exception e) {
            throw new AppException(ErrorCode.BUSINESS_ERROR, "Loại nhóm '" + type + "' không hợp lệ. Chỉ chấp nhận 'CUSTOMER' hoặc 'SUPPLIER'");
        }
    }

    @Override
    @Transactional
    public CreatePartnerGroupResponse create(PartnerGroupRequest req) {
        if (repository.existsByGroupCode(req.getGroupCode())) {
            // Ném đúng lỗi DUPLICATE_RESOURCE, hệ thống sẽ tự lấy status 409
            throw new AppException(ErrorCode.DUPLICATE_RESOURCE,
                    "Mã nhóm '" + req.getGroupCode() + "' đã tồn tại");
        }
        if (repository.existsByGroupName(req.getGroupName())) {
            throw new AppException(ErrorCode.DUPLICATE_RESOURCE,
                    "Tên nhóm '" + req.getGroupName() + "' đã tồn tại");
        }

        validatePartnerType(req.getType());

        PartnerGroup group = PartnerGroup.builder()
                .groupCode(req.getGroupCode())
                .groupName(req.getGroupName())
                .type(req.getType())
                .description(req.getDescription())
                .initialMemberCount(0)
                .isActive(true)
                .build();
        group.setCreatedBy(getCurrentUser());

        PartnerGroup savedGroup = repository.save(group);

        // KẾT HỢP TẤT CẢ VÀO 1 BUILDER DUY NHẤT
        return CreatePartnerGroupResponse.builder()
                .id(savedGroup.getId())
                .groupCode(savedGroup.getGroupCode())
                .groupName(savedGroup.getGroupName())
                .type(savedGroup.getType())
                .description(savedGroup.getDescription())
                .initialMemberCount(savedGroup.getInitialMemberCount())
                .isActive(savedGroup.getIsActive())
                .createdAt(savedGroup.getCreatedAt())
                .createdBy(savedGroup.getCreatedBy())
                .success(true)
                .status(200)
                .message("Tạo nhóm thành công")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public UpdatePartnerGroupResponse update(Long id, PartnerGroupRequest req) {
        // 1. Validate loại nhóm
        validatePartnerType(req.getType());

        // 2. Tìm nhóm hiện tại
        PartnerGroup group = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy nhóm"));

        // 3. Chỉ check trùng nếu mã/tên MỚI khác với mã/tên HIỆN TẠI
        if (!req.getGroupCode().equals(group.getGroupCode())) {
            if (repository.existsByGroupCode(req.getGroupCode())) {
                throw new AppException(ErrorCode.DUPLICATE_RESOURCE, "Mã nhóm đã tồn tại");
            }
        }
        if (!req.getGroupName().equals(group.getGroupName())) {
            if (repository.existsByGroupName(req.getGroupName())) {
                throw new AppException(ErrorCode.DUPLICATE_RESOURCE, "Tên nhóm đã tồn tại");
            }
        }

        // 4. Cập nhật thông tin
        group.setGroupCode(req.getGroupCode());
        group.setGroupName(req.getGroupName());
        repository.save(group);
        group.setType(req.getType().toUpperCase());
        group.setDescription(req.getDescription());
        group.setIsActive(req.getIsActive() != null ? req.getIsActive() : group.getIsActive());
        group.setUpdatedBy(getCurrentUser());

        // 5. Lưu xuống DB
        PartnerGroup updatedGroup = repository.save(group);

        // 6. Trả về Response
        return UpdatePartnerGroupResponse.builder()
                .id(group.getId())
                .groupCode(updatedGroup.getGroupCode())
                .groupName(updatedGroup.getGroupName())
                .type(updatedGroup.getType())
                .description(updatedGroup.getDescription())
                .isActive(updatedGroup.getIsActive())
                // Đếm danh sách an toàn
                .initialMemberCount(group.getCustomers() != null ? group.getCustomers().size() : 0)                .updatedAt(updatedGroup.getUpdatedAt())
                .updatedBy(updatedGroup.getUpdatedBy())
                .success(true)
                .status(200)
                .message("Cập nhật thành công")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public UpdatePartnerGroupResponse toggleStatus(Long id) {
        PartnerGroup group = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy nhóm"));
        group.setIsActive(!group.getIsActive());
        group.setUpdatedBy(getCurrentUser());
        PartnerGroup updatedGroup = repository.save(group);

        return UpdatePartnerGroupResponse.builder()
                .id(updatedGroup.getId())
                .groupCode(updatedGroup.getGroupCode())
                .groupName(updatedGroup.getGroupName())
                .type(updatedGroup.getType())
                .description(updatedGroup.getDescription())
                .initialMemberCount(updatedGroup.getInitialMemberCount())
                .isActive(updatedGroup.getIsActive())
                .updatedAt(updatedGroup.getUpdatedAt())
                .updatedBy(updatedGroup.getUpdatedBy())
                .success(true)
                .status(200)
                .message("Thay đổi trạng thái thành công")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public DeletePartnerGroupResponse delete(Long id) {
        PartnerGroup group = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy nhóm"));
        if (Boolean.TRUE.equals(group.getIsActive())) {
            throw new AppException(ErrorCode.BUSINESS_ERROR, "Không thể xóa nhóm đang ở trạng thái KÍCH HOẠT.");
        }
        group.setIsDeleted(true);
        group.setDeletedAt(LocalDateTime.now());
        group.setDeletedBy(getCurrentUser());
        group.setIsActive(false);
        PartnerGroup deleted = repository.save(group);

        return DeletePartnerGroupResponse.builder()
                .id(deleted.getId())
                .deletedAt(deleted.getDeletedAt())
                .deletedBy(deleted.getDeletedBy())
                .isDeleted(deleted.getIsDeleted())
                .isActive(deleted.getIsActive())
                .success(true)
                .status(200)
                .message("Xóa nhóm thành công")
                .timestamp(LocalDateTime.now())
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    public PartnerGroupDetailResponse getById(Long id) {
        PartnerGroup group = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy nhóm"));

        // Map khách hàng an toàn
        List<CustomerInfo> customerInfos = (group.getCustomers() == null) ?
                Collections.emptyList() :
                group.getCustomers().stream()
                        .map(c -> CustomerInfo.builder()
                                .id(c.getId())
                                .name(c.getName())
                                .phone(c.getPhone())
                                .customerCode(c.getCustomerCode())
                                .build())
                        .toList();

        List<SupplierInfo> supplierInfos = (group.getSuppliers() == null) ?
                Collections.emptyList() :
                group.getSuppliers().stream()
                        .map(s -> SupplierInfo.builder()
                                .id(s.getId())
                                .name(s.getName())
                                .phone(s.getPhone())
                                .build())
                        .toList();

        return PartnerGroupDetailResponse.builder()
                .id(group.getId())
                .groupCode(group.getGroupCode())
                .groupName(group.getGroupName())
                .type(group.getType())
                .description(group.getDescription())
                .isActive(group.getIsActive())
                .isDeleted(group.getIsDeleted())
                .initialMemberCount("SUPPLIER".equals(group.getType()) ? supplierInfos.size() : customerInfos.size())
                .customers(customerInfos)
                .suppliers(supplierInfos)
                .createdAt(group.getCreatedAt())
                .createdBy(group.getCreatedBy())
                .updatedAt(group.getUpdatedAt())
                .updatedBy(group.getUpdatedBy())
                .success(true)
                .status(200)
                .message("Lấy chi tiết thành công")
                .timestamp(LocalDateTime.now())
                .suppliers(supplierInfos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PartnerGroupListResponse> findWithFilter(Pageable pageable, String groupCode, String type, String groupName, Boolean isActive) {
        // 1. Validate 'type' trước khi lọc
        if (type != null && !type.isEmpty()) {
            if (!"CUSTOMER".equalsIgnoreCase(type) && !"SUPPLIER".equalsIgnoreCase(type)) {
                throw new AppException(ErrorCode.BUSINESS_ERROR,
                        "Loại nhóm '" + type + "' không hợp lệ. Chỉ chấp nhận 'CUSTOMER' hoặc 'SUPPLIER'");
            }
        }

        // 2. Thực hiện lọc dữ liệu (chỉ 1 lần gọi findAll duy nhất)
        return repository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (groupCode != null && !groupCode.isEmpty())
                predicates.add(cb.equal(root.get("groupCode"), groupCode));
            if (type != null && !type.isEmpty())
                predicates.add(cb.equal(root.get("type"), type.toUpperCase())); // Đảm bảo so sánh đồng nhất
            if (groupName != null && !groupName.isEmpty())
                predicates.add(cb.like(cb.lower(root.get("groupName")), "%" + groupName.toLowerCase() + "%"));
            if (isActive != null)
                predicates.add(cb.equal(root.get("isActive"), isActive));

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable).map(this::mapToListResponse);
    }

    @Override
    public Page<PartnerGroupListResponse> getAll(Pageable pageable, String search, String type) {
        // Dùng Specification để lọc
        Specification<PartnerGroup> spec = (root, query, cb) -> {
            Predicate p = cb.equal(root.get("isDeleted"), false); // Chỉ lấy cái chưa xóa
            if (search != null) p = cb.and(p, cb.like(root.get("groupName"), "%" + search + "%"));
            return p;
        };
        return repository.findAll(spec, pageable).map(this::mapToListResponse);
    }


    private CreatePartnerGroupResponse mapToCreateResponse(PartnerGroup g, String msg) {
        return CreatePartnerGroupResponse.builder()
                .id(g.getId())
                .groupCode(g.getGroupCode())
                .groupName(g.getGroupName())
                .type(g.getType())
                .description(g.getDescription())
                .initialMemberCount(g.getInitialMemberCount())
                .isActive(g.getIsActive())
                .createdAt(g.getCreatedAt())
                .createdBy(g.getCreatedBy()).updatedAt(g.getUpdatedAt())
                .updatedBy(g.getUpdatedBy())
                .message(msg)
                .build();
    }

    private UpdatePartnerGroupResponse mapToUpdateResponse(PartnerGroup g, String msg) {
        return UpdatePartnerGroupResponse.builder()
                .id(g.getId())
                .groupCode(g.getGroupCode())
                .groupName(g.getGroupName())
                .type(g.getType())
                .description(g.getDescription())
                .initialMemberCount(g.getInitialMemberCount())
                .isActive(g.getIsActive())
                .updatedAt(g.getUpdatedAt())
                .updatedBy(g.getUpdatedBy())
                .message(msg)
                .build();
    }

    private PartnerGroupDetailResponse mapToDetailResponse(PartnerGroup g) {
        // 1. Xử lý danh sách khách hàng an toàn
        System.out.println("DEBUG - ID: " + g.getId() + ", Suppliers list size: " + (g.getSuppliers() != null ? g.getSuppliers().size() : "NULL"));
        List<CustomerInfo> customerInfos = (g.getCustomers() == null) ?
                Collections.emptyList() :
                g.getCustomers().stream()
                        .map(c -> CustomerInfo.builder()
                                .id(c.getId())
                                .name(c.getName())
                                .phone(c.getPhone())
                                .customerCode(c.getCustomerCode())
                                .build())
                        .toList();

        List<SupplierInfo> supplierInfos = (g.getSuppliers() == null) ?
                Collections.emptyList() :
                g.getSuppliers().stream()
                        .map(s -> SupplierInfo.builder()
                                .id(s.getId())
                                .name(s.getName())
                                .phone(s.getPhone())
                                .build())
                        .toList();

        // 2. Build response đầy đủ
        return PartnerGroupDetailResponse.builder()
                .id(g.getId())
                .groupCode(g.getGroupCode())
                .groupName(g.getGroupName())
                .type(g.getType())
                .description(g.getDescription())
                .isActive(g.getIsActive())
                .isDeleted(g.getIsDeleted())
                .initialMemberCount("SUPPLIER".equalsIgnoreCase(g.getType()) ? supplierInfos.size() : customerInfos.size())
                .customers(customerInfos)                 // Gán luôn list đã map
                .suppliers(supplierInfos)
                .createdAt(g.getCreatedAt())
                .createdBy(g.getCreatedBy())
                .updatedAt(g.getUpdatedAt())
                .updatedBy(g.getUpdatedBy())
                .success(true)                            // Thêm vào nếu DTO có các trường này
                .status(200)
                .message("Lấy chi tiết thành công")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private PartnerGroupListResponse mapToListResponse(PartnerGroup g) {
        return PartnerGroupListResponse.builder()
                .id(g.getId()).groupCode(g.getGroupCode()).groupName(g.getGroupName())
                .type(g.getType()).isActive(g.getIsActive())
                .success(true)
                .status(200)
                .message("Lấy danh sách thành công")
                .timestamp(LocalDateTime.now())
                .build();
    }
}