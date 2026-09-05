package org.example.storemanager.modules.system.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.system.entity.*;
import org.example.storemanager.modules.system.repository.*;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
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
    @GetMapping({"/config", "/configs"})
    public ResponseEntity<ApiResponse<List<SystemConfig>>> getSystemConfig() {
        return ResponseEntity.ok(ApiResponse.ok(systemConfigRepository.findByIsDeletedFalse()));
    }

    @GetMapping({"/config/{id}", "/configs/{id}"})
    public ResponseEntity<ApiResponse<SystemConfig>> getConfigById(@PathVariable Long id) {
        return systemConfigRepository.findByIdAndIsDeletedFalse(id)
                .map(item -> ResponseEntity.ok(ApiResponse.ok(item)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping({"/config", "/configs"})
    public ResponseEntity<ApiResponse<SystemConfig>> createSystemConfig(@RequestBody SystemConfig req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(systemConfigRepository.save(req)));
    }

    @PutMapping({"/config/{id}", "/configs/{id}"})
    public ResponseEntity<ApiResponse<SystemConfig>> updateSystemConfig(@PathVariable Long id, @RequestBody SystemConfig req) {
        SystemConfig existing = systemConfigRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SystemConfig", "id", id));
        if (req.getConfigKey() != null) existing.setConfigKey(req.getConfigKey());
        if (req.getConfigValue() != null) existing.setConfigValue(req.getConfigValue());
        if (req.getDescription() != null) existing.setDescription(req.getDescription());
        if (req.getCategory() != null) existing.setCategory(req.getCategory());
        if (req.getDataType() != null) existing.setDataType(req.getDataType());
        if (req.getIsEncrypted() != null) existing.setIsEncrypted(req.getIsEncrypted());
        if (req.getRequiresRebootToApply() != null) existing.setRequiresRebootToApply(req.getRequiresRebootToApply());
        if (req.getUpdatedByRole() != null) existing.setUpdatedByRole(req.getUpdatedByRole());
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật cấu hình thành công", systemConfigRepository.save(existing)));
    }

    @DeleteMapping({"/config/{id}", "/configs/{id}"})
    public ResponseEntity<ApiResponse<Void>> deleteSystemConfig(@PathVariable Long id) {
        SystemConfig existing = systemConfigRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SystemConfig", "id", id));
        existing.setIsDeleted(true);
        systemConfigRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Xóa cấu hình thành công", null));
    }

    // --- VAT CONFIG ---
    @GetMapping("/vat")
    public ResponseEntity<ApiResponse<List<EvatConfig>>> getVatConfig() {
        return ResponseEntity.ok(ApiResponse.ok(evatConfigRepository.findByIsDeletedFalse()));
    }

    @GetMapping("/vat/{id}")
    public ResponseEntity<ApiResponse<EvatConfig>> getVatConfigById(@PathVariable Long id) {
        return evatConfigRepository.findByIdAndIsDeletedFalse(id)
                .map(item -> ResponseEntity.ok(ApiResponse.ok(item)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/vat")
    public ResponseEntity<ApiResponse<EvatConfig>> createVatConfig(@RequestBody EvatConfig req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(evatConfigRepository.save(req)));
    }

    @PutMapping("/vat/{id}")
    public ResponseEntity<ApiResponse<EvatConfig>> updateVatConfig(@PathVariable Long id, @RequestBody EvatConfig req) {
        EvatConfig existing = evatConfigRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("EvatConfig", "id", id));
        if (req.getProvider() != null) existing.setProvider(req.getProvider());
        if (req.getTaxCode() != null) existing.setTaxCode(req.getTaxCode());
        if (req.getApiEndpoint() != null) existing.setApiEndpoint(req.getApiEndpoint());
        if (req.getUsername() != null) existing.setUsername(req.getUsername());
        if (req.getPassword() != null) existing.setPassword(req.getPassword());
        if (req.getSymbol() != null) existing.setSymbol(req.getSymbol());
        if (req.getBranch() != null) existing.setBranch(req.getBranch());
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật cấu hình VAT thành công", evatConfigRepository.save(existing)));
    }

    @DeleteMapping("/vat/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVatConfig(@PathVariable Long id) {
        EvatConfig existing = evatConfigRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("EvatConfig", "id", id));
        existing.setIsDeleted(true);
        evatConfigRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Xóa cấu hình VAT thành công", null));
    }

    // --- PRINT TEMPLATES ---
    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<PrintTemplate>>> getPrintTemplates() {
        return ResponseEntity.ok(ApiResponse.ok(printTemplateRepository.findByIsDeletedFalse()));
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<PrintTemplate>> getPrintTemplateById(@PathVariable Long id) {
        return printTemplateRepository.findByIdAndIsDeletedFalse(id)
                .map(item -> ResponseEntity.ok(ApiResponse.ok(item)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/templates")
    public ResponseEntity<ApiResponse<PrintTemplate>> createPrintTemplate(@RequestBody PrintTemplate req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(printTemplateRepository.save(req)));
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<PrintTemplate>> updatePrintTemplate(@PathVariable Long id, @RequestBody PrintTemplate req) {
        PrintTemplate existing = printTemplateRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PrintTemplate", "id", id));
        if (req.getTemplateCode() != null) existing.setTemplateCode(req.getTemplateCode());
        if (req.getTemplateName() != null) existing.setTemplateName(req.getTemplateName());
        if (req.getHtmlContent() != null) existing.setHtmlContent(req.getHtmlContent());
        if (req.getPaperSize() != null) existing.setPaperSize(req.getPaperSize());
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật mẫu in thành công", printTemplateRepository.save(existing)));
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePrintTemplate(@PathVariable Long id) {
        PrintTemplate existing = printTemplateRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PrintTemplate", "id", id));
        existing.setIsDeleted(true);
        printTemplateRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Xóa mẫu in thành công", null));
    }

    // --- SYSTEM SETTINGS ---
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSettings() {
        Map<String, Object> settings = new HashMap<>();
        List<SystemConfig> configs = systemConfigRepository.findByIsDeletedFalse();
        for (SystemConfig c : configs) {
            if (c.getConfigKey() != null) {
                settings.put(c.getConfigKey(), c.getConfigValue());
            }
        }
        if (!settings.containsKey("appName")) settings.put("appName", "RetailHub");
        if (!settings.containsKey("version")) settings.put("version", "1.0.0");
        if (!settings.containsKey("currency")) settings.put("currency", "VND");
        if (!settings.containsKey("timezone")) settings.put("timezone", "Asia/Ho_Chi_Minh");
        return ResponseEntity.ok(ApiResponse.ok(settings));
    }

    @RequestMapping(value = "/settings", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveSettings(@RequestBody Map<String, Object> newSettings) {
        if (newSettings != null) {
            for (Map.Entry<String, Object> entry : newSettings.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    SystemConfig cfg = systemConfigRepository.findByConfigKeyAndIsDeletedFalse(entry.getKey()).orElse(null);
                    if (cfg == null) {
                        cfg = new SystemConfig();
                        cfg.setConfigKey(entry.getKey());
                        cfg.setIsDeleted(false);
                    }
                    cfg.setConfigValue(String.valueOf(entry.getValue()));
                    systemConfigRepository.save(cfg);
                }
            }
        }
        return getSettings();
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getNotifications() {
        return ResponseEntity.ok(ApiResponse.ok(new ArrayList<>()));
    }

    @GetMapping("/errors")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSystemErrors() {
        return ResponseEntity.ok(ApiResponse.ok(new ArrayList<>()));
    }
}
