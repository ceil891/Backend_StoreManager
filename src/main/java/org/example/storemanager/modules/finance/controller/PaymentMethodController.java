package org.example.storemanager.modules.finance.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.finance.entity.PaymentMethod;
import org.example.storemanager.modules.finance.entity.PaymentMethodBranch;
import org.example.storemanager.modules.finance.repository.PaymentMethodBranchRepository;
import org.example.storemanager.modules.finance.repository.PaymentMethodRepository;
import org.example.storemanager.modules.sales.repository.SaleOrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PaymentMethodController {

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMethodBranchRepository paymentMethodBranchRepository;
    private final SaleOrderRepository saleOrderRepository;

    /** Đính kèm branchIds và ytdTotal vào mỗi bản ghi PTTT */
    private Map<String, Object> enrichMethod(PaymentMethod pm) {
        int currentYear = LocalDate.now().getYear();
        List<Long> branchIds = paymentMethodBranchRepository.findBranchIdsByPaymentMethodId(pm.getId());
        Double ytd = saleOrderRepository.sumYtdByPaymentMethodCode(pm.getMethodCode(), currentYear);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", pm.getId());
        map.put("methodCode", pm.getMethodCode());
        map.put("methodName", pm.getMethodName());
        map.put("type", pm.getType());
        map.put("providerType", pm.getProviderType() != null ? pm.getProviderType() : pm.getType());
        map.put("processingFeePct", pm.getProcessingFeePct());
        map.put("fixedFeeUsd", pm.getFixedFeeUsd());
        map.put("settlementTime", pm.getSettlementTime());
        map.put("status", pm.getStatus());
        map.put("sortOrder", pm.getSortOrder());
        map.put("logoUrl", pm.getLogoUrl());
        map.put("bankName", pm.getBankName());
        map.put("bankAccount", pm.getBankAccount());
        map.put("bankAccountName", pm.getBankAccountName());
        map.put("transferSyntax", pm.getTransferSyntax());
        map.put("merchantId", pm.getMerchantId());
        map.put("apiKey", pm.getApiKey());
        map.put("secretKey", pm.getSecretKey());
        map.put("checksumKey", pm.getChecksumKey());
        map.put("allowPos", pm.getAllowPos());
        map.put("allowOnline", pm.getAllowOnline());
        map.put("applyToAllBranches", pm.getApplyToAllBranches() != null ? pm.getApplyToAllBranches() : true);
        map.put("currency", pm.getCurrency());
        map.put("configuredGateways", pm.getConfiguredGateways() != null ? pm.getConfiguredGateways() : "");
        map.put("totalVolumeUsd", ytd != null ? ytd : 0.0);   // backward compat alias
        map.put("ytdTotal", ytd != null ? ytd : 0.0);
        map.put("branchIds", branchIds.stream().map(String::valueOf).collect(Collectors.toList()));
        map.put("supportedCurrencies", List.of("VND"));
        return map;
    }

    // GET /api/v1/payment-methods
    @GetMapping("/payment-methods")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllMethods() {
        List<Map<String, Object>> result = paymentMethodRepository.findByIsDeletedFalse()
                .stream().map(this::enrichMethod).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // GET /api/v1/pos/payment-methods/active?branchId={id}
    @GetMapping("/pos/payment-methods/active")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getActiveMethods(
            @RequestParam(value = "branchId", required = false) Long branchId) {
        List<PaymentMethod> methods;
        if (branchId != null) {
            methods = paymentMethodRepository.findActiveByBranchId(branchId);
        } else {
            methods = paymentMethodRepository.findByIsDeletedFalseAndStatusOrderBySortOrderAsc("ACTIVE");
        }
        List<Map<String, Object>> result = methods.stream().map(this::enrichMethod).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // POST /api/v1/payment-methods
    @PostMapping("/payment-methods")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> createMethod(@RequestBody Map<String, Object> req) {
        PaymentMethod pm = buildFromRequest(new PaymentMethod(), req);
        pm.setIsDeleted(false);
        if (pm.getStatus() == null) pm.setStatus("ACTIVE");
        if (pm.getSortOrder() == null) pm.setSortOrder(0);
        PaymentMethod saved = paymentMethodRepository.save(pm);
        // Save branch mappings
        saveBranchMappings(saved.getId(), req);
        return ResponseEntity.status(201).body(ApiResponse.created(enrichMethod(saved)));
    }

    // PUT /api/v1/payment-methods/{id}
    @PutMapping("/payment-methods/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateMethod(
            @PathVariable Long id, @RequestBody Map<String, Object> req) {
        PaymentMethod existing = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức thanh toán ID: " + id));
        buildFromRequest(existing, req);
        existing.setIsDeleted(false);
        PaymentMethod saved = paymentMethodRepository.save(existing);
        // Replace branch mappings
        paymentMethodBranchRepository.deleteByPaymentMethodId(id);
        saveBranchMappings(id, req);
        return ResponseEntity.ok(ApiResponse.ok(enrichMethod(saved)));
    }

    // DELETE /api/v1/payment-methods/{id}
    @DeleteMapping("/payment-methods/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteMethod(@PathVariable Long id) {
        paymentMethodRepository.findById(id).ifPresent(entity -> {
            entity.setIsDeleted(true);
            paymentMethodRepository.save(entity);
        });
        paymentMethodBranchRepository.deleteByPaymentMethodId(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ---- Helpers ----

    @SuppressWarnings("unchecked")
    private void saveBranchMappings(Long paymentMethodId, Map<String, Object> req) {
        Object applyToAll = req.get("applyToAllBranches");
        boolean applyAll = applyToAll == null || Boolean.TRUE.equals(applyToAll) || "true".equals(String.valueOf(applyToAll));
        if (!applyAll) {
            Object branchIdsObj = req.get("branchIds");
            if (branchIdsObj instanceof List<?> branchList) {
                for (Object bid : branchList) {
                    try {
                        Long branchId = Long.parseLong(String.valueOf(bid));
                        PaymentMethodBranch mapping = PaymentMethodBranch.builder()
                                .paymentMethodId(paymentMethodId)
                                .branchId(branchId)
                                .build();
                        mapping.setIsDeleted(false);
                        paymentMethodBranchRepository.save(mapping);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    private PaymentMethod buildFromRequest(PaymentMethod pm, Map<String, Object> req) {
        if (req.get("methodCode") != null) pm.setMethodCode(String.valueOf(req.get("methodCode")));
        if (req.get("methodName") != null) pm.setMethodName(String.valueOf(req.get("methodName")));
        String type = req.get("providerType") != null ? String.valueOf(req.get("providerType"))
                     : (req.get("type") != null ? String.valueOf(req.get("type")) : null);
        if (type != null) { pm.setType(type); pm.setProviderType(type); }
        if (req.get("status") != null) pm.setStatus(String.valueOf(req.get("status")));
        if (req.get("sortOrder") != null) pm.setSortOrder(toInt(req.get("sortOrder")));
        if (req.get("logoUrl") != null) pm.setLogoUrl(String.valueOf(req.get("logoUrl")));
        if (req.get("bankName") != null) pm.setBankName(String.valueOf(req.get("bankName")));
        if (req.get("bankAccount") != null) pm.setBankAccount(String.valueOf(req.get("bankAccount")));
        if (req.get("bankAccountName") != null) pm.setBankAccountName(String.valueOf(req.get("bankAccountName")));
        if (req.get("transferSyntax") != null) pm.setTransferSyntax(String.valueOf(req.get("transferSyntax")));
        if (req.get("merchantId") != null) pm.setMerchantId(String.valueOf(req.get("merchantId")));
        if (req.get("apiKey") != null) pm.setApiKey(String.valueOf(req.get("apiKey")));
        if (req.get("secretKey") != null) pm.setSecretKey(String.valueOf(req.get("secretKey")));
        if (req.get("checksumKey") != null) pm.setChecksumKey(String.valueOf(req.get("checksumKey")));
        if (req.get("configuredGateways") != null) pm.setConfiguredGateways(String.valueOf(req.get("configuredGateways")));
        if (req.get("currency") != null) pm.setCurrency(String.valueOf(req.get("currency")));
        if (req.get("settlementTime") != null) pm.setSettlementTime(String.valueOf(req.get("settlementTime")));
        if (req.get("allowPos") != null) pm.setAllowPos(toBoolean(req.get("allowPos")));
        if (req.get("allowOnline") != null) pm.setAllowOnline(toBoolean(req.get("allowOnline")));
        if (req.get("processingFeePct") != null) pm.setProcessingFeePct(toBigDecimal(req.get("processingFeePct")));
        if (req.get("fixedFeeUsd") != null) pm.setFixedFeeUsd(toBigDecimal(req.get("fixedFeeUsd")));

        Object applyToAll = req.get("applyToAllBranches");
        if (applyToAll != null) {
            pm.setApplyToAllBranches(toBoolean(applyToAll));
        } else {
            pm.setApplyToAllBranches(true);
        }
        return pm;
    }

    private int toInt(Object val) {
        if (val instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(val)); } catch (Exception e) { return 0; }
    }

    private boolean toBoolean(Object val) {
        if (val instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(val));
    }

    private java.math.BigDecimal toBigDecimal(Object val) {
        if (val instanceof Number n) return new java.math.BigDecimal(n.toString());
        try { return new java.math.BigDecimal(String.valueOf(val)); } catch (Exception e) { return java.math.BigDecimal.ZERO; }
    }
}
