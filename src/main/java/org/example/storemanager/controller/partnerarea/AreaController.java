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

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(ApiResponse.ok("Thành công", service.getAll(isActive, PageRequest.of(page, size))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> create(@ModelAttribute CreateAreaRequest req) {
        // Bỏ @Valid ở đây để xem request có vào được Service không
        return ResponseEntity.ok(ApiResponse.ok("Tạo thành công", service.create(req)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<?>> updateStatus(@PathVariable Long id) {
        service.updateStatus(id);
        return ResponseEntity.ok(ApiResponse.ok("Đảo trạng thái thành công", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa thành công", null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> update(@PathVariable Long id, @ModelAttribute CreateAreaRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công", service.update(id, req)));
    }
}