package org.example.storemanager.modules.sales.service;

import org.example.storemanager.modules.sales.service.impl.CustomerReturnServiceImpl;
import org.example.storemanager.modules.sales.entity.CustomerReturn;
import org.example.storemanager.modules.sales.repository.*;
import org.example.storemanager.modules.finance.entity.FundBalance;
import org.example.storemanager.modules.finance.repository.*;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.shared.service.DocumentSequenceService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CustomerReturnRefundTest {
    @Mock CustomerReturnRepository returns;
    @Mock CustomerReturnDetailRepository details;
    @Mock PaymentVoucherRepository payments;
    @Mock FundBalanceRepository funds;
    @Mock DocumentSequenceService sequence;
    @Mock jakarta.persistence.EntityManager entityManager;
    @InjectMocks CustomerReturnServiceImpl service;
    CustomerReturn ret;
    @BeforeEach void setup() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("tester", null, List.of()));
        Branch branch = new Branch(); branch.setId(1L);
        ret = CustomerReturn.builder().returnCode("RET-1").status("REFUND_PROCESSING")
                .branch(branch).totalRefund(new BigDecimal("10000")).build();
        ret.setId(1L);
        when(returns.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(ret));
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }
    @Test void refundedCreatesAutomaticPaymentAndDebitsCashOnce() {
        when(returns.save(ret)).thenReturn(ret);
        when(details.findByCustomerReturnIdAndIsDeletedFalse(1L)).thenReturn(List.of());
        FundBalance fund = new FundBalance(); fund.setBranch(ret.getBranch()); fund.setCashBalance(new BigDecimal("30000"));
        when(funds.findByIsDeletedFalse()).thenReturn(List.of(fund));
        when(sequence.generatePaymentCode()).thenReturn("PAY-1");
        when(payments.existsByInvoiceCodeAndIsDeletedFalse("RET-1")).thenReturn(false, true);
        service.updateStatus(1L, "REFUNDED");
        service.updateStatus(1L, "REFUNDED");
        assertThat(fund.getCashBalance()).isEqualByComparingTo("20000");
        verify(payments).save(argThat(p -> "AUTO".equals(p.getCreationSource()) && "CUSTOMER_RETURN".equals(p.getSourceDocumentType())));
        verify(funds).save(fund);
    }
    @Test void refundedCannotMoveBackToApproved() {
        ret.setStatus("REFUNDED");
        assertThatThrownBy(() -> service.updateStatus(1L, "APPROVED")).hasMessageContaining("không được chuyển ngược");
        verifyNoInteractions(payments, funds);
    }
    @Test void insufficientCashDoesNotSavePayment() {
        when(returns.save(ret)).thenReturn(ret);
        when(details.findByCustomerReturnIdAndIsDeletedFalse(1L)).thenReturn(List.of());
        FundBalance fund = new FundBalance(); fund.setBranch(ret.getBranch()); fund.setCashBalance(BigDecimal.ZERO);
        when(funds.findByIsDeletedFalse()).thenReturn(List.of(fund));
        assertThatThrownBy(() -> service.updateStatus(1L, "REFUNDED")).hasMessageContaining("không đủ số dư");
        verify(payments, never()).save(any());
        verify(funds, never()).save(any());
    }
}
