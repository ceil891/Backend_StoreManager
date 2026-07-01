package org.example.storemanager.controller.hrm;

import jakarta.validation.Valid;
import org.example.storemanager.dto.request.hrm.department.CreateDepartmentHrmRequest;
import org.example.storemanager.dto.request.hrm.department.UpdateDepartmentHrmRequest;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.hrm.department.*;
import org.example.storemanager.service.hrm.DepartmentHrmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hrm/departments")
public class DepartmentHrmController {

    private final DepartmentHrmService departmentHrmService;

    @Autowired
    public DepartmentHrmController(DepartmentHrmService departmentHrmService) {
        this.departmentHrmService = departmentHrmService;
    }

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:department:create')")
    public ResponseEntity<ApiResponse<CreateDepartmentHrmResponse>> create(@Valid @RequestBody CreateDepartmentHrmRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(departmentHrmService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:department:update')")
    public ResponseEntity<ApiResponse<UpdateDepartmentHrmResponse>> update(
            @PathVariable Long id, @Valid @RequestBody UpdateDepartmentHrmRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật phòng ban thành công", departmentHrmService.update(id, request)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:department:update-status')")
    public ResponseEntity<ApiResponse<UpdateDepartmentHrmResponse>> updateStatus(
            @PathVariable Long id, @RequestParam Boolean isActive) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", departmentHrmService.updateStatus(id, isActive)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:department:delete')")
    public ResponseEntity<ApiResponse<DeleteDepartmentHrmResponse>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Xóa phòng ban thành công", departmentHrmService.delete(id)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:department:view')")
    public ResponseEntity<ApiResponse<DepartmentHrmResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(departmentHrmService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:department:view')")
    public ResponseEntity<ApiResponse<?>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "deptName,asc") String sort) {
        if (page != null && size != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                    departmentHrmService.getPaginated(search, isActive, page, size, sort, includeDeleted)));
        }
        return ResponseEntity.ok(ApiResponse.ok(
                departmentHrmService.getAll(search, isActive, sort, includeDeleted)));
    }
}
