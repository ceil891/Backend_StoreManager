package org.example.storemanager.controller.wms;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.system.branch.CreateBranchRequest;
import org.example.storemanager.dto.request.system.branch.UpdateBranchRequest;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.system.branch.BranchResponse;
import org.example.storemanager.dto.response.system.branch.CreateBranchResponse;
import org.example.storemanager.dto.response.system.branch.UpdateBranchResponse;
import org.example.storemanager.dto.response.system.branch.MapBranchResponse;
import org.example.storemanager.dto.wms.WarehouseBinDTO;
import org.example.storemanager.dto.wms.WarehouseZoneDTO;
import org.example.storemanager.dto.wms.AreaDTO;
import org.example.storemanager.dto.wms.RackDTO;
import org.example.storemanager.service.system.BranchService;
import org.example.storemanager.service.wms.AreaService;
import org.example.storemanager.service.wms.RackService;
import org.example.storemanager.service.wms.WarehouseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Disabled to resolve duplicate mapping conflicts with WarehouseController, AreaController, and RackController
// @RestController
// @RequestMapping("/api/v1/wms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class WarehouseApiController {

    private final BranchService branchService;
    private final WarehouseService warehouseService;
    private final AreaService areaService;
    private final RackService rackService;

    // --- WAREHOUSE CRUD ---

    @GetMapping("/warehouses")
    public ResponseEntity<ApiResponse<List<MapBranchResponse>>> getWarehouses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {
        List<MapBranchResponse> list = branchService.getAllBranches(search, isActive, "branchName,asc", false);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/warehouses/{id}")
    public ResponseEntity<ApiResponse<BranchResponse>> getWarehouseById(@PathVariable Long id) {
        BranchResponse response = branchService.getBranchById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/warehouses")
    public ResponseEntity<ApiResponse<CreateBranchResponse>> createWarehouse(@Valid @RequestBody CreateBranchRequest request) {
        CreateBranchResponse response = branchService.createBranch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PutMapping("/warehouses/{id}")
    public ResponseEntity<ApiResponse<UpdateBranchResponse>> updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBranchRequest request) {
        UpdateBranchResponse response = branchService.updateBranch(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật kho thành công", response));
    }

    @PatchMapping("/warehouses/{id}/status")
    public ResponseEntity<ApiResponse<UpdateBranchResponse>> updateWarehouseStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        UpdateBranchResponse response = branchService.updateStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", response));
    }

    @DeleteMapping("/warehouses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWarehouse(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa kho thành công", null));
    }

    // --- ZONE ---

    @GetMapping("/warehouses/{warehouseId}/zones")
    public ResponseEntity<ApiResponse<List<WarehouseZoneDTO>>> getZonesByWarehouse(@PathVariable Long warehouseId) {
        List<WarehouseZoneDTO> zones = warehouseService.getAllZones().stream()
                .filter(z -> warehouseId.equals(z.getBranchId()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(zones));
    }

    @GetMapping("/zones/{id}")
    public ResponseEntity<ApiResponse<WarehouseZoneDTO>> getZoneById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(warehouseService.getZoneById(id)));
    }

    @PostMapping("/warehouses/{warehouseId}/zones")
    public ResponseEntity<ApiResponse<WarehouseZoneDTO>> createZone(
            @PathVariable Long warehouseId,
            @RequestBody WarehouseZoneDTO dto) {
        dto.setBranchId(warehouseId);
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

    // --- AREA ---

    @GetMapping("/zones/{zoneId}/areas")
    public ResponseEntity<ApiResponse<List<AreaDTO.Response>>> getAreasByZone(@PathVariable Long zoneId) {
        return ResponseEntity.ok(ApiResponse.ok(areaService.getAreasByZoneId(zoneId)));
    }

    @GetMapping("/areas/{id}")
    public ResponseEntity<ApiResponse<AreaDTO.Response>> getAreaById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(areaService.getAreaById(id)));
    }

    @PostMapping("/zones/{zoneId}/areas")
    public ResponseEntity<ApiResponse<AreaDTO.Response>> createArea(
            @PathVariable Long zoneId,
            @Valid @RequestBody AreaDTO.Request request) {
        request.setZoneId(zoneId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(areaService.createArea(request)));
    }

    @PutMapping("/areas/{id}")
    public ResponseEntity<ApiResponse<AreaDTO.Response>> updateArea(
            @PathVariable Long id,
            @Valid @RequestBody AreaDTO.Request request) {
        return ResponseEntity.ok(ApiResponse.ok(areaService.updateArea(id, request)));
    }

    @DeleteMapping("/areas/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArea(@PathVariable Long id) {
        areaService.deleteArea(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- RACK ---

    @GetMapping("/areas/{areaId}/racks")
    public ResponseEntity<ApiResponse<List<RackDTO.Response>>> getRacksByArea(@PathVariable Long areaId) {
        return ResponseEntity.ok(ApiResponse.ok(rackService.getRacksByAreaId(areaId)));
    }

    @GetMapping("/racks/{id}")
    public ResponseEntity<ApiResponse<RackDTO.Response>> getRackById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(rackService.getRackById(id)));
    }

    @PostMapping("/areas/{areaId}/racks")
    public ResponseEntity<ApiResponse<RackDTO.Response>> createRack(
            @PathVariable Long areaId,
            @Valid @RequestBody RackDTO.Request request) {
        request.setAreaId(areaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(rackService.createRack(request)));
    }

    @PutMapping("/racks/{id}")
    public ResponseEntity<ApiResponse<RackDTO.Response>> updateRack(
            @PathVariable Long id,
            @Valid @RequestBody RackDTO.Request request) {
        return ResponseEntity.ok(ApiResponse.ok(rackService.updateRack(id, request)));
    }

    @DeleteMapping("/racks/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRack(@PathVariable Long id) {
        rackService.deleteRack(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- BIN ---

    @GetMapping("/racks/{rackId}/bins")
    public ResponseEntity<ApiResponse<List<WarehouseBinDTO>>> getBinsByRack(@PathVariable Long rackId) {
        return ResponseEntity.ok(ApiResponse.ok(warehouseService.getBinsByRackId(rackId)));
    }

    @GetMapping("/bins/{id}")
    public ResponseEntity<ApiResponse<WarehouseBinDTO>> getBinById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(warehouseService.getBinById(id)));
    }

    @PostMapping("/racks/{rackId}/bins")
    public ResponseEntity<ApiResponse<WarehouseBinDTO>> createBin(
            @PathVariable Long rackId,
            @RequestBody WarehouseBinDTO dto) {
        dto.setRackId(rackId);
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
