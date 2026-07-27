package org.example.storemanager.modules.wms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.wms.dto.LocationTransferDTO;
import org.example.storemanager.modules.wms.service.LocationTransferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wms/location-transfers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class LocationTransferController {

    private final LocationTransferService locationTransferService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LocationTransferDTO.Response>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(locationTransferService.getAllTransfers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LocationTransferDTO.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(locationTransferService.getTransferById(id)));
    }

    @GetMapping("/by-branch/{branchId}")
    public ResponseEntity<ApiResponse<List<LocationTransferDTO.Response>>> getByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(ApiResponse.ok(locationTransferService.getTransfersByBranchId(branchId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LocationTransferDTO.Response>> create(
            @Valid @RequestBody LocationTransferDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(locationTransferService.createTransfer(request)));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<LocationTransferDTO.Response>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(locationTransferService.completeTransfer(id)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<LocationTransferDTO.Response>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(locationTransferService.cancelTransfer(id)));
    }
}
