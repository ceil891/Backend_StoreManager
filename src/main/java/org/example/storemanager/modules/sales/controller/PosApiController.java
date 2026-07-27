package org.example.storemanager.modules.sales.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.system.entity.PosSession;
import org.example.storemanager.modules.system.repository.PosSessionRepository;
import org.example.storemanager.modules.finance.entity.PaymentMethod;
import org.example.storemanager.modules.finance.repository.PaymentMethodRepository;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/pos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PosApiController {

    private final PosSessionRepository posSessionRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    // --- POS SESSIONS ---
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<PosSession>>> getAllSessions() {
        return ResponseEntity.ok(ApiResponse.ok(posSessionRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<PosSession>> createSession(@RequestBody PosSession req) {
        req.setIsDeleted(false);
        req.setStartTime(LocalDateTime.now());
        req.setStatus("OPEN");
        return ResponseEntity.status(201).body(ApiResponse.created(posSessionRepository.save(req)));
    }

    @PutMapping("/sessions/{id}/close")
    public ResponseEntity<ApiResponse<PosSession>> closeSession(
            @PathVariable Long id,
            @RequestParam java.math.BigDecimal actualClosingCash) {
        PosSession existing = posSessionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PosSession", "id", id));
        existing.setEndTime(LocalDateTime.now());
        existing.setActualClosingCash(actualClosingCash);
        existing.setStatus("CLOSED");
        return ResponseEntity.ok(ApiResponse.ok("Đóng phiên bán hàng thành công", posSessionRepository.save(existing)));
    }

    // --- PAYMENT METHODS ---
    @GetMapping("/payment-methods")
    public ResponseEntity<ApiResponse<List<PaymentMethod>>> getPosPaymentMethods() {
        return ResponseEntity.ok(ApiResponse.ok(paymentMethodRepository.findByIsDeletedFalse()));
    }

    // --- MOCKED / PLACEHOLDER ENDPOINTS FOR POS TERMINALS ---
    @GetMapping("/terminals")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPosTerminals() {
        List<Map<String, Object>> mock = new ArrayList<>();
        Map<String, Object> m = new HashMap<>();
        m.put("id", 1L);
        m.put("terminalCode", "POS-001");
        m.put("terminalName", "Máy bán hàng POS 01");
        m.put("isActive", true);
        mock.add(m);
        return ResponseEntity.ok(ApiResponse.ok(mock));
    }
}
