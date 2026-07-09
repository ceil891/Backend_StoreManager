package org.example.storemanager.controller.hrm;

import jakarta.validation.Valid;
import org.example.storemanager.dto.request.hrm.position.CreatePositionRequest;
import org.example.storemanager.dto.request.hrm.position.UpdatePositionRequest;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.hrm.position.CreatePositionResponse;
import org.example.storemanager.dto.response.hrm.position.DeletePositionResponse;
import org.example.storemanager.dto.response.hrm.position.PositionDropdownResponse;
import org.example.storemanager.dto.response.hrm.position.PositionResponse;
import org.example.storemanager.dto.response.hrm.position.UpdatePositionResponse;
import org.example.storemanager.service.hrm.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hrm/positions")
public class PositionController {

    private final PositionService positionService;

    @Autowired
    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:position:create')")
    public ResponseEntity<ApiResponse<CreatePositionResponse>> create(@Valid @RequestBody CreatePositionRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(positionService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:position:update')")
    public ResponseEntity<ApiResponse<UpdatePositionResponse>> update(
            @PathVariable Long id, @Valid @RequestBody UpdatePositionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật vị trí thành công", positionService.update(id, request)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:position:update-status')")
    public ResponseEntity<ApiResponse<UpdatePositionResponse>> updateStatus(
            @PathVariable Long id, @RequestParam Boolean isActive) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", positionService.updateStatus(id, isActive)));
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:position:update-status')")
    public ResponseEntity<ApiResponse<UpdatePositionResponse>> toggleActive(@PathVariable Long id, @RequestParam Boolean isActive) {
        return ResponseEntity.ok(ApiResponse.ok("Bật/Tắt trạng thái thành công", positionService.updateStatus(id, isActive)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:position:delete')")
    public ResponseEntity<ApiResponse<DeletePositionResponse>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Xóa vị trí thành công", positionService.delete(id)));
    }

    @GetMapping("/dropdown")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:position:view')")
    public ResponseEntity<ApiResponse<List<PositionDropdownResponse>>> getDropdown() {
        return ResponseEntity.ok(ApiResponse.ok(positionService.getDropdownList()));
    }

    @GetMapping("/search")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:position:view')")
    public ResponseEntity<ApiResponse<List<PositionResponse>>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(positionService.search(keyword)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:position:view')")
    public ResponseEntity<ApiResponse<PositionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(positionService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:position:view')")
    public ResponseEntity<ApiResponse<?>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "positionName,asc") String sort) {
        if (page != null && size != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                    positionService.getPaginated(search, isActive, departmentId, page, size, sort, includeDeleted)));
        }
        return ResponseEntity.ok(ApiResponse.ok(
                positionService.getAll(search, isActive, departmentId, sort, includeDeleted)));
    }
}
