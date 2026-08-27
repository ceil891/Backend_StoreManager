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
