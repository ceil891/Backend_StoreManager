package org.example.storemanager.controller.partnerarea;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.partnerarea.supplier.CreateSupplierRequest;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.partnerarea.supplier.CreateSupplierResponse;
import org.example.storemanager.dto.response.partnerarea.supplier.UpdateSupplierResponse;
import org.example.storemanager.dto.response.partnerarea.supplier.SupplierDetailResponse;
import org.example.storemanager.service.partnerarea.supplier.SupplierService;
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

    // 3. POST: Tạo mới (Dùng consumes form-data)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CreateSupplierResponse>> create(
            @Valid @ModelAttribute CreateSupplierRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Tạo thành công", service.create(req)));
    }

    // 4. PUT: Cập nhật
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UpdateSupplierResponse>> update(
            @PathVariable Long id,
            @Valid @ModelAttribute CreateSupplierRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công", service.update(id, req)));
    }

    // 5. DELETE: Khóa/Xóa mềm
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Đã khóa thành công", null));
    }
}