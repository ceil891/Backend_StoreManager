package org.example.storemanager.modules.system.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.system.entity.*;
import org.example.storemanager.modules.system.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SystemConfigController {

    private final SystemConfigRepository systemConfigRepository;
    private final EvatConfigRepository evatConfigRepository;
    private final PrintTemplateRepository printTemplateRepository;

    // --- SYSTEM CONFIG ---
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<List<SystemConfig>>> getSystemConfig() {
        return ResponseEntity.ok(ApiResponse.ok(systemConfigRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/config")
    public ResponseEntity<ApiResponse<SystemConfig>> createSystemConfig(@RequestBody SystemConfig req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(systemConfigRepository.save(req)));
    }

    // --- VAT CONFIG ---
    @GetMapping("/vat")
    public ResponseEntity<ApiResponse<List<EvatConfig>>> getVatConfig() {
        return ResponseEntity.ok(ApiResponse.ok(evatConfigRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/vat")
    public ResponseEntity<ApiResponse<EvatConfig>> createVatConfig(@RequestBody EvatConfig req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(evatConfigRepository.save(req)));
    }

    // --- PRINT TEMPLATES ---
    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<PrintTemplate>>> getPrintTemplates() {
        return ResponseEntity.ok(ApiResponse.ok(printTemplateRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/templates")
    public ResponseEntity<ApiResponse<PrintTemplate>> createPrintTemplate(@RequestBody PrintTemplate req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(printTemplateRepository.save(req)));
    }

    // --- MOCKED / PLACEHOLDER ENDPOINTS FOR SYSTEM SETTINGS PAGES ---
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSettings() {
        Map<String, Object> mock = new HashMap<>();
        mock.put("appName", "RetailHub");
        mock.put("version", "1.0.0");
        mock.put("currency", "VND");
        mock.put("timezone", "Asia/Ho_Chi_Minh");
        return ResponseEntity.ok(ApiResponse.ok(mock));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getNotifications() {
        return ResponseEntity.ok(ApiResponse.ok(new ArrayList<>()));
    }

    @GetMapping("/errors")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSystemErrors() {
        return ResponseEntity.ok(ApiResponse.ok(new ArrayList<>()));
    }

    @GetMapping("/banners")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBanners() {
        return ResponseEntity.ok(ApiResponse.ok(new ArrayList<>()));
    }

    @GetMapping("/device-sessions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDeviceSessions() {
        return ResponseEntity.ok(ApiResponse.ok(new ArrayList<>()));
    }

    @GetMapping("/password-history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPasswordHistory() {
        return ResponseEntity.ok(ApiResponse.ok(new ArrayList<>()));
    }
}
