package org.example.storemanager.controller.catalog;

import jakarta.validation.Valid;
import org.example.storemanager.dto.request.catalog.department.CreateDepartmentRequest;
import org.example.storemanager.dto.request.catalog.department.UpdateDepartmentRequest;
import org.example.storemanager.dto.response.catalog.department.CreateDepartmentResponse;
import org.example.storemanager.dto.response.catalog.department.DeleteDepartmentResponse;
import org.example.storemanager.dto.response.catalog.department.UpdateDepartmentResponse;
import org.example.storemanager.dto.response.catalog.department.DepartmentResponse;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.service.catalog.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    @Autowired
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // ========== TẠO MỚI ==========
    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:department:create')")
    public ResponseEntity<ApiResponse<CreateDepartmentResponse>> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        CreateDepartmentResponse response = departmentService.createDepartment(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    // ========== CẬP NHẬT ==========
    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:department:update')")
    public ResponseEntity<ApiResponse<UpdateDepartmentResponse>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest request) {
        UpdateDepartmentResponse response = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật ngành hàng thành công", response));
    }

    // ========== CẬP NHẬT TRẠNG THÁI (Bật/Tắt hoạt động) ==========
    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:department:update-status')")
    public ResponseEntity<ApiResponse<UpdateDepartmentResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        UpdateDepartmentResponse response = departmentService.updateStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", response));
    }

    // ========== XÓA MỀM (Chỉ xóa được khi đã tắt hoạt động) ==========
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:department:delete')")
    public ResponseEntity<ApiResponse<DeleteDepartmentResponse>> deleteDepartment(@PathVariable Long id) {
        DeleteDepartmentResponse response = departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa ngành hàng thành công", response));
    }

    // ========== XEM CHI TIẾT THEO ID (kể cả đã xóa) ==========
    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:department:view')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(@PathVariable Long id) {
        DepartmentResponse response = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== DANH SÁCH (phân trang hoặc tất cả) ==========
    /**
     * Tham số:
     * - search       : tìm kiếm theo mã/tên/mô tả
     * - isActive     : true=Hoạt động | false=Tắt | null=Tất cả
     * - includeDeleted: true=Bao gồm cả đã xóa mềm | false (mặc định)=Chỉ chưa xóa
     * - page, size   : phân trang (nếu có cả 2 thì trả PageResponse, không thì trả List)
     * - sort         : ví dụ "deptName,asc" hoặc "createdAt,desc"
     *
     * Ví dụ Postman:
     * GET /api/v1/departments?includeDeleted=true              → tất cả kể cả đã xóa
     * GET /api/v1/departments?isActive=false&includeDeleted=false → chỉ đang tắt, chưa xóa
     * GET /api/v1/departments?page=0&size=10&includeDeleted=true  → phân trang kể cả xóa
     */
    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:department:view')")
    public ResponseEntity<ApiResponse<?>> getDepartments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "deptName,asc") String sort) {
        if (page != null && size != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                departmentService.getDepartmentsPaginated(search, isActive, page, size, sort, includeDeleted)));
        } else {
            return ResponseEntity.ok(ApiResponse.ok(
                departmentService.getAllDepartments(search, isActive, sort, includeDeleted)));
        }
    }
}
