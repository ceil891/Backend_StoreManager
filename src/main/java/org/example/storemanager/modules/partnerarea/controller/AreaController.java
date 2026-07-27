package org.example.storemanager.modules.partnerarea.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.partnerarea.dto.request.area.CreateAreaRequest;
import org.example.storemanager.shared.dto.response.ApiResponse;
import org.example.storemanager.modules.partnerarea.dto.response.area.AreaListResponse;
import org.example.storemanager.modules.partnerarea.service.area.AreaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // ĐÚNG IMPORT NÀY
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("partnerAreaController")
@RequestMapping("/api/v1/partnerarea/areas")
@RequiredArgsConstructor
public class AreaController {
    private final AreaService service;

    @PostMapping("/sync")
    public ResponseEntity<?> sync() {
        service.syncDataFromPublicApi();
        return ResponseEntity.ok("Sync thành công");
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateAreaRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    // API 3: Dùng Page của Spring Data, KHÔNG ép kiểu
    @GetMapping
    public ResponseEntity<Page<AreaListResponse>> getAll(
            Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(service.getAll(pageable, search, type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AreaListResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AreaListResponse> update(@PathVariable Long id, @RequestBody CreateAreaRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Xóa thành công");
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AreaListResponse>> toggleStatus(@PathVariable Long id) {
        AreaListResponse data = service.toggleStatus(id);
        return ResponseEntity.ok(ApiResponse.<AreaListResponse>builder()
                .success(true)
                .message("Cập nhật trạng thái thành công")
                .data(data) // Trả về ĐỦ thông tin sau khi cập nhật
                .build());
    }

    @GetMapping("/tree")
    public ResponseEntity<List<AreaListResponse>> getTree() {
        return ResponseEntity.ok(service.getTree());
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<List<AreaListResponse>> getChildren(@PathVariable Long parentId) {
        return ResponseEntity.ok(service.getChildren(parentId));
    }

    @GetMapping("/by-type")
    public ResponseEntity<List<AreaListResponse>> getByType(@RequestParam String type) {
        return ResponseEntity.ok(service.getByType(type));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<List<AreaListResponse>> getDropdown() {
        return ResponseEntity.ok(service.getDropdown());
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> exists(@RequestParam String code) {
        return ResponseEntity.ok(service.exists(code));
    }
}