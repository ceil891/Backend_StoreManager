package org.example.storemanager.modules.sales.purchase;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.example.storemanager.modules.purchase.dto.request.CreatePurchaseOrderRequest;
import org.example.storemanager.modules.purchase.dto.request.UpdatePurchaseOrderRequest;
import org.example.storemanager.modules.purchase.dto.request.PurchaseOrderDetailRequest;
import org.example.storemanager.modules.sales.dto.request.CreateSaleOrderRequest;
import org.example.storemanager.modules.sales.dto.request.UpdateSaleOrderRequest;
import org.example.storemanager.modules.sales.dto.request.SaleOrderDetailRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Contract tests for create/update request validation shared by sales and purchase APIs. */
class ValidationRequestTest {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @AfterAll
    static void tearDown() {
        Validation.buildDefaultValidatorFactory().close();
    }

    @Test
    void saleCreate_requiresIdentityAndAtLeastOneLine() {
        CreateSaleOrderRequest request = new CreateSaleOrderRequest();
        assertTrue(validator.validate(request).size() >= 5);

        request.setOrderCode("SO-001");
        request.setOrderDate(LocalDateTime.now());
        request.setBranchId(1L);
        request.setStatus("DRAFT");
        request.setDetails(List.of(validSaleLine()));
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void saleUpdate_rejectsBlankStatusAndEmptyDetails() {
        UpdateSaleOrderRequest request = new UpdateSaleOrderRequest();
        request.setOrderDate(LocalDateTime.now());
        request.setCustomerId(1L);
        request.setBranchId(1L);
        request.setStatus(" ");
        request.setDetails(List.of());
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void saleLine_rejectsZeroOrNegativeQuantityAndPrice() {
        SaleOrderDetailRequest line = new SaleOrderDetailRequest();
        line.setProductVariantId(1L);
        line.setQuantity(BigDecimal.ZERO);
        line.setUnitPriceSnapshot(BigDecimal.valueOf(-1));
        assertFalse(validator.validate(line).isEmpty());
    }

    @Test
    void purchaseCreate_requiresSupplierBranchAndLines() {
        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest();
        request.setPoCode("PO-001");
        request.setPoDate(LocalDateTime.now());
        request.setStatus("DRAFT");
        assertFalse(validator.validate(request).isEmpty());

        request.setSupplierId(1L);
        request.setBranchId(1L);
        request.setDetails(List.of(validPurchaseLine()));
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void purchaseUpdate_rejectsMissingSupplierBranchAndLines() {
        UpdatePurchaseOrderRequest request = new UpdatePurchaseOrderRequest();
        request.setPoDate(LocalDateTime.now());
        request.setStatus("DRAFT");
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void purchaseLine_rejectsZeroOrNegativeQuantityAndPrice() {
        PurchaseOrderDetailRequest line = new PurchaseOrderDetailRequest();
        line.setProductId(1L);
        line.setQuantity(BigDecimal.ZERO);
        line.setUnitPrice(BigDecimal.valueOf(-10));
        assertFalse(validator.validate(line).isEmpty());
    }

    @Test
    void createRequests_cascadeValidationIntoDetailLines() {
        SaleOrderDetailRequest saleLine = new SaleOrderDetailRequest();
        saleLine.setProductVariantId(1L);
        saleLine.setQuantity(BigDecimal.ZERO);
        saleLine.setUnitPriceSnapshot(BigDecimal.valueOf(100));
        CreateSaleOrderRequest sale = new CreateSaleOrderRequest();
        sale.setOrderCode("SO-002");
        sale.setOrderDate(LocalDateTime.now());
        sale.setBranchId(1L);
        sale.setStatus("DRAFT");
        sale.setDetails(List.of(saleLine));
        assertFalse(validator.validate(sale).isEmpty());

        PurchaseOrderDetailRequest purchaseLine = new PurchaseOrderDetailRequest();
        purchaseLine.setProductId(1L);
        purchaseLine.setQuantity(BigDecimal.ONE);
        purchaseLine.setUnitPrice(BigDecimal.ZERO);
        CreatePurchaseOrderRequest purchase = new CreatePurchaseOrderRequest();
        purchase.setPoCode("PO-002");
        purchase.setPoDate(LocalDateTime.now());
        purchase.setSupplierId(1L);
        purchase.setBranchId(1L);
        purchase.setStatus("DRAFT");
        purchase.setDetails(List.of(purchaseLine));
        assertFalse(validator.validate(purchase).isEmpty());
    }

    private static SaleOrderDetailRequest validSaleLine() {
        SaleOrderDetailRequest line = new SaleOrderDetailRequest();
        line.setProductVariantId(1L);
        line.setQuantity(BigDecimal.ONE);
        line.setUnitPriceSnapshot(BigDecimal.valueOf(10000));
        return line;
    }

    private static PurchaseOrderDetailRequest validPurchaseLine() {
        PurchaseOrderDetailRequest line = new PurchaseOrderDetailRequest();
        line.setProductId(1L);
        line.setQuantity(BigDecimal.ONE);
        line.setUnitPrice(BigDecimal.valueOf(10000));
        return line;
    }
}
