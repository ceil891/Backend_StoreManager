package org.example.storemanager.controller.advancedaccounting;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.advancedaccounting.CreateAccountRequest;
import org.example.storemanager.dto.response.advancedaccounting.ChartOfAccount.AccountDropdownResponse;
import org.example.storemanager.dto.response.advancedaccounting.ChartOfAccount.AccountResponse;
import org.example.storemanager.dto.response.advancedaccounting.ChartOfAccount.AccountDetailResponse;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.service.advancedaccounting.ChartOfAccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounting/accounts")
@RequiredArgsConstructor
public class ChartOfAccountController {

    private final ChartOfAccountService chartOfAccountService;

    // 1. Lấy danh sách (phân trang) - Trả về AccountResponse (Không có isDeleted)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AccountResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(ApiResponse.ok("Thành công",
                chartOfAccountService.getAll(isActive, PageRequest.of(page, size))));
    }

    // 2. Lấy cây tài khoản (Không có isDeleted)
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getTree() {
        return ResponseEntity.ok(ApiResponse.ok("Lấy cây tài khoản thành công", chartOfAccountService.getTree()));
    }

    // 3. Lấy dropdown
    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<AccountDropdownResponse>>> getDropdown() {
        return ResponseEntity.ok(ApiResponse.ok("Lấy dropdown thành công", chartOfAccountService.getDropdown()));
    }

    // 4. Lấy chi tiết theo ID - Trả về AccountDetailResponse (CÓ isDeleted)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDetailResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Thành công", chartOfAccountService.getById(id)));
    }

    // 5. Tạo mới - Trả về AccountResponse (Không có isDeleted)
    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> create(@RequestBody CreateAccountRequest req) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created("Tạo tài khoản thành công", chartOfAccountService.create(req)));
    }

    // 6. Cập nhật - Trả về AccountResponse (Không có isDeleted)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> update(
            @PathVariable Long id,
            @RequestBody CreateAccountRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công", chartOfAccountService.update(id, req)));
    }

    // 7. Bật/Tắt trạng thái - Trả về AccountResponse (Không có isDeleted)
    @PatchMapping("/{id}/isactive")
    public ResponseEntity<ApiResponse<AccountResponse>> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Đã cập nhật trạng thái", chartOfAccountService.toggleActive(id)));
    }

    // 8. Xóa mềm - Trả về AccountDetailResponse (CÓ isDeleted = true)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDetailResponse>> delete(@PathVariable Long id) { // Phải là AccountDetailResponse
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa tài khoản thành công", chartOfAccountService.delete(id)));
    }
}