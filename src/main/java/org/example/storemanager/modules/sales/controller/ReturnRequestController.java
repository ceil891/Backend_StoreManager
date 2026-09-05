package org.example.storemanager.modules.sales.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.sales.entity.SaleReturnRequest;
import org.example.storemanager.modules.sales.repository.SaleReturnRequestRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sales/return-requests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReturnRequestController {

    private final SaleReturnRequestRepository returnRequestRepository;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SaleReturnRequest>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(returnRequestRepository.findByIsDeletedFalseOrderByCreatedAtDesc()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SaleReturnRequest>> getById(@PathVariable Long id) {
        return returnRequestRepository.findByIdAndIsDeletedFalse(id)
                .map(item -> ResponseEntity.ok(ApiResponse.ok(item)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SaleReturnRequest>> create(@RequestBody Map<String, Object> req) {
        String requestCode = req.get("requestCode") != null ? req.get("requestCode").toString() : "RR-" + System.currentTimeMillis();
        String orderCode = req.get("orderCode") != null ? req.get("orderCode").toString() : "";
        Long customerId = null;
        if (req.get("customerId") != null) {
            try {
                String cIdStr = req.get("customerId").toString().trim();
                if (!cIdStr.equalsIgnoreCase("walk-in") && !cIdStr.equalsIgnoreCase("null")) {
                    customerId = Long.valueOf(cIdStr);
                }
            } catch (Exception ignored) {}
        }
        String customerName = req.get("customerName") != null ? req.get("customerName").toString() : "";
        String customerPhone = req.get("customerPhone") != null ? req.get("customerPhone").toString() : "";
        
        int requestedQty = req.get("requestedQty") != null ? Integer.parseInt(req.get("requestedQty").toString()) : 1;
        int returnedQty = req.get("returnedQty") != null ? Integer.parseInt(req.get("returnedQty").toString()) : 0;
        int remainingQty = req.get("remainingQty") != null ? Integer.parseInt(req.get("remainingQty").toString()) : (requestedQty - returnedQty);
        
        BigDecimal refundAmount = req.get("refundAmount") != null ? new BigDecimal(req.get("refundAmount").toString()) : BigDecimal.ZERO;
        String refundMethod = req.get("refundMethod") != null ? req.get("refundMethod").toString() : "CASH";
        String reason = req.get("reason") != null ? req.get("reason").toString() : "";
        String status = req.get("status") != null ? req.get("status").toString() : "PENDING";
        
        String itemsJson = null;
        if (req.containsKey("items") && req.get("items") != null) {
            try {
                itemsJson = objectMapper.writeValueAsString(req.get("items"));
            } catch (Exception ignored) {}
        }

        SaleReturnRequest entity = SaleReturnRequest.builder()
                .requestCode(requestCode)
                .orderCode(orderCode)
                .customerId(customerId)
                .customerName(customerName)
                .customerPhone(customerPhone)
                .requestedQty(requestedQty)
                .returnedQty(returnedQty)
                .remainingQty(remainingQty)
                .refundAmount(refundAmount)
                .refundMethod(refundMethod)
                .reason(reason)
                .requestDate(LocalDateTime.now())
                .status(status)
                .itemsJson(itemsJson)
                .build();
        entity.setIsDeleted(false);

        SaleReturnRequest saved = returnRequestRepository.save(entity);
        return ResponseEntity.status(201).body(ApiResponse.created(saved));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SaleReturnRequest>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return returnRequestRepository.findByIdAndIsDeletedFalse(id)
                .map(item -> {
                    item.setStatus(status);
                    SaleReturnRequest saved = returnRequestRepository.save(item);
                    return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái yêu cầu trả hàng thành công", saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return returnRequestRepository.findByIdAndIsDeletedFalse(id)
                .map(item -> {
                    item.setIsDeleted(true);
                    returnRequestRepository.save(item);
                    return ResponseEntity.ok(ApiResponse.ok("Xóa yêu cầu đổi trả thành công", (Void) null));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
