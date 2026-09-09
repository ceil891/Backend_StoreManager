package org.example.storemanager.modules.sales.service;

import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.catalog.repository.ProductRepository;
import org.example.storemanager.modules.sales.dto.request.CreateCustomerReturnRequest;
import org.example.storemanager.modules.sales.dto.request.CustomerReturnDetailRequest;
import org.example.storemanager.modules.sales.entity.CustomerReturn;
import org.example.storemanager.modules.sales.entity.ExportInvoice;
import org.example.storemanager.modules.sales.entity.SaleOrder;
import org.example.storemanager.modules.sales.repository.CustomerReturnRepository;
import org.example.storemanager.modules.sales.repository.CustomerReturnDetailRepository;
import org.example.storemanager.modules.sales.repository.ExportInvoiceRepository;
import org.example.storemanager.modules.sales.repository.SaleOrderRepository;
import org.example.storemanager.modules.sales.service.impl.CustomerReturnServiceImpl;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.system.repository.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerReturnSourceTest {
    @Mock CustomerReturnRepository returns;
    @Mock CustomerReturnDetailRepository details;
    @Mock SaleOrderRepository orders;
    @Mock ExportInvoiceRepository invoices;
    @Mock BranchRepository branches;
    @Mock ProductRepository products;
    @InjectMocks CustomerReturnServiceImpl service;

    @BeforeEach
    void prepareRepositories() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("return-test", null, List.of()));
        Branch branch = new Branch();
        branch.setId(1L);
        Product product = new Product();
        product.setId(7L);
        when(branches.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(branch));
        when(products.findByIdAndIsDeletedFalse(7L)).thenReturn(Optional.of(product));
        when(returns.save(any(CustomerReturn.class))).thenAnswer(invocation -> {
            CustomerReturn saved = invocation.getArgument(0);
            saved.setId(22L);
            return saved;
        });
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private CreateCustomerReturnRequest request() {
        CustomerReturnDetailRequest line = new CustomerReturnDetailRequest();
        line.setProductId(7L);
        line.setQuantity(BigDecimal.ONE);
        line.setRefundPrice(new BigDecimal("25000"));
        CreateCustomerReturnRequest request = new CreateCustomerReturnRequest();
        request.setReturnCode("RET-TEST");
        request.setReturnRequestCode("RR-2026-7031");
        request.setReturnDate(LocalDateTime.of(2026, 9, 9, 0, 0));
        request.setStatus("PENDING_RECEIPT");
        request.setBranchId(1L);
        request.setDetails(List.of(line));
        return request;
    }

    @Test
    void createPendingReturnFromOrderWithoutInventingAnInvoice() {
        SaleOrder order = new SaleOrder();
        order.setId(60L);
        order.setOrderCode("ONLINE-395688");
        when(orders.findByIdAndIsDeletedFalse(60L)).thenReturn(Optional.of(order));
        CreateCustomerReturnRequest request = request();
        request.setOrderId(60L);

        var response = service.createReturn(request);

        ArgumentCaptor<CustomerReturn> saved = ArgumentCaptor.forClass(CustomerReturn.class);
        verify(returns).save(saved.capture());
        assertThat(saved.getValue().getInvoice()).isNull();
        assertThat(saved.getValue().getOrder()).isSameAs(order);
        assertThat(response.getOrderId()).isEqualTo(60L);
        assertThat(response.getInvoiceId()).isNull();
        assertThat(response.getTotalRefund()).isEqualByComparingTo("25000");
        assertThat(response.getStatus()).isEqualTo("PENDING_RECEIPT");
        verifyNoInteractions(invoices);
    }

    @Test
    void invoiceBasedReturnStillRetainsItsSourceInvoice() {
        ExportInvoice invoice = new ExportInvoice();
        invoice.setId(12L);
        when(invoices.findByIdAndIsDeletedFalse(12L)).thenReturn(Optional.of(invoice));
        CreateCustomerReturnRequest request = request();
        request.setInvoiceId(12L);

        var response = service.createReturn(request);

        assertThat(response.getInvoiceId()).isEqualTo(12L);
        assertThat(response.getOrderId()).isNull();
        assertThat(response.getTotalRefund()).isEqualByComparingTo("25000");
        verifyNoInteractions(orders);
    }
}
