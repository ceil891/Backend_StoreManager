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
