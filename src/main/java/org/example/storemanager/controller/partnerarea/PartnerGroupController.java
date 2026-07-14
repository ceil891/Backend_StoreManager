package org.example.storemanager.controller.partnerarea;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.partnerarea.partnergroup.PartnerGroupRequest;
import org.example.storemanager.dto.response.ApiResponse;
import org.example.storemanager.dto.response.partnerarea.partnergroup.*; // Import tất cả DTO response
import org.example.storemanager.service.partnerarea.partnergroup.PartnerGroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partnerarea/groups")
@RequiredArgsConstructor
public class PartnerGroupController {

    private final PartnerGroupService service;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CreatePartnerGroupResponse>> create(@Valid @RequestBody PartnerGroupRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.create(req), "Tạo nhóm thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UpdatePartnerGroupResponse>> update(@PathVariable Long id, @Valid @RequestBody PartnerGroupRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req), "Cập nhật thành công"));
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UpdatePartnerGroupResponse>> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.toggleStatus(id), "Đổi trạng thái thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa nhóm thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartnerGroupDetailResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id), "Lấy chi tiết thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PartnerGroupListResponse>>> getAll(
            Pageable pageable,
            @RequestParam(required = false) String groupCode,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) Boolean isActive) {

        return ResponseEntity.ok(ApiResponse.success(service.findWithFilter(pageable, groupCode, type, groupName, isActive), "Lấy danh sách thành công"));
    }

    @PostMapping("/{groupId}/add-member")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> addMember(
            @PathVariable Long groupId,
            @RequestParam Long memberId,
            @RequestParam String type) {

        service.addMemberToGroup(groupId, memberId, type);
        // Ép kiểu <Void>
        return ResponseEntity.ok(ApiResponse.<Void>ok(null, "Thêm thành viên vào nhóm thành công"));
    }
}