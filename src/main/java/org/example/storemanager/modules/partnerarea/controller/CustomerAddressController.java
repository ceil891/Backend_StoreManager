package org.example.storemanager.modules.partnerarea.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.partnerarea.dto.request.customerdto.CustomerAddressRequest;
import org.example.storemanager.modules.partnerarea.dto.response.customer.CustomerAddressResponse;
import org.example.storemanager.modules.partnerarea.service.customer.CustomerAddressService;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer-addresses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CustomerAddressController {

    private final CustomerAddressService addressService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerAddressResponse>>> getAddresses(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String phone) {
        List<CustomerAddressResponse> addresses = addressService.getAddresses(customerId, phone);
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách địa chỉ nhận hàng thành công", addresses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> getAddressById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Lấy chi tiết địa chỉ thành công", addressService.getAddressById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> createAddress(@RequestBody CustomerAddressRequest request) {
        CustomerAddressResponse created = addressService.createAddress(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Thêm địa chỉ giao hàng mới thành công", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> updateAddress(
            @PathVariable Long id,
            @RequestBody CustomerAddressRequest request) {
        CustomerAddressResponse updated = addressService.updateAddress(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật địa chỉ thành công", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa địa chỉ thành công", null));
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> setDefaultAddress(
            @PathVariable Long id,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String phone) {
        CustomerAddressResponse response = addressService.setDefaultAddress(id, customerId, phone);
        return ResponseEntity.ok(ApiResponse.ok("Đã đặt làm địa chỉ mặc định", response));
    }
}
