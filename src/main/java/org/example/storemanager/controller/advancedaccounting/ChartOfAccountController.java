package org.example.storemanager.controller.advancedaccounting;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.advancedaccounting.CreateAccountRequest;
import org.example.storemanager.dto.response.advancedaccounting.AccountResponse;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.service.advancedaccounting.ChartOfAccountService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounting/accounts")
@RequiredArgsConstructor
public class ChartOfAccountController {

    private final ChartOfAccountService service;

    // 1. Lấy danh sách (có phân trang và lọc trạng thái)
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean isActive) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok("Thành công", service.getAll(isActive, pageable)));
    }

    // 2. Tạo mới tài khoản
    @PostMapping
    public ResponseEntity<ApiResponse<?>> create(@RequestBody CreateAccountRequest req) {
        return ResponseEntity.status(201)
                .body(ApiResponse.ok("Tạo tài khoản thành công", service.create(req)));
    }

    // 3. Cập nhật tài khoản
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> update(
            @PathVariable Long id,
            @RequestBody CreateAccountRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công", service.update(id, req)));
    }

    // 4. Xóa mềm (khóa tài khoản)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Đã khóa tài khoản thành công", null));
    }
}