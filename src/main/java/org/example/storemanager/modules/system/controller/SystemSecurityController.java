package org.example.storemanager.modules.system.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SystemSecurityController {

    // ========== 1. LỊCH SỬ HOẠT ĐỘNG (AUDIT LOGS) ==========
    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAuditLogs() {
        List<Map<String, Object>> logs = new ArrayList<>();
        Map<String, Object> log1 = new HashMap<>();
        log1.put("id", "log_001");
        log1.put("timestamp", "2026-08-05 08:30:00");
        log1.put("userName", "Quản trị viên");
        log1.put("userEmail", "admin@store.com");
        log1.put("role", "SUPER_ADMIN");
        log1.put("actionType", "UPDATE");
        log1.put("moduleName", "Hệ thống");
        log1.put("pageName", "Phân quyền");
        log1.put("description", "Cập nhật phân quyền vai trò Store Manager");
        log1.put("ipAddress", "192.168.1.100");
        log1.put("status", "SUCCESS");
        logs.add(log1);
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }

    // ========== 2. LỊCH SỬ ĐỔI MẬT KHẨU ==========
    @GetMapping("/password-history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPasswordHistory() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", "pwd_001");
        item.put("userName", "Nguyen Van A");
        item.put("userEmail", "nva@store.com");
        item.put("changedAt", "2026-08-01 14:20:00");
        item.put("changedBy", "Chính chủ");
        item.put("ipAddress", "14.232.110.15");
        item.put("status", "SUCCESS");
        list.add(item);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    // ========== 3. PHIÊN ĐĂNG NHẬP THIẾT BỊ ==========
    @GetMapping("/device-sessions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDeviceSessions() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", "sess_101");
        item.put("userEmail", "admin@store.com");
        item.put("deviceName", "Chrome / Windows 11");
        item.put("ipAddress", "118.69.182.20");
        item.put("lastActive", "2026-08-05 09:15:22");
        item.put("isCurrent", true);
        list.add(item);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @DeleteMapping("/device-sessions/{id}")
    public ResponseEntity<ApiResponse<Void>> revokeDeviceSession(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("Thu hồi phiên đăng nhập thành công", null));
    }

    // ========== 4. LỊCH SỬ LỖI (ERROR LOGS) ==========
    @GetMapping("/error-logs")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getErrorLogs() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", "err_500");
        item.put("timestamp", "2026-08-05 01:29:39");
        item.put("path", "/api/v1/permissions");
        item.put("exceptionClass", "PSQLException");
        item.put("message", "ERROR: function lower(bytea) does not exist");
        item.put("statusCode", 500);
        list.add(item);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    // ========== 5. LUẬT THÔNG BÁO (NOTIFICATION RULES) ==========
    @GetMapping("/notification-rules")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getNotificationRules() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> rule = new HashMap<>();
        rule.put("id", "rule_01");
        rule.put("ruleName", "Cảnh báo tồn kho dưới định mức");
        rule.put("triggerEvent", "LOW_STOCK");
        rule.put("channel", "EMAIL_PUSH");
        rule.put("recipientRole", "STORE_MANAGER");
        rule.put("isActive", true);
        list.add(rule);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/notification-rules")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createNotificationRule(@RequestBody Map<String, Object> req) {
        req.put("id", "rule_" + System.currentTimeMillis());
        return ResponseEntity.status(201).body(ApiResponse.created(req));
    }
}
