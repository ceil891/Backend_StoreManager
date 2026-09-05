package org.example.storemanager.modules.finance.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.advancedaccounting.entity.*;
import org.example.storemanager.modules.advancedaccounting.repository.*;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/accounting")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AdvancedAccountingController {

    private final AccountBalanceRepository accountBalanceRepository;
    private final AccountingPeriodRepository accountingPeriodRepository;
    private final AssetDisposalRepository assetDisposalRepository;
    private final AssetMaintenanceRepository assetMaintenanceRepository;
    private final ChartOfAccountRepository chartOfAccountRepository;
    private final CostCenterRepository costCenterRepository;
    private final DepreciationHistoryRepository depreciationHistoryRepository;
    private final FixedAssetRepository fixedAssetRepository;
    private final JournalAttachmentRepository journalAttachmentRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;

    // --- CHART OF ACCOUNTS ---
    @GetMapping("/chart-of-accounts")
    public ResponseEntity<ApiResponse<List<ChartOfAccount>>> getAllChartOfAccounts() {
        List<ChartOfAccount> list = chartOfAccountRepository.findByIsDeletedFalse();
        if (list.isEmpty()) {
            List<ChartOfAccount> defaultAccounts = List.of(
                ChartOfAccount.builder().accountCode("111").accountName("Tiền mặt tại két").type("ASSET").isActive(true).build(),
                ChartOfAccount.builder().accountCode("112").accountName("Tiền gửi ngân hàng").type("ASSET").isActive(true).build(),
                ChartOfAccount.builder().accountCode("131").accountName("Phải thu của khách hàng").type("ASSET").isActive(true).build(),
                ChartOfAccount.builder().accountCode("156").accountName("Hàng hóa tồn kho").type("ASSET").isActive(true).build(),
                ChartOfAccount.builder().accountCode("211").accountName("Tài sản cố định hữu hình").type("ASSET").isActive(true).build(),
                ChartOfAccount.builder().accountCode("331").accountName("Phải trả cho người bán").type("LIABILITY").isActive(true).build(),
                ChartOfAccount.builder().accountCode("333").accountName("Thuế và các khoản phải nộp NN").type("LIABILITY").isActive(true).build(),
                ChartOfAccount.builder().accountCode("334").accountName("Phải trả người lao động (Lương)").type("LIABILITY").isActive(true).build(),
                ChartOfAccount.builder().accountCode("411").accountName("Vốn đầu tư của chủ sở hữu").type("EQUITY").isActive(true).build(),
                ChartOfAccount.builder().accountCode("511").accountName("Doanh thu bán hàng và cung cấp DV").type("REVENUE").isActive(true).build(),
                ChartOfAccount.builder().accountCode("632").accountName("Giá vốn hàng bán").type("EXPENSE").isActive(true).build(),
                ChartOfAccount.builder().accountCode("641").accountName("Chi phí bán hàng").type("EXPENSE").isActive(true).build(),
                ChartOfAccount.builder().accountCode("642").accountName("Chi phí quản lý doanh nghiệp").type("EXPENSE").isActive(true).build()
            );
            defaultAccounts.forEach(a -> a.setIsDeleted(false));
            list = chartOfAccountRepository.saveAll(defaultAccounts);
        }
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/chart-of-accounts")
    public ResponseEntity<ApiResponse<ChartOfAccount>> createChartOfAccount(@RequestBody ChartOfAccount req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(chartOfAccountRepository.save(req)));
    }

    @PutMapping("/chart-of-accounts/{id}")
    public ResponseEntity<ApiResponse<ChartOfAccount>> updateChartOfAccount(@PathVariable Long id, @RequestBody ChartOfAccount req) {
        ChartOfAccount account = chartOfAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChartOfAccount", "id", id));
        account.setAccountCode(req.getAccountCode());
        account.setAccountName(req.getAccountName());
        account.setType(req.getType());
        account.setIsActive(req.getIsActive());
        if (req.getParent() != null) {
            account.setParent(req.getParent());
        }
        return ResponseEntity.ok(ApiResponse.ok(chartOfAccountRepository.save(account)));
    }

    @DeleteMapping("/chart-of-accounts/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteChartOfAccount(@PathVariable Long id) {
        ChartOfAccount account = chartOfAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChartOfAccount", "id", id));
        account.setIsDeleted(true);
        chartOfAccountRepository.save(account);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- COST CENTERS ---
    @GetMapping("/cost-centers")
    public ResponseEntity<ApiResponse<List<CostCenter>>> getAllCostCenters() {
        List<CostCenter> list = costCenterRepository.findByIsDeletedFalse();
        if (list.isEmpty()) {
            List<CostCenter> defaultCenters = List.of(
                CostCenter.builder().centerCode("CC-HO").centerName("Trung tâm Hội sở chính").description("Quản lý toàn bộ chi phí vận hành khối văn phòng").build(),
                CostCenter.builder().centerCode("CC-STORE-01").centerName("Trung tâm Chi nhánh Q1 TP.HCM").description("Chi phí vận hành bán lẻ cửa hàng flagship").build(),
                CostCenter.builder().centerCode("CC-STORE-02").centerName("Trung tâm Chi nhánh Cầu Giấy HN").description("Chi phí mặt bằng & nhân sự bán lẻ Hà Nội").build(),
                CostCenter.builder().centerCode("CC-WMS-NORTH").centerName("Tổng kho Phân phối Miền Bắc").description("Chi phí lưu kho, logistics & bốc xếp").build(),
                CostCenter.builder().centerCode("CC-MKT").centerName("Phòng Marketing & Tiếp thị số").description("Chi phí quảng cáo Google, FB, TikTok & Sự kiện").build()
            );
            defaultCenters.forEach(c -> c.setIsDeleted(false));
            list = costCenterRepository.saveAll(defaultCenters);
        }
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/cost-centers")
    public ResponseEntity<ApiResponse<CostCenter>> createCostCenter(@RequestBody CostCenter req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(costCenterRepository.save(req)));
    }

    @PutMapping("/cost-centers/{id}")
    public ResponseEntity<ApiResponse<CostCenter>> updateCostCenter(@PathVariable Long id, @RequestBody CostCenter req) {
        CostCenter center = costCenterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CostCenter", "id", id));
        center.setCenterCode(req.getCenterCode());
        center.setCenterName(req.getCenterName());
        center.setDescription(req.getDescription());
        return ResponseEntity.ok(ApiResponse.ok(costCenterRepository.save(center)));
    }

    @DeleteMapping("/cost-centers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCostCenter(@PathVariable Long id) {
        CostCenter center = costCenterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CostCenter", "id", id));
        center.setIsDeleted(true);
        costCenterRepository.save(center);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- FIXED ASSETS ---
    @GetMapping("/fixed-assets")
    public ResponseEntity<ApiResponse<List<FixedAsset>>> getAllFixedAssets() {
        List<FixedAsset> list = fixedAssetRepository.findByIsDeletedFalse();
        if (list.isEmpty()) {
            List<FixedAsset> defaultAssets = List.of(
                FixedAsset.builder()
                    .assetCode("FA-SRV-001")
                    .assetName("Hệ thống Máy chủ Dell PowerEdge R750")
                    .category("Thiết bị CNTT")
                    .purchaseDate(java.time.LocalDate.of(2024, 1, 15))
                    .purchasePrice(new BigDecimal("125000000"))
                    .salvageValue(new BigDecimal("5000000"))
                    .accumulatedDepreciation(new BigDecimal("35000000"))
                    .usefulLifeMonths(36)
                    .status("ACTIVE")
                    .build(),
                FixedAsset.builder()
                    .assetCode("FA-VAN-002")
                    .assetName("Xe tải giao hàng Hyundai Porter H150")
                    .category("Phương tiện vận tải")
                    .purchaseDate(java.time.LocalDate.of(2023, 6, 20))
                    .purchasePrice(new BigDecimal("420000000"))
                    .salvageValue(new BigDecimal("20000000"))
                    .accumulatedDepreciation(new BigDecimal("110000000"))
                    .usefulLifeMonths(60)
                    .status("ACTIVE")
                    .build(),
                FixedAsset.builder()
                    .assetCode("FA-POS-003")
                    .assetName("Dàn máy bán hàng POS Sunmi D2s Plus (10 máy)")
                    .category("Thiết bị bán lẻ")
                    .purchaseDate(java.time.LocalDate.of(2024, 3, 10))
                    .purchasePrice(new BigDecimal("85000000"))
                    .salvageValue(new BigDecimal("2000000"))
                    .accumulatedDepreciation(new BigDecimal("12000000"))
                    .usefulLifeMonths(24)
                    .status("ACTIVE")
                    .build()
            );
            defaultAssets.forEach(a -> a.setIsDeleted(false));
            list = fixedAssetRepository.saveAll(defaultAssets);
        }
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/fixed-assets")
    public ResponseEntity<ApiResponse<FixedAsset>> createFixedAsset(@RequestBody FixedAsset req) {
        if (req.getPurchaseDate() == null) req.setPurchaseDate(java.time.LocalDate.now());
        if (req.getPurchasePrice() == null) req.setPurchasePrice(BigDecimal.ZERO);
        if (req.getSalvageValue() == null) req.setSalvageValue(BigDecimal.ZERO);
        if (req.getAccumulatedDepreciation() == null) req.setAccumulatedDepreciation(BigDecimal.ZERO);
        if (req.getStatus() == null) req.setStatus("ACTIVE");
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(fixedAssetRepository.save(req)));
    }

    @PutMapping("/fixed-assets/{id}")
    public ResponseEntity<ApiResponse<FixedAsset>> updateFixedAsset(@PathVariable Long id, @RequestBody FixedAsset req) {
        FixedAsset existing = fixedAssetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedAsset", "id", id));
        if (req.getAssetName() != null) existing.setAssetName(req.getAssetName());
        if (req.getAssetCode() != null) existing.setAssetCode(req.getAssetCode());
        if (req.getCategory() != null) existing.setCategory(req.getCategory());
        if (req.getPurchaseDate() != null) existing.setPurchaseDate(req.getPurchaseDate());
        if (req.getPurchasePrice() != null) existing.setPurchasePrice(req.getPurchasePrice());
        if (req.getSalvageValue() != null) existing.setSalvageValue(req.getSalvageValue());
        if (req.getAccumulatedDepreciation() != null) existing.setAccumulatedDepreciation(req.getAccumulatedDepreciation());
        if (req.getUsefulLifeMonths() != null) existing.setUsefulLifeMonths(req.getUsefulLifeMonths());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());
        return ResponseEntity.ok(ApiResponse.ok(fixedAssetRepository.save(existing)));
    }

    @DeleteMapping("/fixed-assets/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFixedAsset(@PathVariable Long id) {
        FixedAsset existing = fixedAssetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedAsset", "id", id));
        existing.setIsDeleted(true);
        fixedAssetRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- DEPRECIATION HISTORY ---
    @GetMapping("/depreciation-history")
    public ResponseEntity<ApiResponse<List<DepreciationHistory>>> getAllDepreciationHistory() {
        return ResponseEntity.ok(ApiResponse.ok(depreciationHistoryRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/depreciation-history")
    public ResponseEntity<ApiResponse<DepreciationHistory>> createDepreciationHistory(@RequestBody java.util.Map<String, Object> req) {
        DepreciationHistory dh = new DepreciationHistory();
        dh.setIsDeleted(false);
        
        Long assetId = req.get("assetId") != null ? Long.valueOf(req.get("assetId").toString().replaceAll("\\D+", "")) : null;
        FixedAsset asset = null;
        if (assetId != null) {
            asset = fixedAssetRepository.findById(assetId).orElse(null);
        }
        if (asset == null) {
            List<FixedAsset> assets = fixedAssetRepository.findByIsDeletedFalse();
            if (!assets.isEmpty()) {
                asset = assets.get(0);
            }
        }
        dh.setAsset(asset);
        
        String dateStr = req.get("depreciationDate") != null ? req.get("depreciationDate").toString() : null;
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            if (dateStr.contains("T")) dateStr = dateStr.split("T")[0];
            try {
                dh.setDepreciationDate(java.time.LocalDate.parse(dateStr));
            } catch (Exception e) {
                dh.setDepreciationDate(java.time.LocalDate.now());
            }
        } else {
            dh.setDepreciationDate(java.time.LocalDate.now());
        }
        
        BigDecimal amount = req.get("amount") != null ? new BigDecimal(req.get("amount").toString()) :
                (req.get("monthlyAmount") != null ? new BigDecimal(req.get("monthlyAmount").toString()) : BigDecimal.ZERO);
        dh.setAmount(amount);
        
        BigDecimal accumulated = req.get("accumulated") != null ? new BigDecimal(req.get("accumulated").toString()) : amount;
        dh.setAccumulated(accumulated);
        
        BigDecimal netValue = req.get("netValue") != null ? new BigDecimal(req.get("netValue").toString()) :
                (asset != null && asset.getPurchasePrice() != null ? asset.getPurchasePrice().subtract(accumulated) : BigDecimal.ZERO);
        dh.setNetValue(netValue.compareTo(BigDecimal.ZERO) >= 0 ? netValue : BigDecimal.ZERO);
        
        return ResponseEntity.status(201).body(ApiResponse.created(depreciationHistoryRepository.save(dh)));
    }

    @PutMapping("/depreciation-history/{id}")
    public ResponseEntity<ApiResponse<DepreciationHistory>> updateDepreciationHistory(@PathVariable Long id, @RequestBody java.util.Map<String, Object> req) {
        DepreciationHistory existing = depreciationHistoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DepreciationHistory", "id", id));
        if (req.get("amount") != null) {
            existing.setAmount(new BigDecimal(req.get("amount").toString()));
        }
        if (req.get("accumulated") != null) {
            existing.setAccumulated(new BigDecimal(req.get("accumulated").toString()));
        }
        if (req.get("netValue") != null) {
            existing.setNetValue(new BigDecimal(req.get("netValue").toString()));
        }
        return ResponseEntity.ok(ApiResponse.ok(depreciationHistoryRepository.save(existing)));
    }

    @DeleteMapping("/depreciation-history/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepreciationHistory(@PathVariable Long id) {
        DepreciationHistory existing = depreciationHistoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DepreciationHistory", "id", id));
        existing.setIsDeleted(true);
        depreciationHistoryRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- JOURNAL ENTRIES ---
    @GetMapping("/journal-entries")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<JournalEntry>>> getAllJournalEntries() {
        List<JournalEntry> entries = journalEntryRepository.findByIsDeletedFalse();
        for (JournalEntry entry : entries) {
            List<JournalEntryLine> lines = journalEntryLineRepository.findByJournalEntryIdAndIsDeletedFalse(entry.getId());
            List<java.util.Map<String, Object>> mappedLines = new java.util.ArrayList<>();
            for (JournalEntryLine line : lines) {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("id", line.getId() != null ? line.getId().toString() : "");
                m.put("accountCode", line.getAccount() != null ? line.getAccount().getAccountCode() : "111");
                m.put("accountName", line.getAccount() != null ? line.getAccount().getAccountName() : "");
                m.put("debit", line.getDebitAmount() != null ? line.getDebitAmount() : BigDecimal.ZERO);
                m.put("credit", line.getCreditAmount() != null ? line.getCreditAmount() : BigDecimal.ZERO);
                m.put("description", line.getDescription() != null ? line.getDescription() : "");
                mappedLines.add(m);
            }
            entry.setLines(mappedLines);
        }
        return ResponseEntity.ok(ApiResponse.ok(entries));
    }

    @PostMapping("/journal-entries")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<JournalEntry>> createJournalEntry(@RequestBody java.util.Map<String, Object> req) {
        JournalEntry entry = new JournalEntry();
        entry.setIsDeleted(false);
        
        String refCode = req.get("referenceCode") != null ? req.get("referenceCode").toString() :
                (req.get("code") != null ? req.get("code").toString() :
                (req.get("entryCode") != null ? req.get("entryCode").toString() : "JE-" + System.currentTimeMillis()));
        entry.setReferenceCode(refCode);
        
        String desc = req.get("description") != null ? req.get("description").toString() : "";
        entry.setDescription(desc);
        
        String status = req.get("status") != null ? req.get("status").toString() : "POSTED";
        entry.setStatus(status);
        
        BigDecimal total = req.get("totalAmount") != null ? new BigDecimal(req.get("totalAmount").toString()) :
                (req.get("amount") != null ? new BigDecimal(req.get("amount").toString()) : BigDecimal.ZERO);
        entry.setTotalAmount(total);

        Object dateObj = req.get("entryDate") != null ? req.get("entryDate") :
                (req.get("date") != null ? req.get("date") : req.get("transactionDate"));
        entry.setEntryDate(parseJournalDate(dateObj));
        
        JournalEntry savedEntry = journalEntryRepository.save(entry);
        
        if (req.get("lines") instanceof java.util.List) {
            java.util.List<?> rawLines = (java.util.List<?>) req.get("lines");
            if (!rawLines.isEmpty()) {
                saveLinesForEntry(savedEntry, rawLines);
            }
        }
        
        return ResponseEntity.status(201).body(ApiResponse.created(savedEntry));
    }

    @PutMapping("/journal-entries/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<JournalEntry>> updateJournalEntry(@PathVariable Long id, @RequestBody java.util.Map<String, Object> req) {
        JournalEntry existing = journalEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", "id", id));
        if (req.get("referenceCode") != null) existing.setReferenceCode(req.get("referenceCode").toString());
        else if (req.get("code") != null) existing.setReferenceCode(req.get("code").toString());
        else if (req.get("entryCode") != null) existing.setReferenceCode(req.get("entryCode").toString());

        if (req.get("description") != null) existing.setDescription(req.get("description").toString());
        if (req.get("status") != null) existing.setStatus(req.get("status").toString());
        if (req.get("totalAmount") != null) existing.setTotalAmount(new BigDecimal(req.get("totalAmount").toString()));

        Object dateObj = req.get("entryDate") != null ? req.get("entryDate") :
                (req.get("date") != null ? req.get("date") : req.get("transactionDate"));
        if (dateObj != null) {
            existing.setEntryDate(parseJournalDate(dateObj));
        }
        
        JournalEntry savedEntry = journalEntryRepository.save(existing);
        
        if (req.get("lines") instanceof java.util.List) {
            java.util.List<?> rawLines = (java.util.List<?>) req.get("lines");
            if (!rawLines.isEmpty()) {
                // Soft delete old lines only when new lines are explicitly provided
                List<JournalEntryLine> oldLines = journalEntryLineRepository.findByJournalEntryIdAndIsDeletedFalse(id);
                for (JournalEntryLine l : oldLines) {
                    l.setIsDeleted(true);
                    journalEntryLineRepository.save(l);
                }
                saveLinesForEntry(savedEntry, rawLines);
            }
        }
        
        return ResponseEntity.ok(ApiResponse.ok(savedEntry));
    }

    private LocalDateTime parseJournalDate(Object dateObj) {
        if (dateObj == null) return LocalDateTime.now();
        if (dateObj instanceof LocalDateTime) return (LocalDateTime) dateObj;
        String str = dateObj.toString().trim();
        if (str.isEmpty()) return LocalDateTime.now();
        try {
            if (str.length() == 10) {
                return java.time.LocalDate.parse(str).atStartOfDay();
            } else if (str.contains("T")) {
                return LocalDateTime.parse(str.split("\\+")[0].split("Z")[0]);
            } else if (str.contains(" ")) {
                return LocalDateTime.parse(str.replace(" ", "T"));
            } else {
                return LocalDateTime.parse(str);
            }
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private void saveLinesForEntry(JournalEntry savedEntry, java.util.List<?> rawLines) {
        List<ChartOfAccount> allAccounts = chartOfAccountRepository.findByIsDeletedFalse();
        ChartOfAccount defaultAccount = !allAccounts.isEmpty() ? allAccounts.get(0) : null;
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        
        for (Object item : rawLines) {
            if (!(item instanceof java.util.Map)) continue;
            java.util.Map<?, ?> lineMap = (java.util.Map<?, ?>) item;
            String accCode = lineMap.get("accountCode") != null ? lineMap.get("accountCode").toString() : null;
            ChartOfAccount acc = defaultAccount;
            if (accCode != null) {
                for (ChartOfAccount a : allAccounts) {
                    if (accCode.equals(a.getAccountCode())) {
                        acc = a;
                        break;
                    }
                }
            }
            if (acc == null) continue;
            
            BigDecimal debit = lineMap.get("debit") != null ? new BigDecimal(lineMap.get("debit").toString()) :
                    (lineMap.get("debitAmount") != null ? new BigDecimal(lineMap.get("debitAmount").toString()) : BigDecimal.ZERO);
            BigDecimal credit = lineMap.get("credit") != null ? new BigDecimal(lineMap.get("credit").toString()) :
                    (lineMap.get("creditAmount") != null ? new BigDecimal(lineMap.get("creditAmount").toString()) : BigDecimal.ZERO);
            String lineDesc = lineMap.get("description") != null ? lineMap.get("description").toString() : "";
            
            JournalEntryLine line = new JournalEntryLine();
            line.setJournalEntry(savedEntry);
            line.setAccount(acc);
            line.setDebitAmount(debit);
            line.setCreditAmount(credit);
            line.setDescription(lineDesc);
            line.setIsDeleted(false);
            journalEntryLineRepository.save(line);
            
            if (debit.compareTo(BigDecimal.ZERO) > 0) {
                calculatedTotal = calculatedTotal.add(debit);
            }
        }
        if (savedEntry.getTotalAmount() == null || savedEntry.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            savedEntry.setTotalAmount(calculatedTotal);
            journalEntryRepository.save(savedEntry);
        }
    }

    @DeleteMapping("/journal-entries/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<Void>> deleteJournalEntry(@PathVariable Long id) {
        JournalEntry existing = journalEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", "id", id));
        existing.setIsDeleted(true);
        journalEntryRepository.save(existing);
        
        List<JournalEntryLine> lines = journalEntryLineRepository.findByJournalEntryIdAndIsDeletedFalse(id);
        for (JournalEntryLine line : lines) {
            line.setIsDeleted(true);
            journalEntryLineRepository.save(line);
        }
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- ACCOUNT BALANCES ---
    @GetMapping("/account-balances")
    public ResponseEntity<ApiResponse<List<AccountBalance>>> getAllAccountBalances() {
        return ResponseEntity.ok(ApiResponse.ok(accountBalanceRepository.findByIsDeletedFalse()));
    }

    // --- PERIODS ---
    @GetMapping("/periods")
    public ResponseEntity<ApiResponse<List<AccountingPeriod>>> getAllPeriods() {
        return ResponseEntity.ok(ApiResponse.ok(accountingPeriodRepository.findByIsDeletedFalse()));
    }
}
