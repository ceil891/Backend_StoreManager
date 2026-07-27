package org.example.storemanager.modules.finance.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.advancedaccounting.entity.*;
import org.example.storemanager.modules.advancedaccounting.repository.*;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return ResponseEntity.ok(ApiResponse.ok(chartOfAccountRepository.findByIsDeletedFalse()));
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
        return ResponseEntity.ok(ApiResponse.ok(costCenterRepository.findByIsDeletedFalse()));
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
        return ResponseEntity.ok(ApiResponse.ok(fixedAssetRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/fixed-assets")
    public ResponseEntity<ApiResponse<FixedAsset>> createFixedAsset(@RequestBody FixedAsset req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(fixedAssetRepository.save(req)));
    }

    // --- DEPRECIATION HISTORY ---
    @GetMapping("/depreciation-history")
    public ResponseEntity<ApiResponse<List<DepreciationHistory>>> getAllDepreciationHistory() {
        return ResponseEntity.ok(ApiResponse.ok(depreciationHistoryRepository.findByIsDeletedFalse()));
    }

    // --- JOURNAL ENTRIES ---
    @GetMapping("/journal-entries")
    public ResponseEntity<ApiResponse<List<JournalEntry>>> getAllJournalEntries() {
        return ResponseEntity.ok(ApiResponse.ok(journalEntryRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/journal-entries")
    public ResponseEntity<ApiResponse<JournalEntry>> createJournalEntry(@RequestBody JournalEntry req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(journalEntryRepository.save(req)));
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
