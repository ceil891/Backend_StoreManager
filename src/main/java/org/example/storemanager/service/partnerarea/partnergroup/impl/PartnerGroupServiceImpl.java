package org.example.storemanager.service.partnerarea.partnergroup.impl;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.partnerarea.partnergroup.PartnerGroupRequest;
import org.example.storemanager.dto.response.partnerarea.partnergroup.*;
import org.example.storemanager.entity.partnerarea.PartnerGroup;
import org.example.storemanager.repository.partnerarea.PartnerGroupRepository;
import org.example.storemanager.service.partnerarea.partnergroup.PartnerGroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public CreatePartnerGroupResponse create(PartnerGroupRequest req) {
        if (repository.existsByGroupCode(req.getGroupCode())) {
            throw new RuntimeException("Mã nhóm đã tồn tại");
        }
        PartnerGroup group = PartnerGroup.builder()
                .groupCode(req.getGroupCode()).groupName(req.getGroupName()).type(req.getType())
                .description(req.getDescription()).initialMemberCount(req.getInitialMemberCount() != null ? req.getInitialMemberCount() : 0)
                .isActive(req.getIsActive() != null ? req.getIsActive() : true).build();
        group.setCreatedBy(getCurrentUser());

        return mapToCreateResponse(repository.save(group), "Tạo nhóm thành công");
    }

    @Override
    @Transactional
    public UpdatePartnerGroupResponse update(Long id, PartnerGroupRequest req) {
        PartnerGroup group = repository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm"));
        group.setGroupName(req.getGroupName());
        group.setType(req.getType());
        group.setDescription(req.getDescription());
        group.setIsActive(req.getIsActive() != null ? req.getIsActive() : group.getIsActive());
        group.setInitialMemberCount(req.getInitialMemberCount());
        group.setUpdatedBy(getCurrentUser());

        return mapToUpdateResponse(repository.save(group), "Cập nhật thành công");
    }

    @Override
    @Transactional
    public UpdatePartnerGroupResponse toggleStatus(Long id) {
        PartnerGroup group = repository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm"));
        group.setIsActive(!group.getIsActive());
        group.setUpdatedBy(getCurrentUser());
        return mapToUpdateResponse(repository.save(group), "Thay đổi trạng thái thành công");
    }

    @Override
    @Transactional
    public DeletePartnerGroupResponse delete(Long id) {
        PartnerGroup group = repository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm"));
        if (Boolean.TRUE.equals(group.getIsActive())) {
            throw new RuntimeException("Không thể xóa nhóm đang ở trạng thái KÍCH HOẠT.");
        }
        group.setIsDeleted(true);
        group.setDeletedAt(LocalDateTime.now());
        group.setDeletedBy(getCurrentUser());
        PartnerGroup deleted = repository.save(group);

        return DeletePartnerGroupResponse.builder()
                .id(deleted.getId()).message("Xóa nhóm thành công")
                .deletedAt(deleted.getDeletedAt()).deletedBy(deleted.getDeletedBy()).build();
    }

    @Override
    @Transactional(readOnly = true)
    public PartnerGroupDetailResponse getById(Long id) {
        return repository.findById(id).filter(g -> !g.getIsDeleted()).map(this::mapToDetailResponse)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PartnerGroupListResponse> findWithFilter(Pageable pageable, String groupCode, String type, String groupName, Boolean isActive) {
        return repository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), false));
            if (groupCode != null && !groupCode.isEmpty()) predicates.add(cb.equal(root.get("groupCode"), groupCode));
            if (type != null && !type.isEmpty()) predicates.add(cb.equal(root.get("type"), type));
            if (groupName != null && !groupName.isEmpty()) predicates.add(cb.like(cb.lower(root.get("groupName")), "%" + groupName.toLowerCase() + "%"));
            if (isActive != null) predicates.add(cb.equal(root.get("isActive"), isActive));
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable).map(this::mapToListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PartnerGroupListResponse> getAll(Pageable pageable, String search, String type) {
        return findWithFilter(pageable, null, type, search, null);
    }

    // --- CÁC HÀM MAPPING ĐẦY ĐỦ TRƯỜNG ---

    private CreatePartnerGroupResponse mapToCreateResponse(PartnerGroup g, String msg) {
        return CreatePartnerGroupResponse.builder()
                .id(g.getId()).groupCode(g.getGroupCode()).groupName(g.getGroupName()).type(g.getType())
                .description(g.getDescription()).initialMemberCount(g.getInitialMemberCount()).isActive(g.getIsActive())
                .createdAt(g.getCreatedAt()).createdBy(g.getCreatedBy()).updatedAt(g.getUpdatedAt()).updatedBy(g.getUpdatedBy())
                .message(msg).build();
    }

    private UpdatePartnerGroupResponse mapToUpdateResponse(PartnerGroup g, String msg) {
        return UpdatePartnerGroupResponse.builder()
                .id(g.getId()).groupCode(g.getGroupCode()).groupName(g.getGroupName()).type(g.getType())
                .description(g.getDescription()).initialMemberCount(g.getInitialMemberCount()).isActive(g.getIsActive())
                .createdAt(g.getCreatedAt()).createdBy(g.getCreatedBy()).updatedAt(g.getUpdatedAt()).updatedBy(g.getUpdatedBy())
                .message(msg).build();
    }

    private PartnerGroupDetailResponse mapToDetailResponse(PartnerGroup g) {
        return PartnerGroupDetailResponse.builder()
                .id(g.getId()).groupCode(g.getGroupCode()).groupName(g.getGroupName()).type(g.getType())
                .description(g.getDescription()).initialMemberCount(g.getInitialMemberCount()).isActive(g.getIsActive())
                .createdAt(g.getCreatedAt()).createdBy(g.getCreatedBy()).updatedAt(g.getUpdatedAt()).updatedBy(g.getUpdatedBy()).build();
    }

    private PartnerGroupListResponse mapToListResponse(PartnerGroup g) {
        return PartnerGroupListResponse.builder()
                .id(g.getId()).groupCode(g.getGroupCode()).groupName(g.getGroupName())
                .type(g.getType()).isActive(g.getIsActive()).build();
    }
}