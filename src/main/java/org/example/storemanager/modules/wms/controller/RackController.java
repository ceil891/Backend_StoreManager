package org.example.storemanager.modules.wms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.wms.dto.RackDTO;
import org.example.storemanager.modules.wms.service.RackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wms/racks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class RackController {

    private final RackService rackService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RackDTO.Response>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(rackService.getAllRacks()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RackDTO.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(rackService.getRackById(id)));
    }

    @GetMapping("/by-area/{areaId}")
    public ResponseEntity<ApiResponse<List<RackDTO.Response>>> getByArea(@PathVariable Long areaId) {
        return ResponseEntity.ok(ApiResponse.ok(rackService.getRacksByAreaId(areaId)));
    }

    @GetMapping("/by-branch/{branchId}")
    public ResponseEntity<ApiResponse<List<RackDTO.Response>>> getByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(ApiResponse.ok(rackService.getRacksByBranchId(branchId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RackDTO.Response>> create(@Valid @RequestBody RackDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(rackService.createRack(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RackDTO.Response>> update(@PathVariable Long id,
                                                                 @Valid @RequestBody RackDTO.Request request) {
        return ResponseEntity.ok(ApiResponse.ok(rackService.updateRack(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        rackService.deleteRack(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
