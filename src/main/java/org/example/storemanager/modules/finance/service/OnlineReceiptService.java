package org.example.storemanager.modules.finance.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.finance.entity.*;
import org.example.storemanager.modules.finance.repository.*;
import org.example.storemanager.modules.sales.entity.SaleOrder;
import org.example.storemanager.shared.service.DocumentSequenceService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Posts confirmed online collections in the same transaction as the order. */
@Service
@Transactional
@RequiredArgsConstructor
public class OnlineReceiptService {
    private final ReceiptVoucherRepository receipts;
    private final PaymentMethodRepository methods;
    private final BankAccountRepository accounts;
    private final FundBalanceRepository funds;
    private final DocumentSequenceService sequence;
    private final EntityManager entityManager;

    public boolean isOnline(SaleOrder order) {
        return order != null && ("ONLINE".equalsIgnoreCase(order.getOrderOrigin())
                || "ONLINE_STORE".equalsIgnoreCase(order.getOrderOrigin())
                || (order.getOrderCode() != null && order.getOrderCode().startsWith("ONLINE-")));
    }

    public void collect(SaleOrder order) {
        if (!isOnline(order) || "CANCELLED".equalsIgnoreCase(order.getStatus())
                || "RETURNED".equalsIgnoreCase(order.getStatus())) return;
        String status = Objects.toString(order.getPaymentStatus(), "UNPAID").toUpperCase(Locale.ROOT);
        if (!List.of("PAID", "PARTIAL", "PARTIAL_PAID").contains(status)) return;
        BigDecimal total = zero(order.getFinalAmount() != null ? order.getFinalAmount() : order.getTotalAmount());
        BigDecimal paid = "PAID".equals(status) ? total : zero(order.getPaidAmount());
        if (paid.signum() < 0 || paid.compareTo(total) > 0) throw invalid("Số tiền đã thu không hợp lệ");
        if (paid.signum() == 0) return;
        // Serialize confirmations of the same order. The existing entity version also
        // rejects concurrent stale changes made before this lock is acquired.
        entityManager.lock(order, LockModeType.PESSIMISTIC_WRITE);
        BigDecimal recorded = receipts.findByIsDeletedFalse().stream()
                .filter(r -> "COMPLETED".equalsIgnoreCase(r.getStatus()) || "APPROVED".equalsIgnoreCase(r.getStatus()))
                .filter(r -> ("SALE_ORDER".equals(r.getSourceDocumentType()) && Objects.equals(order.getId(), r.getSourceDocumentId()))
                        || Objects.equals(order.getOrderCode(), r.getInvoiceCode()))
                .map(r -> zero(r.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal delta = paid.subtract(recorded);
        if (delta.signum() <= 0) return;

        PaymentMethod configured = order.getPaymentMethodId() == null ? null
                : methods.findByIdAndIsDeletedFalse(order.getPaymentMethodId()).orElse(null);
        if (configured == null) configured = methods.findByIsDeletedFalse().stream()
                .filter(m -> Objects.toString(m.getMethodCode(), "").equalsIgnoreCase(Objects.toString(order.getPaymentMethodCode(), "")))
                .findFirst().orElse(null);
        String kind = Objects.toString(configured != null ? configured.getType() : order.getPaymentMethodCode(), "")
                .toUpperCase(Locale.ROOT);
        boolean cash = List.of("CASH", "COD", "TIEN_MAT", "TIỀN MẶT").contains(kind);
        boolean bank = List.of("BANK", "BANK_TRANSFER", "TRANSFER", "CK", "CHUYEN_KHOAN", "VIETQR", "CARD", "CREDIT_CARD", "VNPAY", "E_WALLET").contains(kind);
        if (!cash && !bank) throw invalid("Chưa cấu hình quỹ/tài khoản cho phương thức thanh toán của đơn");
        if (order.getBranch() == null) throw invalid("Chưa xác định chi nhánh ghi nhận tiền thu");
        Long branchId = order.getBranch().getId();
        List<FundBalance> branchFunds = funds.findByIsDeletedFalse().stream()
                .filter(f -> f.getBranch() != null && Objects.equals(f.getBranch().getId(), branchId)).toList();
        if (branchFunds.size() != 1) throw invalid("Cần cấu hình một quỹ ghi nhận số dư cho chi nhánh");
        FundBalance fund = branchFunds.get(0);
        BankAccount account = null;
        if (bank) {
            String number = configured == null ? null : configured.getBankAccount();
            if (number == null || number.isBlank()) throw invalid("Phương thức thanh toán chưa cấu hình tài khoản ngân hàng nhận tiền");
            List<BankAccount> matches = accounts.findByIsDeletedFalse().stream()
                    .filter(a -> number.trim().equals(a.getAccountNumber()) && Boolean.TRUE.equals(a.getIsActive())
                            && "ACTIVE".equalsIgnoreCase(a.getStatus()))
                    .filter(a -> a.getBranch() == null || Objects.equals(a.getBranch().getId(), branchId)).toList();
            if (matches.size() != 1) throw invalid("Tài khoản nhận tiền không tồn tại, không hoạt động hoặc không thuộc chi nhánh");
            account = matches.get(0);
        }
        entityManager.refresh(fund, LockModeType.PESSIMISTIC_WRITE);
        String destination;
        if (cash) {
            fund.setCashBalance(zero(fund.getCashBalance()).add(delta));
            destination = "Quỹ tiền mặt - " + order.getBranch().getBranchName();
        } else {
            entityManager.refresh(account, LockModeType.PESSIMISTIC_WRITE);
            account.setCurrentBalance(zero(account.getCurrentBalance()).add(delta));
            fund.setBankBalance(zero(fund.getBankBalance()).add(delta));
            accounts.save(account);
            destination = account.getBankName() + " - " + account.getAccountNumber();
        }
        funds.save(fund);
        ReceiptVoucher receipt = ReceiptVoucher.builder()
                .voucherCode(sequence.generateReceiptCode()).voucherDate(LocalDateTime.now())
                .amount(delta).status("COMPLETED").creationSource("AUTO")
                .sourceDocumentType("SALE_ORDER").sourceDocumentId(order.getId())
                .sourceDocumentCode(order.getOrderCode()).invoiceCode(order.getOrderCode())
                .payerName(order.getCustomer() == null ? order.getCustomerName() : order.getCustomer().getName())
                .paymentMethod(cash ? "CASH" : "BANK_TRANSFER").fundAccountName(destination)
                .branch(order.getBranch()).category("Bán hàng").handler(order.getUpdatedBy() != null ? order.getUpdatedBy() : order.getCreatedBy())
                .notes("Thu tiền đơn online " + order.getOrderCode() + "; tổng đã thu: " + paid.toPlainString()).build();
        receipt.setIsDeleted(false);
        receipt.setCreatedBy(receipt.getHandler());
        receipts.save(receipt);
        order.setPaidAmount(paid);
    }

    private static BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private static ResponseStatusException invalid(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
}
