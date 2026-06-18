package org.example.storemanager.controller.partnerarea;

import org.example.storemanager.dto.request.partnerarea.customerdto.CreateCustomerRequest;
import org.example.storemanager.dto.request.partnerarea.customerdto.UpdateCustomerRequest;
import org.example.storemanager.dto.response.ApiResponse;
import org.example.storemanager.service.partnerarea.customer.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/partnerarea/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getAllCustomers(page, size, keyword), "Lấy danh sách thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getCustomerDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerById(id), "Lấy chi tiết thành công"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createCustomer(@RequestBody CreateCustomerRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.success(customerService.createCustomer(request), "Tạo mới thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateCustomer(@PathVariable Long id, @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(customerService.updateCustomer(id, request), "Cập nhật thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteCustomer(@PathVariable Long id) {
        var data = customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success(data, "Xóa thành công"));
    }

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<?>> importCustomers(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(customerService.importCustomers(file), "Import thành công"));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCustomers() {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=customers.xlsx")
                .body(customerService.exportCustomers());
    }

    @GetMapping("/{id}/sales-history")
    public ResponseEntity<ApiResponse<?>> getSalesHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getSalesHistory(id), "Lấy lịch sử thành công"));
    }

    @GetMapping("/{id}/debts")
    public ResponseEntity<ApiResponse<?>> getCustomerDebts(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerDebts(id), "Lấy công nợ thành công"));
    }
}