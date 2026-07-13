package org.example.storemanager.controller.system;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.system.posSession.CreatePosSessionRequest;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.system.posSession.PosSessionResponse;
import org.example.storemanager.service.system.PosSessionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/possessions")
@RequiredArgsConstructor
public class PosSessionController {

    private final PosSessionService posSessionService;

    // ========== TẠO MỚI (MỞ CA) ==========
    @PostMapping("/start")
    @PreAuthorize("@securityEvaluator.hasPermission('system:possession:create')")
    public ResponseEntity<ApiResponse<PosSessionResponse>> startSession(@RequestBody CreatePosSessionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(posSessionService.startSession(request)));
    }

    // ========== KẾT THÚC CA ==========
    @PatchMapping("/{id}/end")
    @PreAuthorize("@securityEvaluator.hasPermission('system:possession:update')")
    public ResponseEntity<ApiResponse<PosSessionResponse>> endSession(
            @PathVariable Long id,
            @RequestParam BigDecimal actualClosingCash) {
        return ResponseEntity.ok(ApiResponse.ok(posSessionService.endSession(id, actualClosingCash)));
    }

    // ========== DANH SÁCH PHÂN TRANG ==========
    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('system:possession:view')")
    public ResponseEntity<ApiResponse<Page<PosSessionResponse>>> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(posSessionService.getAllSessions(pageable)));
    }

    // ========== XEM CHI TIẾT ==========
    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:possession:view')")
    public ResponseEntity<ApiResponse<PosSessionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(posSessionService.getSessionById(id)));
    }
}