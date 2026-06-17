package org.example.storemanager.controller.catalog;

import jakarta.validation.Valid;
import org.example.storemanager.dto.request.catalog.unit.CreateUnitRequest;
import org.example.storemanager.dto.request.catalog.unit.UpdateUnitRequest;
import org.example.storemanager.dto.response.catalog.unit.CreateUnitResponse;
import org.example.storemanager.dto.response.catalog.unit.DeleteUnitResponse;
import org.example.storemanager.dto.response.catalog.unit.UpdateUnitResponse;
import org.example.storemanager.dto.response.catalog.unit.UnitResponse;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.service.catalog.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/units")
public class UnitController {

    private final UnitService unitService;

    @Autowired
    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    // ========== TẠO MỚI ==========
    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:unit:create')")
    public ResponseEntity<ApiResponse<CreateUnitResponse>> createUnit(@Valid @RequestBody CreateUnitRequest request) {
        CreateUnitResponse response = unitService.createUnit(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    // ========== CẬP NHẬT ==========
    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:unit:update')")
    public ResponseEntity<ApiResponse<UpdateUnitResponse>> updateUnit(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUnitRequest request) {
        UpdateUnitResponse response = unitService.updateUnit(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật đơn vị thành công", response));
    }

    // ========== CẬP NHẬT TRẠNG THÁI (Bật/Tắt hoạt động) ==========
    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:unit:update-status')")
    public ResponseEntity<ApiResponse<UpdateUnitResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        UpdateUnitResponse response = unitService.updateStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", response));
    }

    // ========== XÓA MỀM (Chỉ xóa được khi đã tắt hoạt động) ==========
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:unit:delete')")
    public ResponseEntity<ApiResponse<DeleteUnitResponse>> deleteUnit(@PathVariable Long id) {
        DeleteUnitResponse response = unitService.deleteUnit(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa đơn vị thành công", response));
    }

    // ========== XEM CHI TIẾT THEO ID (kể cả đã xóa) ==========
    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:unit:view')")
    public ResponseEntity<ApiResponse<UnitResponse>> getUnitById(@PathVariable Long id) {
        UnitResponse response = unitService.getUnitById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ========== DANH SÁCH (phân trang hoặc tất cả) ==========
    /**
     * Tham số:
     * - search       : tìm kiếm theo mã/tên/mô tả
     * - isActive     : true=Hoạt động | false=Tắt | null=Tất cả
     * - includeDeleted: true=Bao gồm cả đã xóa mềm | false (mặc định)=Chỉ chưa xóa
     * - page, size   : phân trang (nếu có cả 2 thì trả PageResponse, không thì trả List)
     * - sort         : ví dụ "unitName,asc" hoặc "createdAt,desc"
     *
     * Ví dụ Postman:
     * GET /api/v1/units?includeDeleted=true              → tất cả kể cả đã xóa
     * GET /api/v1/units?isActive=false&includeDeleted=false → chỉ đang tắt, chưa xóa
     * GET /api/v1/units?page=0&size=10&includeDeleted=true  → phân trang kể cả xóa
     */
    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('catalog:unit:view')")
    public ResponseEntity<ApiResponse<?>> getUnits(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "unitName,asc") String sort) {
        if (page != null && size != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                unitService.getUnitsPaginated(search, isActive, page, size, sort, includeDeleted)));
        } else {
            return ResponseEntity.ok(ApiResponse.ok(
                unitService.getAllUnits(search, isActive, sort, includeDeleted)));
        }
    }
}
