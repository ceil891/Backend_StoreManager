package org.example.storemanager.modules.finance.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.finance.entity.*;
import org.example.storemanager.modules.finance.repository.*;
import org.example.storemanager.modules.sales.repository.ExportInvoiceRepository;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.example.storemanager.modules.advancedaccounting.entity.JournalEntry;
import org.example.storemanager.modules.advancedaccounting.entity.JournalEntryLine;
import org.example.storemanager.modules.advancedaccounting.entity.ChartOfAccount;
import org.example.storemanager.modules.advancedaccounting.repository.JournalEntryRepository;
import org.example.storemanager.modules.advancedaccounting.repository.JournalEntryLineRepository;
import org.example.storemanager.modules.advancedaccounting.repository.ChartOfAccountRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class FinanceController {

    private final ReceiptVoucherRepository receiptVoucherRepository;
    private final PaymentVoucherRepository paymentVoucherRepository;
    private final DebtLedgerRepository debtLedgerRepository;
    private final OperatingCostRepository operatingCostRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TaxDutyRepository taxDutyRepository;
    private final FundBalanceRepository fundBalanceRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final TransactionReasonRepository transactionReasonRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PayrollRepository payrollRepository;
    private final ExportInvoiceRepository exportInvoiceRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final org.example.storemanager.modules.sales.repository.PurchaseOrderRepository purchaseOrderRepository;
    private final ChartOfAccountRepository chartOfAccountRepository;
    private final org.example.storemanager.modules.system.repository.BranchRepository branchRepository;

    // --- BANK ACCOUNTS ---
    @GetMapping("/bank-accounts")
    public ResponseEntity<ApiResponse<List<BankAccount>>> getAllBankAccounts() {
        List<BankAccount> list = bankAccountRepository.findByIsDeletedFalse();
        if (list.isEmpty() || list.stream().allMatch(b -> b.getCurrentBalance() == null || b.getCurrentBalance().compareTo(java.math.BigDecimal.ZERO) == 0)) {
            // Seed realistic treasury corporate accounts
            List<BankAccount> seeded = List.of(
                BankAccount.builder()
                    .bankName("Techcombank")
                    .accountNumber("19036528998018")
                    .accountHolder("CÔNG TY TNHH BÁN LẺ RETAILHUB")
                    .branchName("Hội Sở Ba Đình, Hà Nội")
                    .swiftBic("TCBVNVX")
                    .currency("VND")
                    .currentBalance(new java.math.BigDecimal("2850000000"))
                    .availableWorkingCapital(new java.math.BigDecimal("2850000000"))
                    .accountType("PRIMARY_OPERATING")
                    .openedDate("2024-01-15")
                    .status("ACTIVE")
                    .isActive(true)
                    .build(),
                BankAccount.builder()
                    .bankName("Vietcombank")
                    .accountNumber("0071000889988")
                    .accountHolder("CÔNG TY TNHH BÁN LẺ RETAILHUB")
                    .branchName("Chi nhánh Quận 1 TP.HCM")
                    .swiftBic("BFTVVNVX")
                    .currency("VND")
                    .currentBalance(new java.math.BigDecimal("1620000000"))
                    .availableWorkingCapital(new java.math.BigDecimal("1620000000"))
                    .accountType("PRIMARY_OPERATING")
                    .openedDate("2024-03-20")
                    .status("ACTIVE")
                    .isActive(true)
                    .build(),
                BankAccount.builder()
                    .bankName("MBBank (Quân Đội)")
                    .accountNumber("0888999888")
                    .accountHolder("CÔNG TY TNHH BÁN LẺ RETAILHUB")
                    .branchName("Chi nhánh Đà Nẵng")
                    .swiftBic("MBBEVNVX")
                    .currency("VND")
                    .currentBalance(new java.math.BigDecimal("950000000"))
                    .availableWorkingCapital(new java.math.BigDecimal("950000000"))
                    .accountType("PAYROLL_DISBURSEMENT")
                    .openedDate("2024-06-10")
                    .status("ACTIVE")
                    .isActive(true)
                    .build(),
                BankAccount.builder()
                    .bankName("VPBank")
                    .accountNumber("18669998888")
                    .accountHolder("CÔNG TY TNHH BÁN LẺ RETAILHUB")
                    .branchName("Chi nhánh Cầu Giấy, Hà Nội")
                    .swiftBic("VPBKVNVX")
                    .currency("VND")
                    .currentBalance(new java.math.BigDecimal("740000000"))
                    .availableWorkingCapital(new java.math.BigDecimal("740000000"))
                    .accountType("MERCHANT_SETTLEMENT")
                    .openedDate("2024-08-05")
                    .status("ACTIVE")
                    .isActive(true)
                    .build(),
                BankAccount.builder()
                    .bankName("BIDV")
                    .accountNumber("21510001234567")
                    .accountHolder("CÔNG TY TNHH BÁN LẺ RETAILHUB")
                    .branchName("Chi nhánh Hoàn Kiếm, Hà Nội")
                    .swiftBic("BIDVVNVX")
                    .currency("VND")
                    .currentBalance(new java.math.BigDecimal("1200000000"))
                    .availableWorkingCapital(new java.math.BigDecimal("1200000000"))
                    .accountType("ESCROW_RESERVE")
                    .openedDate("2024-09-12")
                    .status("ACTIVE")
                    .isActive(true)
                    .build()
            );
            seeded.forEach(b -> b.setIsDeleted(false));
            if (list.isEmpty()) {
                list = bankAccountRepository.saveAll(seeded);
            } else {
                // Enrich existing records with meaningful balances
                for (int i = 0; i < list.size(); i++) {
                    BankAccount b = list.get(i);
                    if (b.getCurrentBalance() == null || b.getCurrentBalance().compareTo(java.math.BigDecimal.ZERO) == 0) {
                        BankAccount template = seeded.get(i % seeded.size());
                        b.setCurrentBalance(template.getCurrentBalance());
                        b.setAvailableWorkingCapital(template.getAvailableWorkingCapital());
                        if (b.getSwiftBic() == null) b.setSwiftBic(template.getSwiftBic());
                        if (b.getCurrency() == null) b.setCurrency(template.getCurrency());
                        if (b.getAccountType() == null) b.setAccountType(template.getAccountType());
                        if (b.getAccountHolder() == null || b.getAccountHolder().trim().isEmpty()) {
                            b.setAccountHolder("CÔNG TY TNHH BÁN LẺ RETAILHUB");
                        }
                    }
                }
                list = bankAccountRepository.saveAll(list);
            }
        }
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/bank-accounts/{id}")
    public ResponseEntity<ApiResponse<BankAccount>> getBankAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(bankAccountRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount", "id", id))));
    }

    @PostMapping("/bank-accounts")
    public ResponseEntity<ApiResponse<BankAccount>> createBankAccount(@RequestBody BankAccount req) {
        req.setIsDeleted(false);
        if (req.getCurrentBalance() == null) req.setCurrentBalance(java.math.BigDecimal.ZERO);
        if (req.getAvailableWorkingCapital() == null) req.setAvailableWorkingCapital(req.getCurrentBalance());
        if (req.getCurrency() == null) req.setCurrency("VND");
        if (req.getAccountType() == null) req.setAccountType("PRIMARY_OPERATING");
        if (req.getStatus() == null) req.setStatus("ACTIVE");
        if (req.getIsActive() == null) req.setIsActive(true);
        return ResponseEntity.status(201).body(ApiResponse.created(bankAccountRepository.save(req)));
    }

    @PutMapping("/bank-accounts/{id}")
    public ResponseEntity<ApiResponse<BankAccount>> updateBankAccount(@PathVariable Long id, @RequestBody BankAccount req) {
        BankAccount existing = bankAccountRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount", "id", id));
        if (req.getBankName() != null) existing.setBankName(req.getBankName());
        if (req.getAccountNumber() != null) existing.setAccountNumber(req.getAccountNumber());
        if (req.getAccountHolder() != null) existing.setAccountHolder(req.getAccountHolder());
        if (req.getBranchName() != null) existing.setBranchName(req.getBranchName());
        if (req.getSwiftBic() != null) existing.setSwiftBic(req.getSwiftBic());
        if (req.getCurrency() != null) existing.setCurrency(req.getCurrency());
        if (req.getCurrentBalance() != null) existing.setCurrentBalance(req.getCurrentBalance());
        if (req.getAvailableWorkingCapital() != null) existing.setAvailableWorkingCapital(req.getAvailableWorkingCapital());
        if (req.getAccountType() != null) existing.setAccountType(req.getAccountType());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());
        if (req.getIsActive() != null) existing.setIsActive(req.getIsActive());
        return ResponseEntity.ok(ApiResponse.ok(bankAccountRepository.save(existing)));
    }

    @DeleteMapping("/bank-accounts/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBankAccount(@PathVariable Long id) {
        BankAccount existing = bankAccountRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount", "id", id));
        existing.setIsDeleted(true);
        bankAccountRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- RECEIPT VOUCHERS ---
    @GetMapping({"/receipt-vouchers", "/receipts"})
    public ResponseEntity<ApiResponse<List<ReceiptVoucher>>> getAllReceipts() {
        return ResponseEntity.ok(ApiResponse.ok(receiptVoucherRepository.findByIsDeletedFalse()));
    }

    @GetMapping({"/receipt-vouchers/{id}", "/receipts/{id}"})
    public ResponseEntity<ApiResponse<ReceiptVoucher>> getReceiptById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(receiptVoucherRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReceiptVoucher", "id", id))));
    }

    @PostMapping({"/receipt-vouchers", "/receipts"})
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<ReceiptVoucher>> createReceipt(@RequestBody ReceiptVoucher req) {
        req.setIsDeleted(false);
        if (req.getVoucherCode() == null || req.getVoucherCode().trim().isEmpty()) {
            req.setVoucherCode("PC-REC-" + System.currentTimeMillis());
        }
        if (req.getVoucherDate() == null) {
            req.setVoucherDate(LocalDateTime.now());
        }
        ReceiptVoucher saved = receiptVoucherRepository.save(req);
        if ("COMPLETED".equalsIgnoreCase(saved.getStatus())) {
            createJournalEntryForReceipt(saved);
            increaseFundBalance(saved);
        }
        return ResponseEntity.status(201).body(ApiResponse.created(saved));
    }

    @PutMapping({"/receipt-vouchers/{id}", "/receipts/{id}"})
    public ResponseEntity<ApiResponse<ReceiptVoucher>> updateReceipt(@PathVariable Long id, @RequestBody ReceiptVoucher req) {
        ReceiptVoucher existing = receiptVoucherRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReceiptVoucher", "id", id));
        
        String oldStatus = existing.getStatus();
        if (req.getPayerName() != null) existing.setPayerName(req.getPayerName());
        if (req.getAmount() != null) existing.setAmount(req.getAmount());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());
        if (req.getPaymentMethod() != null) existing.setPaymentMethod(req.getPaymentMethod());
        if (req.getFundAccountName() != null) existing.setFundAccountName(req.getFundAccountName());
        if (req.getInvoiceCode() != null) existing.setInvoiceCode(req.getInvoiceCode());
        if (req.getNotes() != null) existing.setNotes(req.getNotes());
        if (req.getVoucherDate() != null) existing.setVoucherDate(req.getVoucherDate());
        if (req.getCategory() != null) existing.setCategory(req.getCategory());
        
        ReceiptVoucher saved = receiptVoucherRepository.save(existing);
        
        if ("COMPLETED".equalsIgnoreCase(saved.getStatus()) && !"COMPLETED".equalsIgnoreCase(oldStatus)) {
            createJournalEntryForReceipt(saved);
            increaseFundBalance(saved);
        } else if ("CANCELLED".equalsIgnoreCase(saved.getStatus()) && !"CANCELLED".equalsIgnoreCase(oldStatus)) {
            createStornoEntry(saved.getVoucherCode());
        }
        
        return ResponseEntity.ok(ApiResponse.ok(saved));
    }

    @DeleteMapping({"/receipt-vouchers/{id}", "/receipts/{id}"})
    public ResponseEntity<ApiResponse<Void>> deleteReceipt(@PathVariable Long id) {
        ReceiptVoucher existing = receiptVoucherRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReceiptVoucher", "id", id));
        if ("COMPLETED".equalsIgnoreCase(existing.getStatus()) || "APPROVED".equalsIgnoreCase(existing.getStatus())) {
            existing.setStatus("CANCELLED");
            createStornoEntry(existing.getVoucherCode());
        }
        existing.setIsDeleted(true);
        receiptVoucherRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- PAYMENT VOUCHERS ---
    @GetMapping({"/payment-vouchers", "/payments"})
    public ResponseEntity<ApiResponse<List<PaymentVoucher>>> getAllPayments() {
        return ResponseEntity.ok(ApiResponse.ok(paymentVoucherRepository.findByIsDeletedFalse()));
    }

    @GetMapping({"/payment-vouchers/{id}", "/payments/{id}"})
    public ResponseEntity<ApiResponse<PaymentVoucher>> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(paymentVoucherRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentVoucher", "id", id))));
    }

    @PostMapping({"/payment-vouchers", "/payments"})
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<PaymentVoucher>> createPayment(@RequestBody PaymentVoucher req) {
        req.setIsDeleted(false);
        if (req.getVoucherCode() == null || req.getVoucherCode().trim().isEmpty()) {
            String dateStr = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd").format(java.time.LocalDate.now());
            req.setVoucherCode("PAY-PUR-" + dateStr + "-" + String.format("%03d", (int)(Math.random() * 900 + 100)));
        }
        if (req.getVoucherDate() == null) {
            req.setVoucherDate(LocalDateTime.now());
        }

        // Validate PO / Invoice Status
        if (req.getInvoiceCode() != null && !req.getInvoiceCode().trim().isEmpty()) {
            String invCode = req.getInvoiceCode().trim();
            org.example.storemanager.modules.sales.entity.PurchaseOrder po = null;
            if (invCode.startsWith("INV-MH-")) {
                try {
                    Long idVal = Long.parseLong(invCode.replace("INV-MH-", ""));
                    po = purchaseOrderRepository.findById(idVal).orElse(null);
                } catch (Exception e) {}
            } else {
                po = purchaseOrderRepository.findByPoCodeAndIsDeletedFalse(invCode).orElse(null);
            }
            if (po != null) {
                String poStatus = po.getStatus();
                if ("DRAFT".equals(poStatus) || "PENDING_APPROVAL".equals(poStatus)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Chỉ được phép lập phiếu chi cho hóa đơn hoặc đơn mua hàng đã duyệt (Trạng thái hiện tại: " + poStatus + ")");
                }
            }
        }

        PaymentVoucher saved = paymentVoucherRepository.save(req);
        if ("COMPLETED".equalsIgnoreCase(saved.getStatus()) || "APPROVED".equalsIgnoreCase(saved.getStatus())) {
            checkFundBalance(saved);
            createJournalEntryForPayment(saved);
            syncPurchaseOrderPaymentStatus(saved);
        }
        return ResponseEntity.status(201).body(ApiResponse.created(saved));
    }

    @PutMapping({"/payment-vouchers/{id}", "/payments/{id}"})
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<PaymentVoucher>> updatePayment(@PathVariable Long id, @RequestBody PaymentVoucher req) {
        PaymentVoucher existing = paymentVoucherRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentVoucher", "id", id));
        
        String oldStatus = existing.getStatus();
        boolean wasDone = "COMPLETED".equalsIgnoreCase(oldStatus) || "APPROVED".equalsIgnoreCase(oldStatus);

        if (wasDone) {
            boolean statusChangeToCancelled = "CANCELLED".equalsIgnoreCase(req.getStatus());
            if (!statusChangeToCancelled) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chứng từ đã duyệt ở trạng thái read-only và không thể chỉnh sửa");
            }
        }

        // Validate PO / Invoice Status if changed
        if (req.getInvoiceCode() != null && !req.getInvoiceCode().equalsIgnoreCase(existing.getInvoiceCode())) {
            String invCode = req.getInvoiceCode().trim();
            org.example.storemanager.modules.sales.entity.PurchaseOrder po = null;
            if (invCode.startsWith("INV-MH-")) {
                try {
                    Long idVal = Long.parseLong(invCode.replace("INV-MH-", ""));
                    po = purchaseOrderRepository.findById(idVal).orElse(null);
                } catch (Exception e) {}
            } else {
                po = purchaseOrderRepository.findByPoCodeAndIsDeletedFalse(invCode).orElse(null);
            }
            if (po != null) {
                String poStatus = po.getStatus();
                if ("DRAFT".equals(poStatus) || "PENDING_APPROVAL".equals(poStatus)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Chỉ được phép lập phiếu chi cho hóa đơn hoặc đơn mua hàng đã duyệt (Trạng thái hiện tại: " + poStatus + ")");
                }
            }
        }

        if (req.getReceiverName() != null && !req.getReceiverName().trim().isEmpty()) {
            existing.setReceiverName(req.getReceiverName());
        }
        if (req.getAmount() != null && req.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            existing.setAmount(req.getAmount());
        }
        if (req.getInvoiceCode() != null && !req.getInvoiceCode().trim().isEmpty()) {
            existing.setInvoiceCode(req.getInvoiceCode());
        }
        if (req.getPaymentMethod() != null && !req.getPaymentMethod().trim().isEmpty()) {
            existing.setPaymentMethod(req.getPaymentMethod());
        }
        if (req.getFundAccountName() != null && !req.getFundAccountName().trim().isEmpty()) {
            existing.setFundAccountName(req.getFundAccountName());
        }
        if (req.getAttachmentUrl() != null && !req.getAttachmentUrl().trim().isEmpty()) {
            existing.setAttachmentUrl(req.getAttachmentUrl());
        }
        if (req.getHandler() != null && !req.getHandler().trim().isEmpty()) {
            existing.setHandler(req.getHandler());
        }
        if (req.getNotes() != null && !req.getNotes().trim().isEmpty()) {
            existing.setNotes(req.getNotes());
        }
        if (req.getStatus() != null && !req.getStatus().trim().isEmpty()) {
            existing.setStatus(req.getStatus());
        }
        
        PaymentVoucher saved = paymentVoucherRepository.save(existing);
        
        boolean isNowDone = "COMPLETED".equalsIgnoreCase(saved.getStatus()) || "APPROVED".equalsIgnoreCase(saved.getStatus());

        if (isNowDone && !wasDone) {
            checkFundBalance(saved);
            createJournalEntryForPayment(saved);
            syncPurchaseOrderPaymentStatus(saved);
        } else if ("CANCELLED".equalsIgnoreCase(saved.getStatus()) && !"CANCELLED".equalsIgnoreCase(oldStatus)) {
            createStornoEntry(saved.getVoucherCode());
            revertPurchaseOrderPaymentStatus(saved);
        }
        
        return ResponseEntity.ok(ApiResponse.ok(saved));
    }

    private void syncPurchaseOrderPaymentStatus(PaymentVoucher pv) {
        if (pv.getInvoiceCode() == null || pv.getInvoiceCode().trim().isEmpty()) return;
        String invCode = pv.getInvoiceCode().trim();
        org.example.storemanager.modules.sales.entity.PurchaseOrder po = null;
        if (invCode.startsWith("INV-MH-")) {
            try {
                Long idVal = Long.parseLong(invCode.replace("INV-MH-", ""));
                po = purchaseOrderRepository.findById(idVal).orElse(null);
            } catch (Exception e) {}
        } else {
            po = purchaseOrderRepository.findByPoCodeAndIsDeletedFalse(invCode).orElse(null);
        }
        if (po == null) {
            final String searchCode = invCode.toLowerCase();
            po = purchaseOrderRepository.findAll().stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .filter(p -> p.getPoCode() != null && (p.getPoCode().equalsIgnoreCase(searchCode) || p.getPoCode().toLowerCase().contains(searchCode)))
                .findFirst().orElse(null);
        }
        if (po != null) {
            final org.example.storemanager.modules.sales.entity.PurchaseOrder targetPo = po;
            List<PaymentVoucher> poVouchers = paymentVoucherRepository.findAll().stream()
                .filter(v -> !Boolean.TRUE.equals(v.getIsDeleted()) && v.getInvoiceCode() != null)
                .filter(v -> {
                    String code = v.getInvoiceCode().trim();
                    return (targetPo.getPoCode() != null && (code.equalsIgnoreCase(targetPo.getPoCode()) || code.toLowerCase().contains(targetPo.getPoCode().toLowerCase())))
                        || code.equalsIgnoreCase("INV-MH-" + targetPo.getId())
                        || code.equalsIgnoreCase(String.valueOf(targetPo.getId()));
                })
                .filter(v -> "COMPLETED".equalsIgnoreCase(v.getStatus()) || "APPROVED".equalsIgnoreCase(v.getStatus()) || "DA_THANH_TOAN".equalsIgnoreCase(v.getStatus()))
                .toList();

            BigDecimal totalPaid = poVouchers.stream()
                .map(v -> v.getAmount() != null ? v.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCost = po.getTotalAmount() != null ? po.getTotalAmount() : BigDecimal.ZERO;
            po.setAdvanceAmount(totalPaid);
            if (totalCost.compareTo(BigDecimal.ZERO) > 0 && totalPaid.compareTo(totalCost) >= 0) {
                po.setPaymentStatus("PAID");
            } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
                po.setPaymentStatus("PARTIAL_ADVANCE");
            } else {
                po.setPaymentStatus("UNPAID");
            }
            purchaseOrderRepository.save(po);

            // Cập nhật hoặc ghi nhận công nợ vào DebtLedger
            try {
                BigDecimal remainingDebt = totalCost.subtract(totalPaid).max(BigDecimal.ZERO);
                final String poCode = po.getPoCode();
                DebtLedger dl = debtLedgerRepository.findByIsDeletedFalse().stream()
                    .filter(d -> "SUPPLIER".equalsIgnoreCase(d.getEntityType()) && d.getRefCode() != null && d.getRefCode().equalsIgnoreCase(poCode))
                    .findFirst().orElse(null);

                if (dl == null) {
                    dl = DebtLedger.builder()
                        .refCode(po.getPoCode())
                        .partnerId(po.getSupplier() != null ? po.getSupplier().getId() : 1L)
                        .entityName(po.getSupplier() != null ? po.getSupplier().getName() : "Nhà cung cấp")
                        .entityType("SUPPLIER")
                        .transactionDate(po.getPoDate() != null ? po.getPoDate() : LocalDateTime.now())
                        .dueDate(po.getExpectedDate())
                        .accountManager("Kế toán công nợ mua hàng")
                        .build();
                    dl.setIsDeleted(false);
                }
                dl.setIncrease(totalCost);
                dl.setDecrease(totalPaid);
                dl.setBalance(remainingDebt);
                dl.setLastPaymentDate(pv.getVoucherDate() != null ? pv.getVoucherDate() : LocalDateTime.now());
                dl.setStatus(remainingDebt.compareTo(BigDecimal.ZERO) == 0 ? "SETTLED" : "NORMAL");
                dl.setNotes("Công nợ đơn mua " + po.getPoCode() + " - Đã thanh toán: " + totalPaid.toPlainString() + " / " + totalCost.toPlainString());
                debtLedgerRepository.save(dl);
            } catch (Exception ignored) {}
        }
    }

    private void revertPurchaseOrderPaymentStatus(PaymentVoucher pv) {
        if (pv.getInvoiceCode() == null || pv.getInvoiceCode().trim().isEmpty()) return;
        String invCode = pv.getInvoiceCode().trim();
        org.example.storemanager.modules.sales.entity.PurchaseOrder po = purchaseOrderRepository.findByPoCodeAndIsDeletedFalse(invCode).orElse(null);
        if (po == null) {
            final String searchCode = invCode.toLowerCase();
            po = purchaseOrderRepository.findAll().stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .filter(p -> p.getPoCode() != null && (p.getPoCode().equalsIgnoreCase(searchCode) || p.getPoCode().toLowerCase().contains(searchCode)))
                .findFirst().orElse(null);
        }
        if (po != null) {
            final org.example.storemanager.modules.sales.entity.PurchaseOrder targetPo = po;
            List<PaymentVoucher> poVouchers = paymentVoucherRepository.findAll().stream()
                .filter(v -> !Boolean.TRUE.equals(v.getIsDeleted()) && v.getInvoiceCode() != null)
                .filter(v -> !v.getId().equals(pv.getId()))
                .filter(v -> {
                    String code = v.getInvoiceCode().trim();
                    return (targetPo.getPoCode() != null && (code.equalsIgnoreCase(targetPo.getPoCode()) || code.toLowerCase().contains(targetPo.getPoCode().toLowerCase())))
                        || code.equalsIgnoreCase("INV-MH-" + targetPo.getId())
                        || code.equalsIgnoreCase(String.valueOf(targetPo.getId()));
                })
                .filter(v -> "COMPLETED".equalsIgnoreCase(v.getStatus()) || "APPROVED".equalsIgnoreCase(v.getStatus()) || "DA_THANH_TOAN".equalsIgnoreCase(v.getStatus()))
                .toList();

            BigDecimal totalPaid = poVouchers.stream()
                .map(v -> v.getAmount() != null ? v.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCost = po.getTotalAmount() != null ? po.getTotalAmount() : BigDecimal.ZERO;
            po.setAdvanceAmount(totalPaid);
            if (totalCost.compareTo(BigDecimal.ZERO) > 0 && totalPaid.compareTo(totalCost) >= 0) {
                po.setPaymentStatus("PAID");
            } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
                po.setPaymentStatus("PARTIAL_ADVANCE");
            } else {
                po.setPaymentStatus("UNPAID");
            }
            purchaseOrderRepository.save(po);

            try {
                BigDecimal remainingDebt = totalCost.subtract(totalPaid).max(BigDecimal.ZERO);
                final String poCode = po.getPoCode();
                DebtLedger dl = debtLedgerRepository.findByIsDeletedFalse().stream()
                    .filter(d -> "SUPPLIER".equalsIgnoreCase(d.getEntityType()) && d.getRefCode() != null && d.getRefCode().equalsIgnoreCase(poCode))
                    .findFirst().orElse(null);
                if (dl != null) {
                    dl.setDecrease(totalPaid);
                    dl.setBalance(remainingDebt);
                    dl.setStatus(remainingDebt.compareTo(BigDecimal.ZERO) == 0 ? "SETTLED" : "NORMAL");
                    dl.setNotes("Công nợ đơn mua " + po.getPoCode() + " - Đã thanh toán: " + totalPaid.toPlainString() + " / " + totalCost.toPlainString());
                    debtLedgerRepository.save(dl);
                }
            } catch (Exception ignored) {}
        }
    }

    @DeleteMapping({"/payment-vouchers/{id}", "/payments/{id}"})
    public ResponseEntity<ApiResponse<Void>> deletePayment(@PathVariable Long id) {
        PaymentVoucher existing = paymentVoucherRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentVoucher", "id", id));
        if ("COMPLETED".equalsIgnoreCase(existing.getStatus()) || "APPROVED".equalsIgnoreCase(existing.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không được phép xóa vật lý chứng từ đã duyệt. Vui lòng chuyển trạng thái sang CANCELLED.");
        }
        existing.setIsDeleted(true);
        paymentVoucherRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- DEBT LEDGERS ---
    @GetMapping({"/debt-ledgers", "/debts"})
    public ResponseEntity<ApiResponse<List<DebtLedger>>> getAllDebts() {
        syncSystemDebts();
        return ResponseEntity.ok(ApiResponse.ok(debtLedgerRepository.findByIsDeletedFalse()));
    }

    @GetMapping({"/debt-ledgers/aging-summary", "/debts/aging-summary"})
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getDebtAgingSummary(
            @RequestParam(required = false) String entityType) {
        syncSystemDebts();
        List<DebtLedger> list = debtLedgerRepository.findByIsDeletedFalse();
        if (entityType != null && !entityType.isBlank()) {
            list = list.stream().filter(d -> entityType.equalsIgnoreCase(d.getEntityType())).toList();
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal under30Days = BigDecimal.ZERO;
        BigDecimal days31To60 = BigDecimal.ZERO;
        BigDecimal days61To90 = BigDecimal.ZERO;
        BigDecimal over90Days = BigDecimal.ZERO;
        BigDecimal totalDebt = BigDecimal.ZERO;
        long under30Count = 0;
        long days31To60Count = 0;
        long days61To90Count = 0;
        long over90Count = 0;

        for (DebtLedger d : list) {
            BigDecimal bal = d.getBalance() != null ? d.getBalance() : BigDecimal.ZERO;
            if (bal.compareTo(BigDecimal.ZERO) <= 0) continue;
            totalDebt = totalDebt.add(bal);

            LocalDateTime refTime = d.getDueDate() != null ? d.getDueDate() : d.getTransactionDate();
            long days = refTime != null ? java.time.temporal.ChronoUnit.DAYS.between(refTime, now) : 0;

            if (days <= 30) {
                under30Days = under30Days.add(bal);
                under30Count++;
            } else if (days <= 60) {
                days31To60 = days31To60.add(bal);
                days31To60Count++;
            } else if (days <= 90) {
                days61To90 = days61To90.add(bal);
                days61To90Count++;
            } else {
                over90Days = over90Days.add(bal);
                over90Count++;
            }
        }

        java.util.Map<String, Object> summary = new java.util.LinkedHashMap<>();
        summary.put("totalDebt", totalDebt);
        summary.put("under30Days", under30Days);
        summary.put("under30Count", under30Count);
        summary.put("days31To60", days31To60);
        summary.put("days31To60Count", days31To60Count);
        summary.put("days61To90", days61To90);
        summary.put("days61To90Count", days61To90Count);
        summary.put("over90Days", over90Days);
        summary.put("over90Count", over90Count);
        summary.put("totalOverdue", days31To60.add(days61To90).add(over90Days));
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @PostMapping({"/debt-ledgers", "/debts"})
    public ResponseEntity<ApiResponse<DebtLedger>> createDebt(@RequestBody DebtLedger req) {
        // Validate: Ngày đến hạn phải >= ngày hiện tại
        if (req.getDueDate() != null && req.getDueDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ngày đến hạn thanh toán phải từ hôm nay trở đi"));
        }
        // Validate: Ngày giao dịch gần nhất phải <= ngày hiện tại
        if (req.getLastPaymentDate() != null && req.getLastPaymentDate().isAfter(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ngày giao dịch gần nhất không được là ngày tương lai"));
        }
        if (req.getTransactionDate() == null) {
            req.setTransactionDate(LocalDateTime.now());
        }
        if (req.getBalance() == null && req.getTotalDebt() != null) {
            req.setBalance(req.getTotalDebt());
        }
        if ((req.getIncrease() == null || req.getIncrease().compareTo(BigDecimal.ZERO) == 0) && req.getBalance() != null && req.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            req.setIncrease(req.getBalance());
        }
        if (req.getIncrease() == null) {
            req.setIncrease(BigDecimal.ZERO);
        }
        if (req.getDecrease() == null) {
            req.setDecrease(BigDecimal.ZERO);
        }
        if (req.getBalance() == null) {
            req.setBalance(req.getIncrease().compareTo(BigDecimal.ZERO) > 0 ? req.getIncrease() : BigDecimal.ZERO);
        }
        if (req.getPartnerId() == null) {
            req.setPartnerId(1L);
        }
        if (req.getStatus() == null || req.getStatus().trim().isEmpty()) {
            req.setStatus("NORMAL");
        }
        if (req.getRefCode() == null || req.getRefCode().trim().isEmpty()) {
            req.setRefCode("DBT-" + System.currentTimeMillis());
        }
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(debtLedgerRepository.save(req)));
    }

    @PutMapping({"/debt-ledgers/{id}", "/debts/{id}"})
    public ResponseEntity<ApiResponse<DebtLedger>> updateDebt(@PathVariable Long id, @RequestBody DebtLedger req) {
        DebtLedger existing = debtLedgerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("DebtLedger", "id", id));
        // Validate: Ngày đến hạn phải >= ngày hiện tại
        if (req.getDueDate() != null && req.getDueDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ngày đến hạn thanh toán phải từ hôm nay trở đi"));
        }
        // Validate: Ngày giao dịch gần nhất phải <= ngày hiện tại
        if (req.getLastPaymentDate() != null && req.getLastPaymentDate().isAfter(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ngày giao dịch gần nhất không được là ngày tương lai"));
        }
        if (req.getPartnerId() != null) existing.setPartnerId(req.getPartnerId());
        if (req.getRefCode() != null) existing.setRefCode(req.getRefCode());
        if (req.getBalance() != null) existing.setBalance(req.getBalance());
        else if (req.getTotalDebt() != null) existing.setBalance(req.getTotalDebt());
        if (req.getIncrease() != null) existing.setIncrease(req.getIncrease());
        if (req.getDecrease() != null) existing.setDecrease(req.getDecrease());
        if (req.getTransactionDate() != null) existing.setTransactionDate(req.getTransactionDate());
        if (req.getEntityName() != null) existing.setEntityName(req.getEntityName());
        if (req.getEntityType() != null) existing.setEntityType(req.getEntityType());
        if (req.getDueDate() != null) existing.setDueDate(req.getDueDate());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());
        if (req.getLastPaymentDate() != null) existing.setLastPaymentDate(req.getLastPaymentDate());
        if (req.getAccountManager() != null) existing.setAccountManager(req.getAccountManager());
        if (req.getNotes() != null) existing.setNotes(req.getNotes());
        return ResponseEntity.ok(ApiResponse.ok(debtLedgerRepository.save(existing)));
    }

    @DeleteMapping({"/debt-ledgers/{id}", "/debts/{id}"})
    public ResponseEntity<ApiResponse<Void>> deleteDebt(@PathVariable Long id) {
        DebtLedger existing = debtLedgerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("DebtLedger", "id", id));
        existing.setIsDeleted(true);
        debtLedgerRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    private void syncSystemDebts() {
        try {
            // 1. Đồng bộ công nợ phải trả Nhà cung cấp từ các Đơn mua hàng (PO)
            List<org.example.storemanager.modules.sales.entity.PurchaseOrder> pos = purchaseOrderRepository.findAll().stream()
                    .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                    .filter(p -> p.getPoCode() != null && !p.getPoCode().trim().isEmpty())
                    .filter(p -> !"DRAFT".equalsIgnoreCase(p.getStatus()) && !"CANCELLED".equalsIgnoreCase(p.getStatus()))
                    .toList();

            List<PaymentVoucher> allPaymentVouchers = paymentVoucherRepository.findAll().stream()
                    .filter(v -> !Boolean.TRUE.equals(v.getIsDeleted()))
                    .filter(v -> "COMPLETED".equalsIgnoreCase(v.getStatus()) || "APPROVED".equalsIgnoreCase(v.getStatus()) || "DA_THANH_TOAN".equalsIgnoreCase(v.getStatus()))
                    .toList();

            for (org.example.storemanager.modules.sales.entity.PurchaseOrder po : pos) {
                BigDecimal totalAmount = po.getTotalAmount() != null ? po.getTotalAmount() : BigDecimal.ZERO;
                if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) continue;

                // Tính tổng thanh toán đã chi cho đơn mua này
                BigDecimal totalPaid = allPaymentVouchers.stream()
                        .filter(v -> v.getInvoiceCode() != null && (
                                v.getInvoiceCode().trim().equalsIgnoreCase(po.getPoCode())
                                || v.getInvoiceCode().trim().toLowerCase().contains(po.getPoCode().toLowerCase())
                                || v.getInvoiceCode().trim().equalsIgnoreCase("INV-MH-" + po.getId())
                                || v.getInvoiceCode().trim().equalsIgnoreCase(String.valueOf(po.getId()))
                        ))
                        .map(v -> v.getAmount() != null ? v.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (po.getAdvanceAmount() != null && po.getAdvanceAmount().compareTo(totalPaid) > 0) {
                    totalPaid = po.getAdvanceAmount();
                }

                BigDecimal balance = totalAmount.subtract(totalPaid).max(BigDecimal.ZERO);
                String status = balance.compareTo(BigDecimal.ZERO) == 0 ? "SETTLED" : (totalPaid.compareTo(BigDecimal.ZERO) > 0 ? "NORMAL" : "NORMAL");

                if (!"SETTLED".equals(status) && po.getExpectedDate() != null && po.getExpectedDate().isBefore(LocalDateTime.now())) {
                    status = "OVERDUE";
                }

                final String poCode = po.getPoCode();
                DebtLedger dl = debtLedgerRepository.findByIsDeletedFalse().stream()
                        .filter(d -> "SUPPLIER".equalsIgnoreCase(d.getEntityType()) && d.getRefCode() != null && d.getRefCode().equalsIgnoreCase(poCode))
                        .findFirst().orElse(null);

                if (dl == null) {
                    dl = DebtLedger.builder()
                            .refCode(po.getPoCode())
                            .partnerId(po.getSupplier() != null ? po.getSupplier().getId() : 1L)
                            .entityName(po.getSupplier() != null ? po.getSupplier().getName() : "Nhà cung cấp")
                            .entityType("SUPPLIER")
                            .transactionDate(po.getPoDate() != null ? po.getPoDate() : LocalDateTime.now())
                            .dueDate(po.getExpectedDate())
                            .increase(totalAmount)
                            .decrease(totalPaid)
                            .balance(balance)
                            .status(status)
                            .accountManager("Kế toán công nợ mua hàng")
                            .notes("Công nợ đơn mua hàng " + po.getPoCode())
                            .build();
                    dl.setIsDeleted(false);
                    debtLedgerRepository.save(dl);
                } else {
                    dl.setIncrease(totalAmount);
                    dl.setDecrease(totalPaid);
                    dl.setBalance(balance);
                    dl.setStatus(status);
                    if (dl.getDueDate() == null && po.getExpectedDate() != null) {
                        dl.setDueDate(po.getExpectedDate());
                    }
                    debtLedgerRepository.save(dl);
                }
            }

            // 2. Đồng bộ công nợ phải thu Khách hàng từ Hóa đơn bán hàng (Export Invoices)
            List<org.example.storemanager.modules.sales.entity.ExportInvoice> invoices = exportInvoiceRepository.findAll().stream()
                    .filter(i -> !Boolean.TRUE.equals(i.getIsDeleted()))
                    .filter(i -> i.getInvoiceCode() != null && !i.getInvoiceCode().trim().isEmpty())
                    .filter(i -> !"CANCELLED".equalsIgnoreCase(i.getStatus()) && !"RETURNED".equalsIgnoreCase(i.getStatus()))
                    .toList();

            List<OrderPayment> allOrderPayments = orderPaymentRepository.findByIsDeletedFalse();

            for (org.example.storemanager.modules.sales.entity.ExportInvoice inv : invoices) {
                BigDecimal totalAmount = inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO;
                if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal totalPaid = allOrderPayments.stream()
                        .filter(op -> op.getInvoice() != null && inv.getId().equals(op.getInvoice().getId()))
                        .map(op -> op.getAmountPaid() != null ? op.getAmountPaid() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (("PAID".equalsIgnoreCase(inv.getStatus()) || "COMPLETED".equalsIgnoreCase(inv.getStatus())) && totalPaid.compareTo(BigDecimal.ZERO) == 0) {
                    totalPaid = totalAmount;
                }

                BigDecimal balance = totalAmount.subtract(totalPaid).max(BigDecimal.ZERO);
                String status = balance.compareTo(BigDecimal.ZERO) == 0 ? "SETTLED" : (totalPaid.compareTo(BigDecimal.ZERO) > 0 ? "NORMAL" : "NORMAL");
                if (!"SETTLED".equals(status) && inv.getDueDate() != null && inv.getDueDate().isBefore(LocalDateTime.now())) {
                    status = "OVERDUE";
                }

                final String invCode = inv.getInvoiceCode();
                DebtLedger dl = debtLedgerRepository.findByIsDeletedFalse().stream()
                        .filter(d -> "CUSTOMER".equalsIgnoreCase(d.getEntityType()) && d.getRefCode() != null && d.getRefCode().equalsIgnoreCase(invCode))
                        .findFirst().orElse(null);

                if (dl == null) {
                    dl = DebtLedger.builder()
                            .refCode(inv.getInvoiceCode())
                            .partnerId(inv.getCustomer() != null ? inv.getCustomer().getId() : 1L)
                            .entityName(inv.getCustomer() != null ? inv.getCustomer().getName() : "Khách hàng")
                            .entityType("CUSTOMER")
                            .transactionDate(inv.getInvoiceDate() != null ? inv.getInvoiceDate() : LocalDateTime.now())
                            .dueDate(inv.getDueDate())
                            .increase(totalAmount)
                            .decrease(totalPaid)
                            .balance(balance)
                            .status(status)
                            .accountManager("Kế toán công nợ bán hàng")
                            .notes("Công nợ hóa đơn bán hàng " + inv.getInvoiceCode())
                            .build();
                    dl.setIsDeleted(false);
                    debtLedgerRepository.save(dl);
                } else {
                    dl.setIncrease(totalAmount);
                    dl.setDecrease(totalPaid);
                    dl.setBalance(balance);
                    dl.setStatus(status);
                    if (dl.getDueDate() == null && inv.getDueDate() != null) {
                        dl.setDueDate(inv.getDueDate());
                    }
                    debtLedgerRepository.save(dl);
                }
            }
        } catch (Exception ignored) {}
    }

    // --- OPERATING COSTS ---
    @GetMapping("/operating-costs")
    public ResponseEntity<ApiResponse<List<OperatingCost>>> getAllCosts() {
        return ResponseEntity.ok(ApiResponse.ok(operatingCostRepository.findByIsDeletedFalse()));
    }

    @GetMapping("/operating-costs/{id}")
    public ResponseEntity<ApiResponse<OperatingCost>> getCostById(@PathVariable Long id) {
        return operatingCostRepository.findByIdAndIsDeletedFalse(id)
                .map(cost -> ResponseEntity.ok(ApiResponse.ok(cost)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/operating-costs")
    public ResponseEntity<ApiResponse<OperatingCost>> createCost(@RequestBody java.util.Map<String, Object> req) {
        OperatingCost cost = new OperatingCost();
        cost.setIsDeleted(false);
        
        if (req.get("amount") != null) {
            cost.setAmount(new BigDecimal(req.get("amount").toString()));
        } else {
            cost.setAmount(BigDecimal.ZERO);
        }
        
        String dateStr = req.get("costDate") != null ? req.get("costDate").toString() :
                (req.get("incurredDate") != null ? req.get("incurredDate").toString() : null);
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            if (dateStr.contains("T")) dateStr = dateStr.split("T")[0];
            try {
                cost.setCostDate(LocalDate.parse(dateStr));
            } catch (Exception e) {
                cost.setCostDate(LocalDate.now());
            }
        } else {
            cost.setCostDate(LocalDate.now());
        }
        
        String desc = req.get("description") != null ? req.get("description").toString() :
                (req.get("costName") != null ? req.get("costName").toString() : null);
        cost.setDescription(desc);
        
        String costCode = req.get("costCode") != null ? req.get("costCode").toString() : "COST-" + System.currentTimeMillis();
        cost.setCostCode(costCode);
        if (req.get("category") != null) {
            cost.setCategory(req.get("category").toString());
        }
        
        String st = req.get("status") != null ? req.get("status").toString() :
                (req.get("paymentStatus") != null ? req.get("paymentStatus").toString() : "COMPLETED");
        cost.setStatus(st);
        
        if (req.get("costCenterId") != null) {
            try {
                cost.setCostCenterId(Long.valueOf(req.get("costCenterId").toString()));
            } catch (Exception ignored) {}
        }
        
        Long bId = null;
        if (req.get("branchId") != null) {
            try {
                bId = Long.valueOf(req.get("branchId").toString().replaceAll("\\D+", ""));
            } catch (Exception ignored) {}
        }
        if (bId != null) {
            cost.setBranch(branchRepository.findById(bId).orElse(null));
        }
        if (cost.getBranch() == null) {
            List<org.example.storemanager.modules.system.entity.Branch> branches = branchRepository.findByIsDeletedFalse();
            if (!branches.isEmpty()) {
                cost.setBranch(branches.get(0));
            }
        }
        
        return ResponseEntity.status(201).body(ApiResponse.created(operatingCostRepository.save(cost)));
    }

    @PutMapping("/operating-costs/{id}")
    public ResponseEntity<ApiResponse<OperatingCost>> updateCost(@PathVariable Long id, @RequestBody java.util.Map<String, Object> req) {
        OperatingCost existing = operatingCostRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("OperatingCost", "id", id));
        if (req.get("amount") != null) {
            existing.setAmount(new BigDecimal(req.get("amount").toString()));
        }
        String dateStr = req.get("costDate") != null ? req.get("costDate").toString() :
                (req.get("incurredDate") != null ? req.get("incurredDate").toString() : null);
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            if (dateStr.contains("T")) dateStr = dateStr.split("T")[0];
            try {
                existing.setCostDate(LocalDate.parse(dateStr));
            } catch (Exception ignored) {}
        }
        if (req.get("description") != null) existing.setDescription(req.get("description").toString());
        else if (req.get("costName") != null) existing.setDescription(req.get("costName").toString());
        
        if (req.get("status") != null) existing.setStatus(req.get("status").toString());
        else if (req.get("paymentStatus") != null) existing.setStatus(req.get("paymentStatus").toString());
        
        if (req.get("costCenterId") != null) {
            try {
                existing.setCostCenterId(Long.valueOf(req.get("costCenterId").toString()));
            } catch (Exception ignored) {}
        }
        if (req.get("costCode") != null) existing.setCostCode(req.get("costCode").toString());
        if (req.get("category") != null) existing.setCategory(req.get("category").toString());
        
        if (req.get("branchId") != null) {
            try {
                Long bId = Long.valueOf(req.get("branchId").toString().replaceAll("\\D+", ""));
                existing.setBranch(branchRepository.findById(bId).orElse(existing.getBranch()));
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật chi phí thành công", operatingCostRepository.save(existing)));
    }

    @DeleteMapping("/operating-costs/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCost(@PathVariable Long id) {
        OperatingCost existing = operatingCostRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("OperatingCost", "id", id));
        existing.setIsDeleted(true);
        operatingCostRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Xóa chi phí thành công", null));
    }

    // --- TAX DUTIES ---
    @GetMapping({"/tax-duties", "/taxes"})
    public ResponseEntity<ApiResponse<List<TaxDuty>>> getAllTaxes() {
        List<TaxDuty> list = taxDutyRepository.findByIsDeletedFalse();
        if (list.isEmpty()) {
            List<TaxDuty> defaultTaxes = List.of(
                TaxDuty.builder()
                    .taxType("Thuế GTGT (VAT)")
                    .period("Q3-2026")
                    .amountDue(new BigDecimal("125000000"))
                    .amountPaid(new BigDecimal("125000000"))
                    .status("PAID")
                    .build(),
                TaxDuty.builder()
                    .taxType("Thuế Thu nhập Doanh nghiệp (CIT)")
                    .period("Q3-2026")
                    .amountDue(new BigDecimal("86000000"))
                    .amountPaid(new BigDecimal("86000000"))
                    .status("PAID")
                    .build(),
                TaxDuty.builder()
                    .taxType("Thuế Thu nhập Cá nhân (PIT)")
                    .period("08-2026")
                    .amountDue(new BigDecimal("34500000"))
                    .amountPaid(new BigDecimal("0"))
                    .status("UNPAID")
                    .build(),
                TaxDuty.builder()
                    .taxType("Thuế Môn bài")
                    .period("2026")
                    .amountDue(new BigDecimal("3000000"))
                    .amountPaid(new BigDecimal("3000000"))
                    .status("PAID")
                    .build()
            );
            defaultTaxes.forEach(t -> t.setIsDeleted(false));
            list = taxDutyRepository.saveAll(defaultTaxes);
        }
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping({"/tax-duties", "/taxes"})
    public ResponseEntity<ApiResponse<TaxDuty>> createTax(@RequestBody TaxDuty req) {
        if (req.getAmountDue() == null) req.setAmountDue(BigDecimal.ZERO);
        if (req.getAmountPaid() == null) req.setAmountPaid(BigDecimal.ZERO);
        if (req.getStatus() == null) req.setStatus("UNPAID");
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(taxDutyRepository.save(req)));
    }

    @PutMapping({"/tax-duties/{id}", "/taxes/{id}"})
    public ResponseEntity<ApiResponse<TaxDuty>> updateTax(@PathVariable Long id, @RequestBody TaxDuty req) {
        TaxDuty existing = taxDutyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaxDuty", "id", id));
        if (req.getTaxType() != null) existing.setTaxType(req.getTaxType());
        if (req.getPeriod() != null) existing.setPeriod(req.getPeriod());
        if (req.getAmountDue() != null) existing.setAmountDue(req.getAmountDue());
        if (req.getAmountPaid() != null) existing.setAmountPaid(req.getAmountPaid());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());
        return ResponseEntity.ok(ApiResponse.ok(taxDutyRepository.save(existing)));
    }

    @DeleteMapping({"/tax-duties/{id}", "/taxes/{id}"})
    public ResponseEntity<ApiResponse<Void>> deleteTax(@PathVariable Long id) {
        TaxDuty existing = taxDutyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaxDuty", "id", id));
        existing.setIsDeleted(true);
        taxDutyRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- FUND BALANCES ---
    @GetMapping({"/fund-balances", "/fund-cash"})
    public ResponseEntity<ApiResponse<List<FundBalance>>> getAllFunds() {
        List<FundBalance> list = fundBalanceRepository.findByIsDeletedFalse();
        if (list.isEmpty()) {
            List<FundBalance> defaultFunds = List.of(
                FundBalance.builder()
                    .balanceDate(LocalDate.now())
                    .branchName("Hội Sở Chính Hà Nội")
                    .managerName("Nguyễn Thị Lan (Thủ quỹ)")
                    .cashBalance(new BigDecimal("150000000"))
                    .bankBalance(new BigDecimal("2850000000"))
                    .build(),
                FundBalance.builder()
                    .balanceDate(LocalDate.now())
                    .branchName("Chi nhánh Quận 1 TP.HCM")
                    .managerName("Trần Văn Minh (Thủ quỹ)")
                    .cashBalance(new BigDecimal("85000000"))
                    .bankBalance(new BigDecimal("1620000000"))
                    .build(),
                FundBalance.builder()
                    .balanceDate(LocalDate.now())
                    .branchName("Chi nhánh Đà Nẵng")
                    .managerName("Lê Hoàng Nam (Thủ quỹ)")
                    .cashBalance(new BigDecimal("45000000"))
                    .bankBalance(new BigDecimal("950000000"))
                    .build()
            );
            defaultFunds.forEach(f -> f.setIsDeleted(false));
            list = fundBalanceRepository.saveAll(defaultFunds);
        }
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping({"/fund-balances", "/fund-cash"})
    public ResponseEntity<ApiResponse<FundBalance>> createFund(@RequestBody FundBalance req) {
        if (req.getBalanceDate() == null) req.setBalanceDate(LocalDate.now());
        if (req.getCashBalance() == null) req.setCashBalance(BigDecimal.ZERO);
        if (req.getBankBalance() == null) req.setBankBalance(BigDecimal.ZERO);
        if (req.getBranchName() == null || req.getBranchName().trim().isEmpty()) {
            req.setBranchName("Chi nhánh Hội sở chính");
        }
        if (req.getManagerName() == null || req.getManagerName().trim().isEmpty()) {
            req.setManagerName("Thủ quỹ");
        }
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(fundBalanceRepository.save(req)));
    }

    @PutMapping({"/fund-balances/{id}", "/fund-cash/{id}"})
    public ResponseEntity<ApiResponse<FundBalance>> updateFund(@PathVariable Long id, @RequestBody FundBalance req) {
        FundBalance existing = fundBalanceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("FundBalance", "id", id));
        if (req.getBalanceDate() != null) existing.setBalanceDate(req.getBalanceDate());
        if (req.getCashBalance() != null) existing.setCashBalance(req.getCashBalance());
        if (req.getBankBalance() != null) existing.setBankBalance(req.getBankBalance());
        if (req.getBranchName() != null) existing.setBranchName(req.getBranchName());
        if (req.getManagerName() != null) existing.setManagerName(req.getManagerName());
        return ResponseEntity.ok(ApiResponse.ok(fundBalanceRepository.save(existing)));
    }

    @DeleteMapping({"/fund-balances/{id}", "/fund-cash/{id}"})
    public ResponseEntity<ApiResponse<Void>> deleteFund(@PathVariable Long id) {
        FundBalance existing = fundBalanceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("FundBalance", "id", id));
        existing.setIsDeleted(true);
        fundBalanceRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- ORDER PAYMENTS ---
    @GetMapping("/order-payments")
    public ResponseEntity<ApiResponse<List<OrderPayment>>> getAllOrderPayments() {
        return ResponseEntity.ok(ApiResponse.ok(orderPaymentRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/order-payments")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<OrderPayment>> createOrderPayment(@RequestBody java.util.Map<String, Object> req) {
        Long invoiceId = req.get("invoiceId") != null ? Long.valueOf(req.get("invoiceId").toString()) : null;
        Long methodId = req.get("methodId") != null ? Long.valueOf(req.get("methodId").toString()) : null;
        java.math.BigDecimal amountPaid = req.get("amountPaid") != null ? new java.math.BigDecimal(req.get("amountPaid").toString()) : java.math.BigDecimal.ZERO;
        String ref = (String) req.get("transactionRef");

        if (invoiceId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invoiceId là bắt buộc. Vui lòng chọn hóa đơn cần gạch nợ.");
        }
        if (methodId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "methodId là bắt buộc. Vui lòng chọn phương thức thanh toán.");
        }

        org.example.storemanager.modules.sales.entity.ExportInvoice inv = exportInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hóa đơn không tồn tại (invoiceId=" + invoiceId + ")"));

        PaymentMethod pm = paymentMethodRepository.findById(methodId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phương thức thanh toán không tồn tại (methodId=" + methodId + ")"));

        OrderPayment payment = new OrderPayment();
        payment.setAmountPaid(amountPaid);
        payment.setTransactionRef(ref);
        payment.setPaymentDate(java.time.LocalDateTime.now());
        payment.setInvoice(inv);
        payment.setPaymentMethod(pm);
        payment.setIsDeleted(false);
        OrderPayment savedPayment = orderPaymentRepository.save(payment);

        if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            // Tính tổng thanh toán tích lũy cho hóa đơn này
            List<OrderPayment> allPayments = orderPaymentRepository.findByInvoiceIdAndIsDeletedFalse(invoiceId);
            BigDecimal totalPaid = allPayments.stream()
                    .map(p -> p.getAmountPaid() != null ? p.getAmountPaid() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalInvoiceAmount = inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal remainingDebt = totalInvoiceAmount.subtract(totalPaid).max(BigDecimal.ZERO);

            if (totalInvoiceAmount.compareTo(BigDecimal.ZERO) > 0 && totalPaid.compareTo(totalInvoiceAmount) >= 0) {
                inv.setStatus("COMPLETED");
            } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
                inv.setStatus("PARTIAL_PAID");
            }
            exportInvoiceRepository.save(inv);

            // Ghi nhận / Cập nhật công nợ trong DebtLedger
            try {
                final String invCode = inv.getInvoiceCode();
                DebtLedger debt = debtLedgerRepository.findByIsDeletedFalse().stream()
                        .filter(d -> "CUSTOMER".equalsIgnoreCase(d.getEntityType()) && d.getRefCode() != null && d.getRefCode().equalsIgnoreCase(invCode))
                        .findFirst().orElse(null);

                if (debt == null) {
                    debt = DebtLedger.builder()
                            .transactionDate(inv.getInvoiceDate() != null ? inv.getInvoiceDate() : LocalDateTime.now())
                            .refCode(inv.getInvoiceCode())
                            .partnerId(inv.getCustomer() != null ? inv.getCustomer().getId() : 1L)
                            .entityName(inv.getCustomer() != null ? inv.getCustomer().getName() : "Khách hàng")
                            .entityType("CUSTOMER")
                            .dueDate(inv.getDueDate())
                            .accountManager("Kế toán công nợ bán hàng")
                            .build();
                    debt.setIsDeleted(false);
                }
                debt.setIncrease(totalInvoiceAmount);
                debt.setDecrease(totalPaid);
                debt.setBalance(remainingDebt);
                debt.setLastPaymentDate(LocalDateTime.now());
                debt.setStatus(remainingDebt.compareTo(BigDecimal.ZERO) == 0 ? "SETTLED" : "NORMAL");
                debt.setNotes("Thanh toán hóa đơn " + inv.getInvoiceCode() + " - Đã trả: " + totalPaid.toPlainString() + " / " + totalInvoiceAmount.toPlainString());
                debtLedgerRepository.save(debt);
            } catch (Exception ignored) {}

            // Tạo ReceiptVoucher ghi nhận tiền vào quỹ
            try {
                ReceiptVoucher rv = ReceiptVoucher.builder()
                        .voucherCode("PT-INV-" + inv.getId() + "-" + (System.currentTimeMillis() % 10000))
                        .voucherDate(LocalDateTime.now())
                        .invoiceCode(inv.getInvoiceCode())
                        .amount(amountPaid)
                        .status("COMPLETED")
                        .payerName(inv.getCustomer() != null ? inv.getCustomer().getName() : "Khách hàng thanh toán hóa đơn")
                        .paymentMethod(pm.getMethodName() != null ? pm.getMethodName() : "TIỀN_MẶT")
                        .notes("Thu tiền hóa đơn " + inv.getInvoiceCode() + " (Đợt thanh toán: " + amountPaid.toPlainString() + " ₫, còn nợ: " + remainingDebt.toPlainString() + " ₫)")
                        .build();
                rv.setIsDeleted(false);
                receiptVoucherRepository.save(rv);
                increaseFundBalance(rv);
            } catch (Exception ignored) {}
        }

        return ResponseEntity.status(201).body(ApiResponse.created(savedPayment));
    }

    @DeleteMapping("/order-payments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrderPayment(@PathVariable Long id) {
        OrderPayment payment = orderPaymentRepository.findById(id).orElse(null);
        if (payment != null) {
            payment.setIsDeleted(true);
            orderPaymentRepository.save(payment);
        }
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- TRANSACTION REASONS ---
    private TransactionReason createDefaultReason(String code, String name, String type, String accCode) {
        TransactionReason r = new TransactionReason();
        r.setReasonCode(code);
        r.setReasonName(name);
        r.setType(type);
        r.setAccountingCode(accCode);
        r.setIsDeleted(false);
        return r;
    }

    @GetMapping("/transaction-reasons")
    public ResponseEntity<ApiResponse<List<TransactionReason>>> getAllReasons() {
        List<TransactionReason> list = transactionReasonRepository.findByIsDeletedFalse();
        if (list.isEmpty()) {
            List<TransactionReason> defaultReasons = List.of(
                createDefaultReason("SALES_REVENUE", "Thu tiền bán hàng & dịch vụ", "RECEIPT", "511"),
                createDefaultReason("DEBT_COLLECTION", "Thu hồi công nợ khách hàng", "RECEIPT", "131"),
                createDefaultReason("CAPITAL_INJECTION", "Góp vốn bổ sung / đầu tư", "RECEIPT", "411"),
                createDefaultReason("SUPPLIER_PAYMENT", "Thanh toán tiền hàng cho Nhà cung cấp", "PAYMENT", "331"),
                createDefaultReason("OPERATING_COST", "Chi phí thuê mặt bằng & vận hành", "PAYMENT", "642"),
                createDefaultReason("SALARY_EXPENSE", "Chi trả lương & thưởng nhân sự", "PAYMENT", "334"),
                createDefaultReason("TAX_DUTY", "Nộp thuế & nghĩa vụ nhà nước", "PAYMENT", "333")
            );
            list = transactionReasonRepository.saveAll(defaultReasons);
        }
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/transaction-reasons")
    public ResponseEntity<ApiResponse<TransactionReason>> createReason(@RequestBody TransactionReason req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(transactionReasonRepository.save(req)));
    }

    @PutMapping("/transaction-reasons/{id}")
    public ResponseEntity<ApiResponse<TransactionReason>> updateReason(@PathVariable Long id, @RequestBody TransactionReason req) {
        TransactionReason existing = transactionReasonRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransactionReason", "id", id));
        if (req.getReasonName() != null) existing.setReasonName(req.getReasonName());
        if (req.getReasonCode() != null) existing.setReasonCode(req.getReasonCode());
        if (req.getType() != null) existing.setType(req.getType());
        if (req.getAccountingCode() != null) existing.setAccountingCode(req.getAccountingCode());
        if (req.getDescription() != null) existing.setDescription(req.getDescription());
        return ResponseEntity.ok(ApiResponse.ok(transactionReasonRepository.save(existing)));
    }

    @DeleteMapping("/transaction-reasons/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReason(@PathVariable Long id) {
        TransactionReason existing = transactionReasonRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransactionReason", "id", id));
        existing.setIsDeleted(true);
        transactionReasonRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- PAYMENT METHODS ---
    @GetMapping("/payment-methods")
    public ResponseEntity<ApiResponse<List<PaymentMethod>>> getAllMethods() {
        List<PaymentMethod> list = paymentMethodRepository.findByIsDeletedFalse();
        if (list.isEmpty()) {
            List<PaymentMethod> defaultMethods = List.of(
                PaymentMethod.builder().methodCode("CASH").methodName("Tiền mặt").type("CASH").status("ACTIVE").sortOrder(1).build(),
                PaymentMethod.builder().methodCode("BANK_TRANSFER").methodName("Chuyển khoản ngân hàng").type("BANK_TRANSFER").status("ACTIVE").sortOrder(2).build(),
                PaymentMethod.builder().methodCode("CARD").methodName("Thẻ tín dụng / ATM").type("CARD").status("ACTIVE").sortOrder(3).build(),
                PaymentMethod.builder().methodCode("E_WALLET").methodName("Ví điện tử").type("E_WALLET").status("ACTIVE").sortOrder(4).build()
            );
            defaultMethods.forEach(m -> m.setIsDeleted(false));
            list = paymentMethodRepository.saveAll(defaultMethods);
        }
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    private void createJournalEntryForReceipt(ReceiptVoucher rv) {
        String creditAccountCode = "511";
        String reasonCode = rv.getReason() != null ? rv.getReason().getReasonCode() : "";
        if ("DEBT_COLLECTION".equals(reasonCode)) {
            creditAccountCode = "131";
            DebtLedger debt = DebtLedger.builder()
                .transactionDate(rv.getVoucherDate())
                .refCode(rv.getVoucherCode())
                .increase(BigDecimal.ZERO)
                .decrease(rv.getAmount())
                .balance(BigDecimal.ZERO)
                .partnerId(1L)
                .build();
            debt.setIsDeleted(false);
            debtLedgerRepository.save(debt);
        } else if ("FUND_SURPLUS".equals(reasonCode)) {
            creditAccountCode = "3381";
        }

        final String creditCode = creditAccountCode;
        ChartOfAccount debitAcc = chartOfAccountRepository.findByAccountCodeAndIsDeletedFalse("111")
                .orElseGet(() -> chartOfAccountRepository.save(ChartOfAccount.builder().accountCode("111").accountName("Tiền mặt").type("ASSET").isActive(true).build()));
        ChartOfAccount creditAcc = chartOfAccountRepository.findByAccountCodeAndIsDeletedFalse(creditCode)
                .orElseGet(() -> chartOfAccountRepository.save(ChartOfAccount.builder().accountCode(creditCode).accountName("Tài khoản đối ứng").type("REVENUE").isActive(true).build()));

        JournalEntry je = JournalEntry.builder()
                .entryDate(rv.getVoucherDate())
                .referenceCode(rv.getVoucherCode())
                .description("Hạch toán tự động phiếu thu " + rv.getVoucherCode())
                .totalAmount(rv.getAmount())
                .build();
        je.setIsDeleted(false);
        JournalEntry savedJe = journalEntryRepository.save(je);

        JournalEntryLine debitLine = JournalEntryLine.builder()
                .journalEntry(savedJe)
                .account(debitAcc)
                .debitAmount(rv.getAmount())
                .creditAmount(BigDecimal.ZERO)
                .description("Nợ TK 111 - Phiếu thu " + rv.getVoucherCode())
                .build();
        debitLine.setIsDeleted(false);
        journalEntryLineRepository.save(debitLine);

        JournalEntryLine creditLine = JournalEntryLine.builder()
                .journalEntry(savedJe)
                .account(creditAcc)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(rv.getAmount())
                .description("Có TK " + creditCode + " - Phiếu thu " + rv.getVoucherCode())
                .build();
        creditLine.setIsDeleted(false);
        journalEntryLineRepository.save(creditLine);
    }

    private void createJournalEntryForPayment(PaymentVoucher pv) {
        String debitAccountCode = "642";
        String reasonCode = pv.getReason() != null ? pv.getReason().getReasonCode() : "";
        if ("SUPPLIER_PAYMENT".equals(reasonCode) || "SUPPLIER_DEPOSIT".equals(reasonCode)) {
            debitAccountCode = "331";
            Long resolvedPartnerId = 1L;
            if (pv.getInvoiceCode() != null && !pv.getInvoiceCode().trim().isEmpty()) {
                String invCode = pv.getInvoiceCode().trim();
                org.example.storemanager.modules.sales.entity.PurchaseOrder po = null;
                if (invCode.startsWith("INV-MH-")) {
                    try {
                        Long idVal = Long.parseLong(invCode.replace("INV-MH-", ""));
                        po = purchaseOrderRepository.findById(idVal).orElse(null);
                    } catch (Exception e) {}
                } else {
                    po = purchaseOrderRepository.findByPoCodeAndIsDeletedFalse(invCode).orElse(null);
                }
                if (po != null && po.getSupplier() != null) {
                    resolvedPartnerId = po.getSupplier().getId();
                }
            }

            DebtLedger debt = DebtLedger.builder()
                .transactionDate(pv.getVoucherDate())
                .refCode(pv.getVoucherCode())
                .increase(BigDecimal.ZERO)
                .decrease(pv.getAmount())
                .balance(BigDecimal.ZERO)
                .partnerId(resolvedPartnerId)
                .build();
            debt.setIsDeleted(false);
            debtLedgerRepository.save(debt);
        } else if ("FUND_DEFICIT".equals(reasonCode)) {
            debitAccountCode = "1381";
        }

        final String debitCode = debitAccountCode;
        ChartOfAccount debitAcc = chartOfAccountRepository.findByAccountCodeAndIsDeletedFalse(debitCode)
                .orElseGet(() -> chartOfAccountRepository.save(ChartOfAccount.builder().accountCode(debitCode).accountName("Tài khoản nợ").type("EXPENSE").isActive(true).build()));
        ChartOfAccount creditAcc = chartOfAccountRepository.findByAccountCodeAndIsDeletedFalse("111")
                .orElseGet(() -> chartOfAccountRepository.save(ChartOfAccount.builder().accountCode("111").accountName("Tiền mặt").type("ASSET").isActive(true).build()));

        JournalEntry je = JournalEntry.builder()
                .entryDate(pv.getVoucherDate())
                .referenceCode(pv.getVoucherCode())
                .description("Hạch toán tự động phiếu chi " + pv.getVoucherCode())
                .totalAmount(pv.getAmount())
                .build();
        je.setIsDeleted(false);
        JournalEntry savedJe = journalEntryRepository.save(je);

        JournalEntryLine debitLine = JournalEntryLine.builder()
                .journalEntry(savedJe)
                .account(debitAcc)
                .debitAmount(pv.getAmount())
                .creditAmount(BigDecimal.ZERO)
                .description("Nợ TK " + debitCode + " - Phiếu chi " + pv.getVoucherCode())
                .build();
        debitLine.setIsDeleted(false);
        journalEntryLineRepository.save(debitLine);

        JournalEntryLine creditLine = JournalEntryLine.builder()
                .journalEntry(savedJe)
                .account(creditAcc)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(pv.getAmount())
                .description("Có TK 111 - Phiếu chi " + pv.getVoucherCode())
                .build();
        creditLine.setIsDeleted(false);
        journalEntryLineRepository.save(creditLine);
    }

    private void createStornoEntry(String referenceCode) {
        journalEntryRepository.findByReferenceCodeAndIsDeletedFalse(referenceCode).ifPresent(original -> {
            String revCode = referenceCode + "-REV";
            if (journalEntryRepository.findByReferenceCodeAndIsDeletedFalse(revCode).isPresent()) {
                return;
            }
            JournalEntry revJe = JournalEntry.builder()
                    .entryDate(LocalDateTime.now())
                    .referenceCode(revCode)
                    .description("Bút toán đảo hoàn tác chứng từ " + referenceCode)
                    .totalAmount(original.getTotalAmount())
                    .build();
            revJe.setIsDeleted(false);
            JournalEntry savedRev = journalEntryRepository.save(revJe);

            List<JournalEntryLine> originalLines = journalEntryLineRepository.findByJournalEntryIdAndIsDeletedFalse(original.getId());
            for (JournalEntryLine line : originalLines) {
                JournalEntryLine revLine = JournalEntryLine.builder()
                        .journalEntry(savedRev)
                        .account(line.getAccount())
                        .debitAmount(line.getCreditAmount())
                        .creditAmount(line.getDebitAmount())
                        .description("Bút toán đảo hoàn tác: " + line.getDescription())
                        .build();
                revLine.setIsDeleted(false);
                journalEntryLineRepository.save(revLine);
            }
        });
    }

    private void checkFundBalance(PaymentVoucher pv) {
        String method = pv.getPaymentMethod();
        BigDecimal checkAmount = pv.getAmount() != null ? pv.getAmount() : BigDecimal.ZERO;
        if (checkAmount.compareTo(BigDecimal.ZERO) <= 0) return;

        Long branchId = pv.getBranch() != null ? pv.getBranch().getId() : null;
        FundBalance fb = fundBalanceRepository.findByIsDeletedFalse().stream()
            .filter(b -> branchId == null || (b.getBranch() != null && b.getBranch().getId().equals(branchId)))
            .findFirst().orElse(null);

        if (fb != null) {
            BigDecimal cashBal = fb.getCashBalance() != null ? fb.getCashBalance() : BigDecimal.ZERO;
            BigDecimal bankBal = fb.getBankBalance() != null ? fb.getBankBalance() : BigDecimal.ZERO;
            if ("CASH".equalsIgnoreCase(method)) {
                if (cashBal.compareTo(checkAmount) < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Không đủ số dư quỹ tiền mặt để thực hiện chi (Số dư hiện tại: " + cashBal + ", Số tiền chi: " + checkAmount + ")");
                }
                fb.setCashBalance(cashBal.subtract(checkAmount));
                fundBalanceRepository.save(fb);
            } else if ("BANK_TRANSFER".equalsIgnoreCase(method) || "BANK".equalsIgnoreCase(method)) {
                if (bankBal.compareTo(checkAmount) < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Không đủ số dư tài khoản ngân hàng để thực hiện chi (Số dư hiện tại: " + bankBal + ", Số tiền chi: " + checkAmount + ")");
                }
                fb.setBankBalance(bankBal.subtract(checkAmount));
                fundBalanceRepository.save(fb);
            }
        }
    }

    private void increaseFundBalance(ReceiptVoucher rv) {
        BigDecimal amount = rv.getAmount() != null ? rv.getAmount() : BigDecimal.ZERO;
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return;

        String method = rv.getPaymentMethod() != null ? rv.getPaymentMethod() : "CASH";
        Long branchId = rv.getBranch() != null ? rv.getBranch().getId() : null;
        FundBalance fb = fundBalanceRepository.findByIsDeletedFalse().stream()
            .filter(b -> branchId == null || (b.getBranch() != null && b.getBranch().getId().equals(branchId)))
            .findFirst().orElse(null);

        if (fb != null) {
            BigDecimal cashBal = fb.getCashBalance() != null ? fb.getCashBalance() : BigDecimal.ZERO;
            BigDecimal bankBal = fb.getBankBalance() != null ? fb.getBankBalance() : BigDecimal.ZERO;
            if ("CASH".equalsIgnoreCase(method)) {
                fb.setCashBalance(cashBal.add(amount));
                fundBalanceRepository.save(fb);
            } else if ("BANK_TRANSFER".equalsIgnoreCase(method) || "BANK".equalsIgnoreCase(method)) {
                fb.setBankBalance(bankBal.add(amount));
                fundBalanceRepository.save(fb);
            }
        }
    }
}
