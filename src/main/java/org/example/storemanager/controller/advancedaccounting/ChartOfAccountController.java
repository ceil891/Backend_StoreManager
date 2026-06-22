package org.example.storemanager.controller.advancedaccounting;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.advancedaccounting.CreateAccountRequest;
import org.example.storemanager.dto.response.advancedaccounting.AccountResponse;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.service.advancedaccounting.ChartOfAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounting/accounts")
@RequiredArgsConstructor
public class ChartOfAccountController {
    private final ChartOfAccountService service;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Lưu ý: Đừng dùng .toString() ở đây, hãy truyền trực tiếp data vào
        return ResponseEntity.ok(ApiResponse.ok("Thành công", service.getAll(page, size)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> create(@RequestBody CreateAccountRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Tạo thành công", service.create(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> update(
            @PathVariable Long id,
            @RequestBody CreateAccountRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công", service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa tài khoản thành công", null));
    }
}