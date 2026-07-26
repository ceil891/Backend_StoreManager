package org.example.storemanager.controller.wms;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.wms.DeliveryNoteDTO;
import org.example.storemanager.service.wms.DeliveryNoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/wms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DeliveryNoteApiController {

    private final DeliveryNoteService deliveryNoteService;

    @GetMapping("/delivery-notes")
    public ResponseEntity<ApiResponse<List<DeliveryNoteDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(deliveryNoteService.getAll()));
    }

    @GetMapping("/delivery-notes/{id}")
    public ResponseEntity<ApiResponse<DeliveryNoteDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(deliveryNoteService.getById(id)));
    }

    @PostMapping("/delivery-notes")
    public ResponseEntity<ApiResponse<DeliveryNoteDTO>> create(@Valid @RequestBody DeliveryNoteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(deliveryNoteService.create(dto)));
    }

    @PutMapping("/delivery-notes/{id}")
    public ResponseEntity<ApiResponse<DeliveryNoteDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryNoteDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(deliveryNoteService.update(id, dto)));
    }

    @DeleteMapping("/delivery-notes/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        deliveryNoteService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- Actions ---

    @PatchMapping("/delivery-notes/{id}/assign-carrier")
    public ResponseEntity<ApiResponse<DeliveryNoteDTO>> assignCarrier(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String carrierName = body.get("carrierName");
        String trackingNumber = body.get("trackingNumber");
        return ResponseEntity.ok(ApiResponse.ok(deliveryNoteService.assignCarrier(id, carrierName, trackingNumber)));
    }

    @PostMapping("/delivery-notes/{id}/dispatch")
    public ResponseEntity<ApiResponse<DeliveryNoteDTO>> dispatch(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(deliveryNoteService.dispatch(id)));
    }

    @PostMapping("/delivery-notes/{id}/in-transit")
    public ResponseEntity<ApiResponse<DeliveryNoteDTO>> inTransit(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(deliveryNoteService.inTransit(id)));
    }

    @PostMapping("/delivery-notes/{id}/deliver")
    public ResponseEntity<ApiResponse<DeliveryNoteDTO>> deliver(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String recipientName = body.get("recipientName");
        return ResponseEntity.ok(ApiResponse.ok(deliveryNoteService.deliver(id, recipientName)));
    }

    @PostMapping("/delivery-notes/{id}/failed")
    public ResponseEntity<ApiResponse<DeliveryNoteDTO>> failed(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String failureReason = body.get("failureReason");
        return ResponseEntity.ok(ApiResponse.ok(deliveryNoteService.failed(id, failureReason)));
    }

    @PostMapping("/delivery-notes/{id}/cancel")
    public ResponseEntity<ApiResponse<DeliveryNoteDTO>> cancel(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String cancelReason = body.get("cancelReason");
        return ResponseEntity.ok(ApiResponse.ok(deliveryNoteService.cancel(id, cancelReason)));
    }
}
