package org.example.storemanager.modules.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.inventory.entity.SupplierStorage;
import org.example.storemanager.modules.inventory.entity.SupplierWarehouse;
import org.example.storemanager.modules.inventory.repository.SupplierStorageRepository;
import org.example.storemanager.modules.inventory.repository.SupplierWarehouseRepository;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/inventories", "/api/v1/inventory"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SupplierWarehouseApiController {

    private final SupplierWarehouseRepository supplierWarehouseRepository;
    private final SupplierStorageRepository supplierStorageRepository;

    // =========================================================================
    // --- SUPPLIER WAREHOUSES ---
    // =========================================================================

    @GetMapping("/supplier-warehouses")
    public ResponseEntity<ApiResponse<List<SupplierWarehouse>>> getAllWarehouses() {
        return ResponseEntity.ok(ApiResponse.ok(supplierWarehouseRepository.findByIsDeletedFalse()));
    }

    @GetMapping("/supplier-warehouses/{id}")
    public ResponseEntity<ApiResponse<SupplierWarehouse>> getWarehouseById(@PathVariable Long id) {
        SupplierWarehouse item = supplierWarehouseRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierWarehouse", "id", id));
        return ResponseEntity.ok(ApiResponse.ok(item));
    }

    @PostMapping("/supplier-warehouses")
    public ResponseEntity<ApiResponse<SupplierWarehouse>> createWarehouse(@RequestBody SupplierWarehouse item) {
        item.setIsDeleted(false);
        if (item.getStatus() == null || item.getStatus().isBlank()) {
            item.setStatus("HOAT_DONG");
        }
        SupplierWarehouse saved = supplierWarehouseRepository.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(saved));
    }

    @PutMapping("/supplier-warehouses/{id}")
    public ResponseEntity<ApiResponse<SupplierWarehouse>> updateWarehouse(@PathVariable Long id, @RequestBody SupplierWarehouse req) {
        SupplierWarehouse existing = supplierWarehouseRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierWarehouse", "id", id));

        if (req.getWarehouseCode() != null) existing.setWarehouseCode(req.getWarehouseCode());
        if (req.getWarehouseName() != null) existing.setWarehouseName(req.getWarehouseName());
        if (req.getSupplierName() != null) existing.setSupplierName(req.getSupplierName());
        if (req.getAddress() != null) existing.setAddress(req.getAddress());
        if (req.getWarehouseType() != null) existing.setWarehouseType(req.getWarehouseType());
        if (req.getCapacity() != null) existing.setCapacity(req.getCapacity());
        if (req.getCapacityUnit() != null) existing.setCapacityUnit(req.getCapacityUnit());
        if (req.getManagerName() != null) existing.setManagerName(req.getManagerName());
        if (req.getManagerPhone() != null) existing.setManagerPhone(req.getManagerPhone());
        if (req.getManagerEmail() != null) existing.setManagerEmail(req.getManagerEmail());
        if (req.getContactPerson() != null) existing.setContactPerson(req.getContactPerson());
        if (req.getPhone() != null) existing.setPhone(req.getPhone());
        if (req.getLoadingContactPhone() != null) existing.setLoadingContactPhone(req.getLoadingContactPhone());
        if (req.getOperatingHours() != null) existing.setOperatingHours(req.getOperatingHours());
        if (req.getOperatingDays() != null) existing.setOperatingDays(req.getOperatingDays());
        if (req.getStorageConditions() != null) existing.setStorageConditions(req.getStorageConditions());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());
        if (req.getNotes() != null) existing.setNotes(req.getNotes());
        if (req.getInternalNotes() != null) existing.setInternalNotes(req.getInternalNotes());

        SupplierWarehouse updated = supplierWarehouseRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật kho nhà cung cấp thành công", updated));
    }

    @DeleteMapping("/supplier-warehouses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWarehouse(@PathVariable Long id) {
        SupplierWarehouse existing = supplierWarehouseRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierWarehouse", "id", id));
        existing.setIsDeleted(true);
        supplierWarehouseRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Xóa kho nhà cung cấp thành công", null));
    }

    // =========================================================================
    // --- SUPPLIER STORAGES ---
    // =========================================================================

    @GetMapping("/supplier-storages")
    public ResponseEntity<ApiResponse<List<SupplierStorage>>> getAllStorages() {
        return ResponseEntity.ok(ApiResponse.ok(supplierStorageRepository.findByIsDeletedFalse()));
    }

    @GetMapping("/supplier-storages/{id}")
    public ResponseEntity<ApiResponse<SupplierStorage>> getStorageById(@PathVariable Long id) {
        SupplierStorage item = supplierStorageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierStorage", "id", id));
        return ResponseEntity.ok(ApiResponse.ok(item));
    }

    @PostMapping("/supplier-storages")
    public ResponseEntity<ApiResponse<SupplierStorage>> createStorage(@RequestBody SupplierStorage item) {
        item.setIsDeleted(false);
        if (item.getStatus() == null || item.getStatus().isBlank()) {
            item.setStatus("TRONG");
        }
        SupplierStorage saved = supplierStorageRepository.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(saved));
    }

    @PutMapping("/supplier-storages/{id}")
    public ResponseEntity<ApiResponse<SupplierStorage>> updateStorage(@PathVariable Long id, @RequestBody SupplierStorage req) {
        SupplierStorage existing = supplierStorageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierStorage", "id", id));

        if (req.getStorageCode() != null) existing.setStorageCode(req.getStorageCode());
        if (req.getStorageName() != null) existing.setStorageName(req.getStorageName());
        if (req.getWarehouseName() != null) existing.setWarehouseName(req.getWarehouseName());
        if (req.getStorageType() != null) existing.setStorageType(req.getStorageType());
        if (req.getAreaType() != null) existing.setAreaType(req.getAreaType());
        if (req.getZoneType() != null) existing.setZoneType(req.getZoneType());
        if (req.getPutawayRule() != null) existing.setPutawayRule(req.getPutawayRule());
        if (req.getCapacity() != null) existing.setCapacity(req.getCapacity());
        if (req.getCapacityPallets() != null) existing.setCapacityPallets(req.getCapacityPallets());
        if (req.getUsedPallets() != null) existing.setUsedPallets(req.getUsedPallets());
        if (req.getCurrentUsage() != null) existing.setCurrentUsage(req.getCurrentUsage());
        if (req.getCapacityUnit() != null) existing.setCapacityUnit(req.getCapacityUnit());
        if (req.getOperatingHours() != null) existing.setOperatingHours(req.getOperatingHours());
        if (req.getAllowImport() != null) existing.setAllowImport(req.getAllowImport());
        if (req.getAllowExport() != null) existing.setAllowExport(req.getAllowExport());
        if (req.getAllowTransfer() != null) existing.setAllowTransfer(req.getAllowTransfer());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());
        if (req.getNotes() != null) existing.setNotes(req.getNotes());

        SupplierStorage updated = supplierStorageRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật bãi kho đối tác thành công", updated));
    }

    @DeleteMapping("/supplier-storages/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStorage(@PathVariable Long id) {
        SupplierStorage existing = supplierStorageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierStorage", "id", id));
        existing.setIsDeleted(true);
        supplierStorageRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Xóa bãi kho đối tác thành công", null));
    }
}
