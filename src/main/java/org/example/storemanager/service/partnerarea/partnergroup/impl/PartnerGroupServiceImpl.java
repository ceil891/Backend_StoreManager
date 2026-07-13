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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
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
        validatePartnerType(req.getType()); // Phải validate trước!

        PartnerGroup group = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy nhóm"));

        // Validate trùng lặp khi sửa (QUAN TRỌNG)
        if (!group.getGroupCode().equals(req.getGroupCode()) && repository.existsByGroupCode(req.getGroupCode())) {
            throw new AppException(ErrorCode.DUPLICATE_RESOURCE, "Mã nhóm đã tồn tại");
        }
        if (!group.getGroupName().equals(req.getGroupName()) && repository.existsByGroupName(req.getGroupName())) {
            throw new AppException(ErrorCode.DUPLICATE_RESOURCE, "Tên nhóm đã tồn tại");
        }

        group.setGroupName(req.getGroupName());
        group.setType(req.getType().toUpperCase()); // Lưu dạng hoa
        group.setDescription(req.getDescription());
        group.setIsActive(req.getIsActive() != null ? req.getIsActive() : group.getIsActive());
        group.setUpdatedBy(getCurrentUser());

        PartnerGroup updatedGroup = repository.save(group);

        validatePartnerType(req.getType());

        return UpdatePartnerGroupResponse.builder()
                .id(updatedGroup
                .getId()).groupCode(updatedGroup.getGroupCode())
                .groupName(updatedGroup.getGroupName())
                .type(updatedGroup.getType())
                .description(updatedGroup.getDescription())
                .isActive(updatedGroup.getIsActive())
                .updatedAt(updatedGroup.getUpdatedAt())
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
        PartnerGroup deleted = repository.save(group);

        return DeletePartnerGroupResponse.builder()
                .id(deleted.getId())
                .deletedAt(deleted.getDeletedAt())
                .deletedBy(deleted.getDeletedBy())
                .success(true)
                .status(200)
                .message("Xóa nhóm thành công")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PartnerGroupDetailResponse getById(Long id) {
        // 1. Tìm nhóm, kiểm tra tồn tại và kiểm tra đã xóa hay chưa
        PartnerGroup group = repository.findById(id)
                .filter(g -> !Boolean.TRUE.equals(g.getIsDeleted()))
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy nhóm"));

        // 2. Map danh sách khách hàng (Xử lý trường hợp null an toàn)
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

        // 3. Build response với tất cả thông tin
        return PartnerGroupDetailResponse.builder()
                // Thông tin nhóm
                .id(group.getId())
                .groupCode(group.getGroupCode())
                .groupName(group.getGroupName())
                .type(group.getType())
                .description(group.getDescription())
                .isActive(group.getIsActive())
                .createdAt(group.getCreatedAt())
                .createdBy(group.getCreatedBy())
                .updatedAt(group.getUpdatedAt())
                .updatedBy(group.getUpdatedBy())
                // Thông tin khách hàng và đếm số lượng
                .customers(customerInfos)
                .initialMemberCount(customerInfos.size())
                // Thông tin response wrapper
                .success(true)
                .status(200)
                .message("Lấy chi tiết thành công")
                .timestamp(LocalDateTime.now())
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
    @Transactional(readOnly = true)
    public Page<PartnerGroupListResponse> getAll(Pageable pageable, String search, String type) {
        return findWithFilter(pageable, null, type, search, null);
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
        return PartnerGroupDetailResponse.builder()
                .id(g.getId())
                .groupCode(g.getGroupCode())
                .groupName(g.getGroupName())
                .type(g.getType())
                .description(g.getDescription())
                .initialMemberCount(g.getInitialMemberCount())
                .isActive(g.getIsActive())
                .createdAt(g.getCreatedAt())
                .createdBy(g.getCreatedBy())
                .updatedAt(g.getUpdatedAt())
                .updatedBy(g.getUpdatedBy())
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