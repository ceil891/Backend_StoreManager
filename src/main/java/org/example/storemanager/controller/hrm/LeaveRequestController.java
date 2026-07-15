package org.example.storemanager.controller.hrm;

import jakarta.validation.Valid;
import org.example.storemanager.dto.request.hrm.leave.ApproveLeaveRequest;
import org.example.storemanager.dto.request.hrm.leave.CreateLeaveRequest;
import org.example.storemanager.dto.request.hrm.leave.UpdateLeaveRequest;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.hrm.leave.*;
import org.example.storemanager.service.hrm.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hrm/leaves")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @Autowired
    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:leave:create')")
    public ResponseEntity<ApiResponse<CreateLeaveResponse>> create(@Valid @RequestBody CreateLeaveRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(leaveRequestService.create(request)));
    }

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:leave:view')")
    public ResponseEntity<ApiResponse<?>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "startDate,desc") String sort) {
        if (page != null && size != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                    leaveRequestService.getPaginated(search, isActive, userId, status, page, size, sort, includeDeleted)));
        }
        return ResponseEntity.ok(ApiResponse.ok(
                leaveRequestService.getAll(search, isActive, userId, status, sort, includeDeleted)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:leave:view')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(leaveRequestService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:leave:update')")
    public ResponseEntity<ApiResponse<UpdateLeaveResponse>> update(
            @PathVariable Long id, @Valid @RequestBody UpdateLeaveRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật đơn nghỉ phép thành công", leaveRequestService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:leave:delete')")
    public ResponseEntity<ApiResponse<DeleteLeaveResponse>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Xóa đơn nghỉ phép thành công", leaveRequestService.delete(id)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:leave:approve')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Duyệt đơn nghỉ phép thành công", leaveRequestService.approve(id)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:leave:approve')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> reject(
            @PathVariable Long id, @RequestBody(required = false) ApproveLeaveRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Từ chối đơn nghỉ phép thành công", leaveRequestService.reject(id, request)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:leave:approve')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Hủy đơn nghỉ phép thành công", leaveRequestService.cancel(id)));
    }

    @GetMapping("/pending")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:leave:approve')")
    public ResponseEntity<ApiResponse<?>> getPendingLeaves() {
        return ResponseEntity.ok(ApiResponse.ok(leaveRequestService.getPendingLeaves()));
    }

    @GetMapping("/users/{userId}/leave-requests")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:leave:view')")
    public ResponseEntity<ApiResponse<?>> getUserLeaveHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(leaveRequestService.getUserLeaveHistory(userId)));
    }

    @GetMapping("/users/{userId}/leave-balance")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:leave:view')")
    public ResponseEntity<ApiResponse<LeaveBalanceResponse>> getUserLeaveBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(leaveRequestService.getUserLeaveBalance(userId)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:leave:update-status')")
    public ResponseEntity<ApiResponse<UpdateLeaveResponse>> updateStatus(
            @PathVariable Long id, @RequestParam Boolean isActive) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", leaveRequestService.updateStatus(id, isActive)));
    }
}
