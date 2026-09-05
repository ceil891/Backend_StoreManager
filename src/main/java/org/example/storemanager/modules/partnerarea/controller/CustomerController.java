package org.example.storemanager.modules.partnerarea.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.partnerarea.dto.request.customerdto.CreateCustomerRequest;
import org.example.storemanager.modules.partnerarea.dto.request.customerdto.UpdateCustomerRequest;
import org.example.storemanager.shared.dto.response.ApiResponse;
import org.example.storemanager.modules.partnerarea.service.customer.CustomerService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/partnerarea/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> createCustomer(@Valid @ModelAttribute CreateCustomerRequest req) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success(customerService.createCustomer(req), "Tạo mới thành công"));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<?>> createCustomerJson(@Valid @RequestBody CreateCustomerRequest req) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success(customerService.createCustomer(req), "Tạo mới thành công"));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> updateCustomer(@PathVariable Long id, @Valid @ModelAttribute UpdateCustomerRequest req) {
        return ResponseEntity.ok(ApiResponse.success(customerService.updateCustomer(id, req), "Cập nhật thành công"));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<?>> updateCustomerJson(@PathVariable Long id, @Valid @RequestBody UpdateCustomerRequest req) {
        return ResponseEntity.ok(ApiResponse.success(customerService.updateCustomer(id, req), "Cập nhật thành công"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<?>> updateStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        return ResponseEntity.ok(ApiResponse.success(customerService.updateStatus(id, isActive), "Cập nhật trạng thái thành công"));
    }

    @PatchMapping("/{id}/credit-block")
    public ResponseEntity<ApiResponse<?>> toggleCreditBlock(@PathVariable Long id, @RequestParam(required = false) Boolean blocked) {
        return ResponseEntity.ok(ApiResponse.success(customerService.toggleCreditBlock(id, blocked), "Cập nhật trạng thái khóa công nợ thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String search) {

        String query = (keyword != null && !keyword.isBlank()) ? keyword : search;
        return ResponseEntity.ok(ApiResponse.success(
                customerService.getAllCustomers(page, size, isActive, query),
                "Lấy danh sách thành công"
        ));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getCustomerDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerById(id), "Lấy chi tiết thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.deleteCustomer(id), "Xóa thành công"));
    }

    @GetMapping("/{id}/debts")
    public ResponseEntity<?> getDebts(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerDebts(id));
    }

    @GetMapping("/{id}/sales-history")
    public ResponseEntity<?> getSalesHistory(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getSalesHistory(id));
    }

    @PutMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @PathVariable Long id,
            @RequestParam(required = false) String newPassword) {
        customerService.resetCustomerPassword(id, newPassword);
        return ResponseEntity.ok(ApiResponse.success(null, "Cấp lại mật khẩu khách hàng thành công! Khách hàng sẽ phải đổi mật khẩu ở lần đăng nhập tiếp theo."));
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        customerService.changeCustomerPassword(id, oldPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.success(null, "Đổi mật khẩu thành công!"));
    }
}