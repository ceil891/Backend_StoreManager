package org.example.storemanager.controller.wms;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.wms.AreaDTO;
import org.example.storemanager.service.wms.AreaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("wmsAreaController")
@RequestMapping("/api/v1/wms/areas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AreaController {

    private final AreaService areaService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AreaDTO.Response>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(areaService.getAllAreas()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AreaDTO.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(areaService.getAreaById(id)));
    }

    @GetMapping("/by-zone/{zoneId}")
    public ResponseEntity<ApiResponse<List<AreaDTO.Response>>> getByZone(@PathVariable Long zoneId) {
        return ResponseEntity.ok(ApiResponse.ok(areaService.getAreasByZoneId(zoneId)));
    }

    @GetMapping("/by-branch/{branchId}")
    public ResponseEntity<ApiResponse<List<AreaDTO.Response>>> getByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(ApiResponse.ok(areaService.getAreasByBranchId(branchId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AreaDTO.Response>> create(@Valid @RequestBody AreaDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(areaService.createArea(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AreaDTO.Response>> update(@PathVariable Long id,
                                                                 @Valid @RequestBody AreaDTO.Request request) {
        return ResponseEntity.ok(ApiResponse.ok(areaService.updateArea(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        areaService.deleteArea(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
