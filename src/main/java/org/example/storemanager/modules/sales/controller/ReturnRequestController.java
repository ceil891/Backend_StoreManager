package org.example.storemanager.modules.sales.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/v1/sales/return-requests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReturnRequestController {

    private static final List<Map<String, Object>> returnRequests = new CopyOnWriteArrayList<>();

    static {
        Map<String, Object> req1 = new HashMap<>();
        req1.put("id", "1");
        req1.put("requestCode", "RR-2026-0001");
        req1.put("orderCode", "ONLINE-805391");
        req1.put("customerId", "1");
        req1.put("customerName", "Nguyễn Lưu Hoàng");
        req1.put("customerPhone", "0901234567");
        req1.put("requestedQty", 10);
        req1.put("returnedQty", 0);
        req1.put("remainingQty", 10);
        req1.put("reason", "Sản phẩm lỗi màng bao bì");
        req1.put("status", "APPROVED");
        req1.put("refundMethod", "CASH");
        req1.put("requestDate", "2026-08-12");
        returnRequests.add(req1);

        Map<String, Object> req2 = new HashMap<>();
        req2.put("id", "2");
        req2.put("requestCode", "RR-2026-0002");
        req2.put("orderCode", "ORD-POS-2026-818712");
        req2.put("customerId", "2");
        req2.put("customerName", "Trần Văn Nam");
        req2.put("customerPhone", "0988776655");
        req2.put("requestedQty", 5);
        req2.put("returnedQty", 2);
        req2.put("remainingQty", 3);
        req2.put("reason", "Khách mua nhầm Size");
        req2.put("status", "PARTIALLY_RETURNED");
        req2.put("refundMethod", "BANK_TRANSFER");
        req2.put("requestDate", "2026-08-11");
        returnRequests.add(req2);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(returnRequests));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        String id = String.valueOf(System.currentTimeMillis());
        req.put("id", id);
        if (!req.containsKey("status") || req.get("status") == null) {
            req.put("status", "PENDING");
        }
        if (!req.containsKey("returnedQty") || req.get("returnedQty") == null) {
            req.put("returnedQty", 0);
        }
        if (!req.containsKey("remainingQty") || req.get("remainingQty") == null) {
            Object reqQtyObj = req.get("requestedQty");
            int reqQty = reqQtyObj != null ? Integer.parseInt(reqQtyObj.toString()) : 1;
            req.put("remainingQty", reqQty);
        }
        returnRequests.add(0, req);
        return ResponseEntity.status(201).body(ApiResponse.created(req));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateStatus(
            @PathVariable String id,
            @RequestParam String status) {
        for (Map<String, Object> item : returnRequests) {
            if (id.equals(String.valueOf(item.get("id")))) {
                item.put("status", status);
                return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái yêu cầu trả thành công", item));
            }
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        returnRequests.removeIf(item -> id.equals(String.valueOf(item.get("id"))));
        return ResponseEntity.ok(ApiResponse.ok("Xóa yêu cầu thành công", null));
    }
}
