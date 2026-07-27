package org.example.storemanager.modules.wms.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.wms.dto.WarehouseBinDTO;
import org.example.storemanager.modules.wms.dto.WarehouseZoneDTO;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.wms.service.WarehouseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class WarehouseController {

    private final WarehouseService warehouseService;

    // --- Zone Endpoints ---

    @GetMapping("/zones")
    public ResponseEntity<ApiResponse<List<WarehouseZoneDTO>>> getAllZones() {
        return ResponseEntity.ok(ApiResponse.ok(warehouseService.getAllZones()));
    }

    @GetMapping("/zones/{id}")
    public ResponseEntity<ApiResponse<WarehouseZoneDTO>> getZoneById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(warehouseService.getZoneById(id)));
    }

    @PostMapping("/zones")
    public ResponseEntity<ApiResponse<WarehouseZoneDTO>> createZone(@RequestBody WarehouseZoneDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(warehouseService.createZone(dto)));
    }

    @PutMapping("/zones/{id}")
    public ResponseEntity<ApiResponse<WarehouseZoneDTO>> updateZone(@PathVariable Long id, @RequestBody WarehouseZoneDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(warehouseService.updateZone(id, dto)));
    }

    @DeleteMapping("/zones/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteZone(@PathVariable Long id) {
        warehouseService.deleteZone(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- Bin Endpoints ---

    @GetMapping("/bins")
    public ResponseEntity<ApiResponse<List<WarehouseBinDTO>>> getAllBins() {
        return ResponseEntity.ok(ApiResponse.ok(warehouseService.getAllBins()));
    }

    @GetMapping("/bins/{id}")
    public ResponseEntity<ApiResponse<WarehouseBinDTO>> getBinById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(warehouseService.getBinById(id)));
    }

    @GetMapping("/zones/{zoneId}/bins")
    public ResponseEntity<ApiResponse<List<WarehouseBinDTO>>> getBinsByZoneId(@PathVariable Long zoneId) {
        return ResponseEntity.ok(ApiResponse.ok(warehouseService.getBinsByZoneId(zoneId)));
    }

    @PostMapping("/bins")
    public ResponseEntity<ApiResponse<WarehouseBinDTO>> createBin(@RequestBody WarehouseBinDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(warehouseService.createBin(dto)));
    }

    @PutMapping("/bins/{id}")
    public ResponseEntity<ApiResponse<WarehouseBinDTO>> updateBin(@PathVariable Long id, @RequestBody WarehouseBinDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(warehouseService.updateBin(id, dto)));
    }

    @DeleteMapping("/bins/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBin(@PathVariable Long id) {
        warehouseService.deleteBin(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
