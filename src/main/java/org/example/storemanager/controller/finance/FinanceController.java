package org.example.storemanager.controller.finance;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.entity.finance.*;
import org.example.storemanager.repository.finance.*;
import org.example.storemanager.repository.sales.ExportInvoiceRepository;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.example.storemanager.entity.advancedaccounting.JournalEntry;
import org.example.storemanager.entity.advancedaccounting.JournalEntryLine;
import org.example.storemanager.entity.advancedaccounting.ChartOfAccount;
import org.example.storemanager.repository.advancedaccounting.JournalEntryRepository;
import org.example.storemanager.repository.advancedaccounting.JournalEntryLineRepository;
import org.example.storemanager.repository.advancedaccounting.ChartOfAccountRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private final ChartOfAccountRepository chartOfAccountRepository;

    // --- BANK ACCOUNTS ---
    @GetMapping("/bank-accounts")
    @PreAuthorize("@securityEvaluator.hasPermission('finance:bank:view')")
    public ResponseEntity<ApiResponse<List<BankAccount>>> getAllBankAccounts() {
        return ResponseEntity.ok(ApiResponse.ok(bankAccountRepository.findByIsDeletedFalse()));
    }

    @GetMapping("/bank-accounts/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('finance:bank:view')")
    public ResponseEntity<ApiResponse<BankAccount>> getBankAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(bankAccountRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount", "id", id))));
    }

    @PostMapping("/bank-accounts")
    @PreAuthorize("@securityEvaluator.hasPermission('finance:bank:create')")
    public ResponseEntity<ApiResponse<BankAccount>> createBankAccount(@RequestBody BankAccount req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(bankAccountRepository.save(req)));
    }

    @PutMapping("/bank-accounts/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('finance:bank:update')")
    public ResponseEntity<ApiResponse<BankAccount>> updateBankAccount(@PathVariable Long id, @RequestBody BankAccount req) {
        BankAccount existing = bankAccountRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount", "id", id));
        existing.setBankName(req.getBankName());
        existing.setAccountNumber(req.getAccountNumber());
        existing.setAccountHolder(req.getAccountHolder());
        existing.setBranchName(req.getBranchName());
        existing.setIsActive(req.getIsActive());
        return ResponseEntity.ok(ApiResponse.ok(bankAccountRepository.save(existing)));
    }

    @DeleteMapping("/bank-accounts/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('finance:bank:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteBankAccount(@PathVariable Long id) {
        BankAccount existing = bankAccountRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount", "id", id));
        existing.setIsDeleted(true);
        bankAccountRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- RECEIPT VOUCHERS ---
    @GetMapping("/receipt-vouchers")
    public ResponseEntity<ApiResponse<List<ReceiptVoucher>>> getAllReceipts() {
        return ResponseEntity.ok(ApiResponse.ok(receiptVoucherRepository.findByIsDeletedFalse()));
    }

    @GetMapping("/receipt-vouchers/{id}")
    public ResponseEntity<ApiResponse<ReceiptVoucher>> getReceiptById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(receiptVoucherRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReceiptVoucher", "id", id))));
    }

    @PostMapping("/receipt-vouchers")
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

    @PutMapping("/receipt-vouchers/{id}")
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

    @DeleteMapping("/receipt-vouchers/{id}")
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
    @GetMapping("/payment-vouchers")
    public ResponseEntity<ApiResponse<List<PaymentVoucher>>> getAllPayments() {
        return ResponseEntity.ok(ApiResponse.ok(paymentVoucherRepository.findByIsDeletedFalse()));
    }

    @GetMapping("/payment-vouchers/{id}")
    public ResponseEntity<ApiResponse<PaymentVoucher>> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(paymentVoucherRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentVoucher", "id", id))));
    }

    @PostMapping("/payment-vouchers")
    public ResponseEntity<ApiResponse<PaymentVoucher>> createPayment(@RequestBody PaymentVoucher req) {
        req.setIsDeleted(false);
        if (req.getVoucherCode() == null || req.getVoucherCode().trim().isEmpty()) {
            req.setVoucherCode("PC-PAY-" + System.currentTimeMillis());
        }
        if (req.getVoucherDate() == null) {
            req.setVoucherDate(LocalDateTime.now());
        }
        PaymentVoucher saved = paymentVoucherRepository.save(req);
        if ("COMPLETED".equalsIgnoreCase(saved.getStatus())) {
            createJournalEntryForPayment(saved);
        }
        return ResponseEntity.status(201).body(ApiResponse.created(saved));
    }

    @PutMapping("/payment-vouchers/{id}")
    public ResponseEntity<ApiResponse<PaymentVoucher>> updatePayment(@PathVariable Long id, @RequestBody PaymentVoucher req) {
        PaymentVoucher existing = paymentVoucherRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentVoucher", "id", id));
        
        String oldStatus = existing.getStatus();
        existing.setReceiverName(req.getReceiverName());
        existing.setAmount(req.getAmount());
        if (req.getStatus() != null) {
            existing.setStatus(req.getStatus());
        }
        
        PaymentVoucher saved = paymentVoucherRepository.save(existing);
        
        if ("COMPLETED".equalsIgnoreCase(saved.getStatus()) && !"COMPLETED".equalsIgnoreCase(oldStatus)) {
            createJournalEntryForPayment(saved);
        } else if ("CANCELLED".equalsIgnoreCase(saved.getStatus()) && !"CANCELLED".equalsIgnoreCase(oldStatus)) {
            createStornoEntry(saved.getVoucherCode());
        }
        
        return ResponseEntity.ok(ApiResponse.ok(saved));
    }

    @DeleteMapping("/payment-vouchers/{id}")
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
    @GetMapping("/debt-ledgers")
    public ResponseEntity<ApiResponse<List<DebtLedger>>> getAllDebts() {
        return ResponseEntity.ok(ApiResponse.ok(debtLedgerRepository.findByIsDeletedFalse()));
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
    @GetMapping("/tax-duties")
    public ResponseEntity<ApiResponse<List<TaxDuty>>> getAllTaxes() {
        return ResponseEntity.ok(ApiResponse.ok(taxDutyRepository.findByIsDeletedFalse()));
    }

    // --- FUND BALANCES ---
    @GetMapping("/fund-balances")
    public ResponseEntity<ApiResponse<List<FundBalance>>> getAllFunds() {
        return ResponseEntity.ok(ApiResponse.ok(fundBalanceRepository.findByIsDeletedFalse()));
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
            List<org.example.storemanager.entity.sales.ExportInvoice> invoices = exportInvoiceRepository.findAll();
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
    @GetMapping("/transaction-reasons")
    public ResponseEntity<ApiResponse<List<TransactionReason>>> getAllReasons() {
        return ResponseEntity.ok(ApiResponse.ok(transactionReasonRepository.findByIsDeletedFalse()));
    }

    // --- PAYMENT METHODS ---
    @GetMapping("/payment-methods")
    public ResponseEntity<ApiResponse<List<PaymentMethod>>> getAllMethods() {
        return ResponseEntity.ok(ApiResponse.ok(paymentMethodRepository.findByIsDeletedFalse()));
    }

    // --- PAYROLL ---
    @GetMapping("/payrolls")
    public ResponseEntity<ApiResponse<List<Payroll>>> getAllPayrolls() {
        return ResponseEntity.ok(ApiResponse.ok(payrollRepository.findByIsDeletedFalse()));
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
            DebtLedger debt = DebtLedger.builder()
                .transactionDate(pv.getVoucherDate())
                .refCode(pv.getVoucherCode())
                .increase(BigDecimal.ZERO)
                .decrease(pv.getAmount())
                .balance(BigDecimal.ZERO)
                .partnerId(1L)
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
}
