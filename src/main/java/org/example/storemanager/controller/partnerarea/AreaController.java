package org.example.storemanager.controller.partnerarea;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.partnerarea.area.CreateAreaRequest;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.service.partnerarea.area.AreaService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partnerarea/areas")
@RequiredArgsConstructor
public class AreaController {
    private final AreaService service;

    @GetMapping("/tree")
    public ResponseEntity<?> getTree() {
        return ResponseEntity.ok(service.getTree());
    }

    @PostMapping("/sync-data")
    public ResponseEntity<?> syncData() {
        service.syncDataFromPublicApi();
        return ResponseEntity.ok("Sync thành công!");
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<?>> updateStatus(@PathVariable Long id) {
        service.updateStatus(id);
        return ResponseEntity.ok(ApiResponse.ok("Đảo trạng thái thành công", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        // Logic xóa mềm ở đây
        return ResponseEntity.ok("Đã xóa");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> update(@PathVariable Long id, @ModelAttribute CreateAreaRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công", service.update(id, req)));
    }
}