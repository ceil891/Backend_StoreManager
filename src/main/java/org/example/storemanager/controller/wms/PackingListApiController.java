package org.example.storemanager.controller.wms;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.wms.PackingListDTO;
import org.example.storemanager.service.wms.PackingListService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PackingListApiController {

    private final PackingListService packingListService;

    @GetMapping("/packing-lists")
    public ResponseEntity<ApiResponse<List<PackingListDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(packingListService.getAll()));
    }

    @GetMapping("/packing-lists/{id}")
    public ResponseEntity<ApiResponse<PackingListDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(packingListService.getById(id)));
    }

    @PostMapping("/packing-lists")
    public ResponseEntity<ApiResponse<PackingListDTO>> create(@Valid @RequestBody PackingListDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(packingListService.create(dto)));
    }

    @PutMapping("/packing-lists/{id}")
    public ResponseEntity<ApiResponse<PackingListDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody PackingListDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(packingListService.update(id, dto)));
    }

    @DeleteMapping("/packing-lists/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        packingListService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- Items ---

    @GetMapping("/packing-lists/{id}/items")
    public ResponseEntity<ApiResponse<List<PackingListDTO.Item>>> getItems(@PathVariable Long id) {
        PackingListDTO pl = packingListService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(pl != null ? pl.getItems() : List.of()));
    }

    @PostMapping("/packing-lists/{id}/items")
    public ResponseEntity<ApiResponse<PackingListDTO.Item>> addItem(
            @PathVariable Long id,
            @Valid @RequestBody PackingListDTO.Item itemDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(packingListService.addItem(id, itemDto)));
    }

    @PutMapping("/packing-list-items/{id}")
    public ResponseEntity<ApiResponse<PackingListDTO.Item>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody PackingListDTO.Item itemDto) {
        return ResponseEntity.ok(ApiResponse.ok(packingListService.updateItem(id, itemDto)));
    }

    @DeleteMapping("/packing-list-items/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        packingListService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- Actions ---

    @PostMapping("/packing-lists/{id}/start-picking")
    public ResponseEntity<ApiResponse<PackingListDTO>> startPicking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(packingListService.startPicking(id)));
    }

    @PostMapping("/packing-lists/{id}/pick")
    public ResponseEntity<ApiResponse<PackingListDTO>> pick(
            @PathVariable Long id,
            @RequestBody List<PackingListDTO.Item> items) {
        return ResponseEntity.ok(ApiResponse.ok(packingListService.pick(id, items)));
    }

    @PostMapping("/packing-lists/{id}/start-packing")
    public ResponseEntity<ApiResponse<PackingListDTO>> startPacking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(packingListService.startPacking(id)));
    }

    @PostMapping("/packing-lists/{id}/complete")
    public ResponseEntity<ApiResponse<PackingListDTO>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(packingListService.completePacking(id)));
    }

    @PostMapping("/packing-lists/{id}/cancel")
    public ResponseEntity<ApiResponse<PackingListDTO>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(packingListService.cancelPacking(id)));
    }
}
