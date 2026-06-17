package org.example.storemanager.controller.partnerarea;

import org.example.storemanager.dto.request.partnerarea.customerdto.CreateCustomerRequest;
import org.example.storemanager.dto.request.partnerarea.customerdto.UpdateCustomerRequest;

import org.example.storemanager.service.partnerarea.CustomerService;
// Import các DTO request/response của bạn...
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/partnerarea/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // 1. GET /: Danh sách
    @GetMapping
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long groupId) {
        return ResponseEntity.ok(customerService.getAllCustomers(page, size, keyword));
    }

    // 2. GET /{id}: Xem chi tiết
    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerDetail(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    // 3. POST /: Thêm mới
    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody CreateCustomerRequest request) {
        return ResponseEntity.ok(customerService.createCustomer(request));
    }

    // 4. PUT /{id}: Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    // 5. DELETE /{id}: Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok("Deleted successfully");
    }

    // 6. POST /import: Import Excel
    @PostMapping("/import")
    public ResponseEntity<?> importCustomers(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(customerService.importCustomers(file));
    }

    // 7. GET /export: Xuất Excel
    @GetMapping("/export")
    public ResponseEntity<?> exportCustomers() {
        return ResponseEntity.ok(customerService.exportCustomers());
    }

    // 8. GET /{id}/sales-history: Lịch sử mua hàng
    @GetMapping("/{id}/sales-history")
    public ResponseEntity<?> getSalesHistory(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getSalesHistory(id));
    }

    // 9. GET /{id}/debts: Công nợ
    @GetMapping("/{id}/debts")
    public ResponseEntity<?> getCustomerDebts(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerDebts(id));
    }
}