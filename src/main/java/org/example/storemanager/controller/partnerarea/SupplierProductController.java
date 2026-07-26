package org.example.storemanager.controller.partnerarea;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.partnerarea.SupplierProductRequest;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.partnerarea.SupplierProductResponse;
import org.example.storemanager.service.partnerarea.SupplierProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/partnerarea/supplier-products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SupplierProductController {

    private final SupplierProductService supplierProductService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierProductResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(supplierProductService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(supplierProductService.getById(id)));
    }

    @GetMapping("/by-supplier/{supplierId}")
    public ResponseEntity<ApiResponse<List<SupplierProductResponse>>> getBySupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(ApiResponse.ok(supplierProductService.getBySupplierId(supplierId)));
    }

    @GetMapping("/by-product/{productId}")
    public ResponseEntity<ApiResponse<List<SupplierProductResponse>>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.ok(supplierProductService.getByProductId(productId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SupplierProductResponse>> create(
            @Valid @RequestBody SupplierProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(supplierProductService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierProductResponse>> update(@PathVariable Long id,
                                                                        @Valid @RequestBody SupplierProductRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(supplierProductService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        supplierProductService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SupplierProductResponse>> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(supplierProductService.toggleStatus(id)));
    }

    @PatchMapping("/{id}/preferred")
    public ResponseEntity<ApiResponse<SupplierProductResponse>> setPreferred(
            @PathVariable Long id, @RequestParam boolean value) {
        return ResponseEntity.ok(ApiResponse.ok(supplierProductService.setPreferred(id, value)));
    }
}
