package org.example.storemanager.modules.partnerarea.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.partnerarea.dto.request.supplier.CreateSupplierRequest;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.partnerarea.dto.response.supplier.CreateSupplierResponse;
import org.example.storemanager.modules.partnerarea.dto.response.supplier.UpdateSupplierResponse;
import org.example.storemanager.modules.partnerarea.dto.response.supplier.SupplierDetailResponse;
import org.example.storemanager.modules.partnerarea.service.supplier.SupplierService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partnerarea/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService service;

    // 1. GET: Danh sách có phân trang
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(ApiResponse.ok("Thành công", service.getAll(isActive, PageRequest.of(page, size))));
    }

    // 2. GET: Lấy chi tiết theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierDetailResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Thành công", service.getById(id)));
    }

    // 3. POST: Tạo mới
    @PostMapping
    public ResponseEntity<ApiResponse<CreateSupplierResponse>> create(
            @Valid @RequestBody CreateSupplierRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Tạo thành công", service.create(req)));
    }

    // 4. PUT: Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UpdateSupplierResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateSupplierRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công", service.update(id, req)));
    }

    // 5. DELETE: Khóa/Xóa mềm
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa nhà cung cấp thành công", null));
    }

    // Lấy danh sách đang hoạt động
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<?>> getActive(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok("Thành công", service.getAll(true, PageRequest.of(page, size))));
    }

    // Lấy danh sách đã khóa (không hoạt động)
    @GetMapping("/inactive")
    public ResponseEntity<ApiResponse<?>> getInactive(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok("Thành công", service.getAll(false, PageRequest.of(page, size))));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<?>> updateStatus(@PathVariable Long id) {
        service.updateStatus(id); // Gọi hàm mới không cần tham số
        return ResponseEntity.ok(ApiResponse.ok("Đã đảo trạng thái hoạt động thành công", null));
    }

}