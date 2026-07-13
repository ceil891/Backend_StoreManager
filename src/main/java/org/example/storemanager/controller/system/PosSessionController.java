package org.example.storemanager.controller.system;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.system.posSession.CreatePosSessionRequest;
import org.example.storemanager.dto.response.ApiResponse;
import org.example.storemanager.dto.response.system.posSession.PosSessionResponse;
import org.example.storemanager.service.system.PosSessionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/pos-sessions")
@RequiredArgsConstructor
public class PosSessionController {

    private final PosSessionService posSessionService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<PosSessionResponse>> startSession(@RequestBody CreatePosSessionRequest request) {
        // Thêm tham số message: "Thành công"
        return ResponseEntity.ok(ApiResponse.success(posSessionService.startSession(request), "Thành công"));
    }

    @PatchMapping("/{id}/end")
    public ResponseEntity<ApiResponse<PosSessionResponse>> endSession(
            @PathVariable Long id,
            @RequestParam BigDecimal actualClosingCash) {
        return ResponseEntity.ok(ApiResponse.success(posSessionService.endSession(id, actualClosingCash), "Thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PosSessionResponse>>> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(posSessionService.getAllSessions(pageable), "Thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PosSessionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(posSessionService.getSessionById(id), "Thành công"));
    }
}