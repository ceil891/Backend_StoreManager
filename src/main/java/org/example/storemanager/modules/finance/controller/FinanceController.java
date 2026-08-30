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
        }
        return ResponseEntity.status(201).body(ApiResponse.created(saved));
    }

    @PutMapping({"/receipt-vouchers/{id}", "/receipts/{id}"})
    public ResponseEntity<ApiResponse<ReceiptVoucher>> updateReceipt(@PathVariable Long id, @RequestBody ReceiptVoucher req) {
        ReceiptVoucher existing = receiptVoucherRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReceiptVoucher", "id", id));
        
        String oldStatus = existing.getStatus();
        existing.setPayerName(req.getPayerName());
        existing.setAmount(req.getAmount());
        if (req.getStatus() != null) {
            existing.setStatus(req.getStatus());
        }
        
        ReceiptVoucher saved = receiptVoucherRepository.save(existing);
        
        if ("COMPLETED".equalsIgnoreCase(saved.getStatus()) && !"COMPLETED".equalsIgnoreCase(oldStatus)) {
            createJournalEntryForReceipt(saved);
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không được phép xóa vật lý chứng từ đã duyệt. Vui lòng chuyển trạng thái sang CANCELLED.");
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
        if (po != null) {
            java.math.BigDecimal amountPaid = pv.getAmount() != null ? pv.getAmount() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalCost = po.getTotalAmount() != null ? po.getTotalAmount() : java.math.BigDecimal.ZERO;
            po.setAdvanceAmount(amountPaid);
            if (totalCost.compareTo(java.math.BigDecimal.ZERO) > 0 && amountPaid.compareTo(totalCost) >= 0) {
                po.setPaymentStatus("PAID");
            } else if (amountPaid.compareTo(java.math.BigDecimal.ZERO) > 0) {
                po.setPaymentStatus("PARTIAL_ADVANCE");
            } else {
                po.setPaymentStatus("UNPAID");
            }
            purchaseOrderRepository.save(po);
        }
    }

    private void revertPurchaseOrderPaymentStatus(PaymentVoucher pv) {
        if (pv.getInvoiceCode() == null || pv.getInvoiceCode().trim().isEmpty()) return;
        String invCode = pv.getInvoiceCode().trim();
        org.example.storemanager.modules.sales.entity.PurchaseOrder po = purchaseOrderRepository.findByPoCodeAndIsDeletedFalse(invCode).orElse(null);
        if (po != null) {
            po.setPaymentStatus("UNPAID");
            po.setAdvanceAmount(java.math.BigDecimal.ZERO);
            purchaseOrderRepository.save(po);
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
        return ResponseEntity.ok(ApiResponse.ok(debtLedgerRepository.findByIsDeletedFalse()));
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
        if (req.getIncrease() != null) existing.setIncrease(req.getIncrease());
        if (req.getDecrease() != null) existing.setDecrease(req.getDecrease());
        if (req.getBalance() != null) existing.setBalance(req.getBalance());
        if (req.getTransactionDate() != null) existing.setTransactionDate(req.getTransactionDate());
        if (req.getEntityName() != null) existing.setEntityName(req.getEntityName());
        if (req.getEntityType() != null) existing.setEntityType(req.getEntityType());
        if (req.getDueDate() != null) existing.setDueDate(req.getDueDate());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());
        if (req.getLastPaymentDate() != null) existing.setLastPaymentDate(req.getLastPaymentDate());
        if (req.getAccountManager() != null) existing.setAccountManager(req.getAccountManager());
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

    // --- OPERATING COSTS ---
    @GetMapping("/operating-costs")
    public ResponseEntity<ApiResponse<List<OperatingCost>>> getAllCosts() {
        return ResponseEntity.ok(ApiResponse.ok(operatingCostRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/operating-costs")
    public ResponseEntity<ApiResponse<OperatingCost>> createCost(@RequestBody OperatingCost req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(operatingCostRepository.save(req)));
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
    public ResponseEntity<ApiResponse<OrderPayment>> createOrderPayment(@RequestBody java.util.Map<String, Object> req) {
        Long invoiceId = req.get("invoiceId") != null ? Long.valueOf(req.get("invoiceId").toString()) : null;
        Long methodId = req.get("methodId") != null ? Long.valueOf(req.get("methodId").toString()) : null;
        java.math.BigDecimal amountPaid = req.get("amountPaid") != null ? new java.math.BigDecimal(req.get("amountPaid").toString()) : java.math.BigDecimal.ZERO;
        String ref = (String) req.get("transactionRef");

        OrderPayment payment = new OrderPayment();
        payment.setAmountPaid(amountPaid);
        payment.setTransactionRef(ref);
        payment.setPaymentDate(java.time.LocalDateTime.now());
        payment.setIsDeleted(false);

        if (invoiceId != null) {
            payment.setInvoice(exportInvoiceRepository.findById(invoiceId).orElse(null));
        }
        if (methodId != null) {
            payment.setPaymentMethod(paymentMethodRepository.findById(methodId).orElse(null));
        }

        if (payment.getInvoice() == null) {
            List<org.example.storemanager.modules.sales.entity.ExportInvoice> invoices = exportInvoiceRepository.findAll();
            if (!invoices.isEmpty()) {
                payment.setInvoice(invoices.get(0));
            }
        }
        if (payment.getPaymentMethod() == null) {
            List<PaymentMethod> methods = paymentMethodRepository.findByIsDeletedFalse();
            if (!methods.isEmpty()) {
                payment.setPaymentMethod(methods.get(0));
            }
        }

        return ResponseEntity.status(201).body(ApiResponse.created(orderPaymentRepository.save(payment)));
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
        return ResponseEntity.ok(ApiResponse.ok(paymentMethodRepository.findByIsDeletedFalse()));
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
                if (cashBal.compareTo(checkAmount) < 0 && cashBal.compareTo(BigDecimal.ZERO) > 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Không đủ số dư quỹ tiền mặt để thực hiện chi (Số dư hiện tại: " + cashBal + ", Số tiền chi: " + checkAmount + ")");
                }
                fb.setCashBalance(cashBal.subtract(checkAmount));
                fundBalanceRepository.save(fb);
            } else if ("BANK_TRANSFER".equalsIgnoreCase(method) || "BANK".equalsIgnoreCase(method)) {
                if (bankBal.compareTo(checkAmount) < 0 && bankBal.compareTo(BigDecimal.ZERO) > 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Không đủ số dư tài khoản ngân hàng để thực hiện chi (Số dư hiện tại: " + bankBal + ", Số tiền chi: " + checkAmount + ")");
                }
                fb.setBankBalance(bankBal.subtract(checkAmount));
                fundBalanceRepository.save(fb);
            }
        }
    }
}
