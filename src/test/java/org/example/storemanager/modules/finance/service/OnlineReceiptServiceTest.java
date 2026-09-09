package org.example.storemanager.modules.finance.service;

import jakarta.persistence.EntityManager;
import org.example.storemanager.modules.finance.entity.*;
import org.example.storemanager.modules.finance.repository.*;
import org.example.storemanager.modules.sales.entity.SaleOrder;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.shared.service.DocumentSequenceService;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OnlineReceiptServiceTest {
    final ReceiptVoucherRepository receipts = mock(ReceiptVoucherRepository.class);
    final PaymentMethodRepository methods = mock(PaymentMethodRepository.class);
    final BankAccountRepository accounts = mock(BankAccountRepository.class);
    final FundBalanceRepository funds = mock(FundBalanceRepository.class);
    final DocumentSequenceService sequence = mock(DocumentSequenceService.class);
    final EntityManager em = mock(EntityManager.class);
    final OnlineReceiptService service = new OnlineReceiptService(receipts, methods, accounts, funds, sequence, em);
    final List<ReceiptVoucher> posted = new ArrayList<>();
    final Branch branch = new Branch();
    final FundBalance fund = FundBalance.builder().cashBalance(new BigDecimal("100")).bankBalance(new BigDecimal("200")).build();
    final SaleOrder order = SaleOrder.builder().orderOrigin("ONLINE").orderCode("ONLINE-60")
            .status("PENDING").paymentStatus("PAID").paymentMethodCode("CASH")
            .finalAmount(new BigDecimal("1000")).build();

    OnlineReceiptServiceTest() {
        branch.setId(1L); branch.setBranchName("Chi nhánh 1");
        fund.setId(2L); fund.setBranch(branch);
        order.setId(60L); order.setBranch(branch);
        when(funds.findByIsDeletedFalse()).thenReturn(List.of(fund));
        when(receipts.findByIsDeletedFalse()).thenAnswer(i -> new ArrayList<>(posted));
        when(receipts.save(any())).thenAnswer(i -> { ReceiptVoucher r = i.getArgument(0); posted.add(r); return r; });
        when(sequence.generateReceiptCode()).thenReturn("PT-TEST");
    }

    @Test void confirmedCashBeforeDeliveryPostsReceiptAndCash() {
        service.collect(order);
        assertThat(posted).hasSize(1);
        assertThat(posted.get(0).getCreationSource()).isEqualTo("AUTO");
        assertThat(posted.get(0).getSourceDocumentId()).isEqualTo(60L);
        assertThat(fund.getCashBalance()).isEqualByComparingTo("1100");
        assertThat(fund.getBankBalance()).isEqualByComparingTo("200");
    }

    @Test void repeatConfirmationAndCompletionDoNotPostTwice() {
        service.collect(order); service.collect(order);
        order.setStatus("COMPLETED"); service.collect(order);
        assertThat(posted).hasSize(1);
        assertThat(fund.getCashBalance()).isEqualByComparingTo("1100");
    }

    @Test void partialThenFullPaymentPostsOnlyDifference() {
        order.setPaymentStatus("PARTIAL"); order.setPaidAmount(new BigDecimal("300"));
        service.collect(order);
        order.setPaymentStatus("PAID"); service.collect(order);
        assertThat(posted).hasSize(2);
        assertThat(posted.get(0).getAmount()).isEqualByComparingTo("300");
        assertThat(posted.get(1).getAmount()).isEqualByComparingTo("700");
        assertThat(fund.getCashBalance()).isEqualByComparingTo("1100");
    }

    @Test void completedButUnpaidDoesNotPost() {
        order.setStatus("COMPLETED"); order.setPaymentStatus("UNPAID");
        service.collect(order);
        assertThat(posted).isEmpty(); verify(funds, never()).save(any());
    }

    @Test void cancelledOrderDoesNotPost() {
        order.setStatus("CANCELLED"); service.collect(order);
        assertThat(posted).isEmpty();
    }

    @Test void configuredBankAccountAndBranchSummaryBothIncrease() {
        PaymentMethod method = PaymentMethod.builder().methodCode("VIETQR").type("BANK_TRANSFER").bankAccount("123456").build();
        BankAccount bank = BankAccount.builder().bankName("Test Bank").accountNumber("123456")
                .currentBalance(new BigDecimal("500")).branch(branch).status("ACTIVE").isActive(true).build();
        order.setPaymentMethodCode("VIETQR");
        when(methods.findByIsDeletedFalse()).thenReturn(List.of(method));
        when(accounts.findByIsDeletedFalse()).thenReturn(List.of(bank));
        service.collect(order);
        assertThat(bank.getCurrentBalance()).isEqualByComparingTo("1500");
        assertThat(fund.getBankBalance()).isEqualByComparingTo("1200");
        assertThat(fund.getCashBalance()).isEqualByComparingTo("100");
        assertThat(posted.get(0).getFundAccountName()).isEqualTo("Test Bank - 123456");
    }

    @Test void missingBankConfigurationDoesNotPostOrChangeBalances() {
        order.setPaymentMethodCode("BANK_TRANSFER");
        assertThatThrownBy(() -> service.collect(order)).hasMessageContaining("chưa cấu hình tài khoản");
        assertThat(posted).isEmpty();
        assertThat(fund.getBankBalance()).isEqualByComparingTo("200");
        verify(funds, never()).save(any());
    }

    @Test void manualReceiptLinkedToOrderIsNotCollectedAgain() {
        posted.add(ReceiptVoucher.builder().invoiceCode(order.getOrderCode()).status("COMPLETED")
                .amount(new BigDecimal("1000")).creationSource("MANUAL").build());
        service.collect(order);
        assertThat(posted).hasSize(1); verify(funds, never()).save(any());
    }

    @Test void partialPaymentAboveOrderTotalIsRejected() {
        order.setPaymentStatus("PARTIAL"); order.setPaidAmount(new BigDecimal("1001"));
        assertThatThrownBy(() -> service.collect(order)).hasMessageContaining("không hợp lệ");
        assertThat(posted).isEmpty();
    }
}
