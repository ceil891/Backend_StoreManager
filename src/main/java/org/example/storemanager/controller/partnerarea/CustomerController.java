package org.example.storemanager.controller.partnerarea;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.partnerarea.customerdto.CreateCustomerRequest;
import org.example.storemanager.dto.request.partnerarea.customerdto.UpdateCustomerRequest;
import org.example.storemanager.dto.response.ApiResponse;
import org.example.storemanager.service.partnerarea.customer.CustomerService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/partnerarea/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> createCustomer(@ModelAttribute CreateCustomerRequest req) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success(customerService.createCustomer(req), "Tạo mới thành công"));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> updateCustomer(@PathVariable Long id, @ModelAttribute UpdateCustomerRequest req) {
        return ResponseEntity.ok(ApiResponse.success(customerService.updateCustomer(id, req), "Cập nhật thành công"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<?>> updateStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        return ResponseEntity.ok(ApiResponse.success(customerService.updateStatus(id, isActive), "Cập nhật trạng thái thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllCustomers(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getAllCustomers(page, size, keyword), "Lấy danh sách thành công"));
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
}