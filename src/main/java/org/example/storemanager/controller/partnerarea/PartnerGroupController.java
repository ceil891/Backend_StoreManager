package org.example.storemanager.controller.partnerarea;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.partnerarea.partnergroup.PartnerGroupRequest;
import org.example.storemanager.dto.response.partnerarea.partnergroup.*; // Import tất cả DTO response
import org.example.storemanager.service.partnerarea.partnergroup.PartnerGroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partnerarea/groups")
@RequiredArgsConstructor
public class PartnerGroupController {

    private final PartnerGroupService service;

    @PostMapping
    public ResponseEntity<CreatePartnerGroupResponse> create(@Valid @RequestBody PartnerGroupRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdatePartnerGroupResponse> update(@PathVariable Long id, @Valid @RequestBody PartnerGroupRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeletePartnerGroupResponse> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.delete(id)); // Trả về DeletePartnerGroupResponse thay vì String
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartnerGroupDetailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PartnerGroupListResponse>> getAll(
            Pageable pageable,
            @RequestParam(required = false) String groupCode,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(service.findWithFilter(pageable, groupCode, type, search, isActive));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<UpdatePartnerGroupResponse> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(service.toggleStatus(id));
    }
}