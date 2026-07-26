package org.example.storemanager.controller.wms;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.wms.LocationTransferDTO;
import org.example.storemanager.service.wms.LocationTransferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Disabled to resolve duplicate mapping conflict with LocationTransferController
// @RestController
// @RequestMapping("/api/v1/wms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class LocationTransferRequestApiController {

    private final LocationTransferService locationTransferService;

    @GetMapping("/location-transfers")
    public ResponseEntity<ApiResponse<List<LocationTransferDTO.Response>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(locationTransferService.getAllTransfers()));
    }

    @GetMapping("/location-transfers/{id}")
    public ResponseEntity<ApiResponse<LocationTransferDTO.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(locationTransferService.getTransferById(id)));
    }

    @GetMapping("/location-transfers/by-branch/{branchId}")
    public ResponseEntity<ApiResponse<List<LocationTransferDTO.Response>>> getByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(ApiResponse.ok(locationTransferService.getTransfersByBranchId(branchId)));
    }

    @PostMapping("/location-transfers")
    public ResponseEntity<ApiResponse<LocationTransferDTO.Response>> create(
            @Valid @RequestBody LocationTransferDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(locationTransferService.createTransfer(request)));
    }

    @PutMapping("/location-transfers/{id}")
    public ResponseEntity<ApiResponse<LocationTransferDTO.Response>> update(
            @PathVariable Long id,
            @Valid @RequestBody LocationTransferDTO.Request request) {
        return ResponseEntity.ok(ApiResponse.ok(locationTransferService.updateTransfer(id, request)));
    }

    @DeleteMapping("/location-transfers/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        locationTransferService.deleteTransfer(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- Actions ---

    @PostMapping("/location-transfers/{id}/submit")
    public ResponseEntity<ApiResponse<LocationTransferDTO.Response>> submit(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(locationTransferService.submitTransfer(id)));
    }

    @PostMapping("/location-transfers/{id}/approve")
    public ResponseEntity<ApiResponse<LocationTransferDTO.Response>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(locationTransferService.approveTransfer(id)));
    }

    @PostMapping("/location-transfers/{id}/execute")
    public ResponseEntity<ApiResponse<LocationTransferDTO.Response>> execute(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(locationTransferService.executeTransfer(id)));
    }

    @PostMapping("/location-transfers/{id}/cancel")
    public ResponseEntity<ApiResponse<LocationTransferDTO.Response>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(locationTransferService.cancelTransfer(id)));
    }
}
