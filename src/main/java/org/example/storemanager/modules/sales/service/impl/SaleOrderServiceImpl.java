package org.example.storemanager.modules.sales.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.sales.dto.request.CreateSaleOrderRequest;
import org.example.storemanager.modules.sales.dto.request.SaleOrderDetailRequest;
import org.example.storemanager.modules.sales.dto.request.UpdateSaleOrderRequest;
import org.example.storemanager.modules.sales.dto.response.SaleOrderDetailResponse;
import org.example.storemanager.modules.sales.dto.response.SaleOrderResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.sales.entity.SaleOrder;
import org.example.storemanager.modules.sales.entity.SaleOrderDetail;
import org.example.storemanager.modules.partnerarea.entity.Customer;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.catalog.entity.ProductVariant;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.sales.repository.SaleOrderRepository;
import org.example.storemanager.modules.sales.repository.SaleOrderDetailRepository;
import org.example.storemanager.modules.partnerarea.repository.CustomerRepository;
import org.example.storemanager.modules.system.repository.BranchRepository;
import org.example.storemanager.modules.catalog.repository.ProductVariantRepository;
import org.example.storemanager.modules.catalog.repository.ProductRepository;
import org.example.storemanager.modules.sales.repository.ExportInvoiceRepository;
import org.example.storemanager.modules.sales.repository.ExportInvoiceDetailRepository;
import org.example.storemanager.modules.finance.repository.ReceiptVoucherRepository;
import org.example.storemanager.modules.sales.entity.ExportInvoice;
import org.example.storemanager.modules.sales.entity.ExportInvoiceDetail;
import org.example.storemanager.modules.finance.entity.ReceiptVoucher;
import org.example.storemanager.modules.finance.entity.DebtLedger;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.sales.service.SaleOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SaleOrderServiceImpl implements SaleOrderService {

    private final SaleOrderRepository saleOrderRepository;
    private final SaleOrderDetailRepository saleOrderDetailRepository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final ProductVariantRepository productVariantRepository;
    private final org.example.storemanager.shared.event.outbox.OutboxService outboxService;
    private final org.example.storemanager.modules.inventory.service.InventoryService inventoryService;
    private final org.example.storemanager.modules.wms.service.WarehouseService warehouseService;
    private final org.example.storemanager.modules.logistics.repository.DeliveryAssignmentHistoryRepository deliveryAssignmentHistoryRepository;
    private final org.example.storemanager.modules.crm.service.LoyaltyService loyaltyService;
    private final org.example.storemanager.modules.catalog.repository.ComboRepository comboRepository;
    private final org.example.storemanager.modules.catalog.repository.ComboDetailRepository comboDetailRepository;
    private final org.example.storemanager.modules.system.repository.PosSessionRepository posSessionRepository;
    private final ExportInvoiceRepository exportInvoiceRepository;
    private final ExportInvoiceDetailRepository exportInvoiceDetailRepository;
    private final ReceiptVoucherRepository receiptVoucherRepository;
    private final ProductRepository productRepository;
    private final org.example.storemanager.shared.service.DocumentSequenceService documentSequenceService;
    private final org.example.storemanager.modules.finance.repository.PaymentVoucherRepository paymentVoucherRepository;
    private final org.example.storemanager.modules.finance.repository.DebtLedgerRepository debtLedgerRepository;

    @Override
    public SaleOrderResponse createOrder(CreateSaleOrderRequest request) {
        Customer customer = null;
        if (request.getCustomerId() != null && request.getCustomerId() > 0) {
            customer = customerRepository.findByIdAndIsDeletedFalse(request.getCustomerId()).orElse(null);
        }
        if (customer == null && request.getCustomerPhone() != null && !request.getCustomerPhone().trim().isEmpty()) {
            String ph = request.getCustomerPhone().trim().replace(" ", "");
            customer = customerRepository.findAll().stream()
                    .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                    .filter(c -> c.getPhone() != null && c.getPhone().replace(" ", "").equals(ph))
                    .findFirst().orElse(null);
        }
        if (customer == null && request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty()) {
            String cName = request.getCustomerName().trim();
            customer = customerRepository.findAll().stream()
                    .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                    .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(cName))
                    .findFirst().orElse(null);
        }

        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseGet(() -> branchRepository.findAll().stream().filter(b -> Boolean.FALSE.equals(b.getIsDeleted())).findFirst().orElse(null));

        String username = getCurrentUsername();

        String origin = request.getOrderOrigin();
        if (origin == null || origin.trim().isEmpty() || "ONLINE_STORE".equalsIgnoreCase(origin)) {
            origin = "ONLINE";
        }

        Long posSessionId = request.getPosSessionId();
        if (posSessionId == null && ("POS".equalsIgnoreCase(origin) || (request.getOrderCode() != null && request.getOrderCode().startsWith("ORD-POS-")))) {
            if (posSessionRepository != null) {
                posSessionId = posSessionRepository.findByIsDeletedFalse().stream()
                        .filter(s -> "OPEN".equalsIgnoreCase(s.getStatus()))
                        .filter(s -> branch == null || s.getBranch() == null || s.getBranch().getId().equals(branch.getId()))
                        .sorted((a, b) -> b.getStartTime().compareTo(a.getStartTime()))
                        .map(org.example.storemanager.modules.system.entity.PosSession::getId)
                        .findFirst()
                        .orElseGet(() -> posSessionRepository.findByIsDeletedFalse().stream()
                                .filter(s -> "OPEN".equalsIgnoreCase(s.getStatus()))
                                .sorted((a, b) -> b.getStartTime().compareTo(a.getStartTime()))
                                .map(org.example.storemanager.modules.system.entity.PosSession::getId)
                                .findFirst()
                                .orElse(null));
            }
        }

        SaleOrder order = SaleOrder.builder()
                .orderCode(request.getOrderCode())
                .orderDate(request.getOrderDate() != null ? request.getOrderDate() : LocalDateTime.now())
                .expectedDelivery(request.getExpectedDelivery())
                .status(request.getStatus() != null ? request.getStatus() : "PENDING")
                .customer(customer)
                .branch(branch)
                .paymentMethodId(request.getPaymentMethodId())
                .paymentMethodCode(request.getPaymentMethodCode())
                .posSessionId(posSessionId)
                .build();

        order.setIsDeleted(false);
        order.setCreatedBy(username != null ? username : "ONLINE_STORE");
        order.setOrderOrigin(origin);
        order.setPaymentStatus(request.getPaymentStatus() != null && !request.getPaymentStatus().trim().isEmpty() ? request.getPaymentStatus() : "UNPAID");
        order.setNote(request.getNote());
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setShippingAddress(request.getShippingAddress());

        // Kiểm tra khách hàng bị khóa nợ
        if (customer != null && Boolean.TRUE.equals(customer.getIsCreditBlocked())) {
            String paymentSt = request.getPaymentStatus();
            String pmCode = request.getPaymentMethodCode();
            boolean isUnpaid = paymentSt == null || !"PAID".equalsIgnoreCase(paymentSt);
            boolean isCreditMethod = pmCode != null && (pmCode.toUpperCase().contains("DEBT") || pmCode.toUpperCase().contains("CONG_NO") || pmCode.toUpperCase().contains("CREDIT"));
            if (isUnpaid || isCreditMethod) {
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Khách hàng '" + customer.getName() + "' hiện đang bị khóa mua nợ (Credit Blocked). Vui lòng yêu cầu thanh toán ngay hoặc tất toán công nợ trước khi xuất đơn."
                );
            }
        }

        BigDecimal subTotalSum = BigDecimal.ZERO;
        BigDecimal taxSum = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<SaleOrderDetail> details = new ArrayList<>();

        if (request.getDetails() != null) {
            for (SaleOrderDetailRequest detailReq : request.getDetails()) {
                Long reqVariantId = detailReq.getProductVariantId();
                ProductVariant variant = null;
                String comboName = null;

                if (reqVariantId != null) {
                    // 1. Tìm trực tiếp theo ProductVariant ID trước
                    variant = productVariantRepository.findByIdAndIsDeletedFalse(reqVariantId).orElse(null);
                    // 2. Nếu không tìm thấy, fallback kiểm tra nếu reqVariantId là Product ID (POS fallback)
                    if (variant == null) {
                        List<ProductVariant> pvs = productVariantRepository.findByProductIdAndIsDeletedFalse(reqVariantId);
                        if (!pvs.isEmpty()) {
                            variant = pvs.get(0);
                        }
                    }
                    // 3. Fallback kiểm tra nếu reqVariantId là Combo ID (POS Combo checkout)
                    if (variant == null && comboRepository != null) {
                        org.example.storemanager.modules.catalog.entity.Combo combo = comboRepository.findByIdAndIsDeletedFalse(reqVariantId).orElse(null);
                        if (combo != null) {
                            comboName = combo.getComboName();
                            List<org.example.storemanager.modules.catalog.entity.ComboDetail> cDetails = comboDetailRepository.findByComboIdAndIsDeletedFalse(combo.getId());
                            if (!cDetails.isEmpty() && cDetails.get(0).getProduct() != null) {
                                List<ProductVariant> cpvs = productVariantRepository.findByProductIdAndIsDeletedFalse(cDetails.get(0).getProduct().getId());
                                if (!cpvs.isEmpty()) {
                                    variant = cpvs.get(0);
                                }
                            }
                        }
                    }
                }

                // 4. Fallback an toàn để đơn hàng không bị ném ngoại lệ 404 làm mất dữ liệu
                if (variant == null) {
                    variant = productVariantRepository.findAll().stream()
                            .filter(v -> !Boolean.TRUE.equals(v.getIsDeleted()))
                            .findFirst().orElse(null);
                }

                BigDecimal qty = detailReq.getQuantity() != null ? detailReq.getQuantity() : BigDecimal.ONE;
                BigDecimal price = detailReq.getUnitPriceSnapshot() != null ? detailReq.getUnitPriceSnapshot() : BigDecimal.ZERO;
                BigDecimal detailDiscount = detailReq.getDiscountAmount() != null ? detailReq.getDiscountAmount() : BigDecimal.ZERO;
                BigDecimal subTotal = qty.multiply(price).subtract(detailDiscount);
                if (subTotal.compareTo(BigDecimal.ZERO) < 0) subTotal = BigDecimal.ZERO;

                BigDecimal detailTaxRate = detailReq.getTaxRate() != null
                        ? detailReq.getTaxRate()
                        : getTaxRateForProduct(variant != null ? variant.getProduct() : null);

                BigDecimal detailTaxAmount = detailReq.getTaxAmount() != null
                        ? detailReq.getTaxAmount()
                        : subTotal.multiply(detailTaxRate).setScale(2, java.math.RoundingMode.HALF_UP);

                BigDecimal detailTotal = subTotal.add(detailTaxAmount);

                subTotalSum = subTotalSum.add(subTotal);
                taxSum = taxSum.add(detailTaxAmount);
                totalAmount = totalAmount.add(detailTotal);

                SaleOrderDetail detail = SaleOrderDetail.builder()
                        .order(order)
                        .productVariant(variant)
                        .productNameSnapshot(comboName != null ? comboName : (variant != null && variant.getProduct() != null ? variant.getProduct().getName() : "Sản phẩm Bán hàng"))
                        .skuSnapshot(variant != null && variant.getSku() != null ? variant.getSku() : "SKU-SALE")
                        .barcodeSnapshot(variant != null ? variant.getBarcode() : null)
                        .variantDescriptionSnapshot(variant != null ? variant.getVariantCode() : null)
                        .quantity(qty)
                        .unitPrice(price)
                        .unitPriceSnapshot(price)
                        .discountAmount(detailDiscount)
                        .subTotal(subTotal)
                        .taxRate(detailTaxRate)
                        .taxAmount(detailTaxAmount)
                        .totalAmount(detailTotal)
                        .build();

                detail.setIsDeleted(false);
                detail.setCreatedBy(username != null ? username : "ONLINE_STORE");
                details.add(detail);
            }
        }

        BigDecimal voucherDiscount = request.getVoucherDiscountAmount() != null ? request.getVoucherDiscountAmount() : BigDecimal.ZERO;
        BigDecimal pointsDiscount = BigDecimal.ZERO;
        if (request.getLoyaltyPointsUsed() != null && request.getLoyaltyPointsUsed() > 0) {
            pointsDiscount = BigDecimal.valueOf(request.getLoyaltyPointsUsed() * 100L);
        }
        BigDecimal finalAmount = totalAmount.subtract(voucherDiscount).subtract(pointsDiscount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        // Kiểm tra hạn mức nợ (Debt Limit) khi khách hàng mua nợ
        if (customer != null) {
            String paymentSt = request.getPaymentStatus();
            String pmCode = request.getPaymentMethodCode();
            boolean isUnpaid = paymentSt == null || !"PAID".equalsIgnoreCase(paymentSt);
            boolean isCreditMethod = pmCode != null && (pmCode.toUpperCase().contains("DEBT") || pmCode.toUpperCase().contains("CONG_NO") || pmCode.toUpperCase().contains("CREDIT"));

            if ((isUnpaid || isCreditMethod) && customer.getDebtLimit() != null && customer.getDebtLimit() > 0) {
                double maxDebt = customer.getDebtLimit();
                List<SaleOrder> unpaidOrders = saleOrderRepository
                        .findByCustomerIdAndPaymentStatusNotAndIsDeletedFalseOrderByOrderDateDesc(customer.getId(), "PAID")
                        .stream()
                        .filter(o -> !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                        .toList();
                BigDecimal currentDebt = unpaidOrders.stream()
                        .map(o -> o.getFinalAmount() != null ? o.getFinalAmount() : (o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal newOrderDebt = finalAmount.compareTo(BigDecimal.ZERO) > 0 ? finalAmount : totalAmount;
                BigDecimal projectedDebt = currentDebt.add(newOrderDebt);

                if (projectedDebt.compareTo(BigDecimal.valueOf(maxDebt)) > 0) {
                    throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        String.format("Đơn hàng vượt quá hạn mức nợ cho phép của khách hàng '%s' (Hạn mức tối đa: %,.0f đ, Đang nợ: %,.0f đ, Đơn phát sinh: %,.0f đ).",
                                customer.getName(), maxDebt, currentDebt.doubleValue(), newOrderDebt.doubleValue())
                    );
                }
            }
        }

        order.setSubTotal(subTotalSum);
        order.setTaxAmount(taxSum);
        order.setTotalAmount(totalAmount);
        order.setVoucherDiscountAmount(voucherDiscount);
        order.setLoyaltyPointsUsed(request.getLoyaltyPointsUsed());
        order.setVoucherCode(request.getVoucherCode());
        order.setFinalAmount(finalAmount);

        BigDecimal paidAmount = request.getPaidAmount();
        if ("PAID".equalsIgnoreCase(request.getPaymentStatus())) {
            paidAmount = finalAmount;
        } else if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            paidAmount = BigDecimal.ZERO;
        } else if (paidAmount.compareTo(finalAmount) > 0) {
            paidAmount = finalAmount;
        }
        order.setPaidAmount(paidAmount);

        SaleOrder savedOrder = saleOrderRepository.save(order);
        saleOrderDetailRepository.saveAll(details);

        // Tự động trừ tồn kho thực tế nếu đơn hàng đã hoàn tất (COMPLETED)
        if ("COMPLETED".equalsIgnoreCase(savedOrder.getStatus())) {
            org.example.storemanager.modules.wms.entity.WarehouseZone defaultZone = 
                    warehouseService.getOrCreateDefaultZone(branch);
            for (SaleOrderDetail detail : details) {
                ProductVariant pv = detail.getProductVariant();
                if (pv != null && pv.getProduct() != null) {
                    inventoryService.deductStock(
                            defaultZone.getId(),
                            branch.getId(),
                            pv.getProduct().getId(),
                            null,
                            null,
                            detail.getQuantity(),
                            "EXPORT",
                            savedOrder.getOrderCode(),
                            savedOrder.getId()
                    );
                }
            }
            // Tự động tạo hóa đơn xuất và phiếu thu khi đơn hàng tạo đã COMPLETED
            autoGenerateInvoiceAndReceiptIfCompleted(savedOrder, details);
        }

        // Tự động xử lý trừ điểm (REDEEM) & tích điểm (EARN) cho khách hàng
        if (customer != null) {
            // 1. Tiêu điểm trước
            if (request.getLoyaltyPointsUsed() != null && request.getLoyaltyPointsUsed() > 0) {
                try {
                    loyaltyService.processOrderRedeem(
                            customer.getId(),
                            savedOrder.getOrderCode(),
                            request.getLoyaltyPointsUsed(),
                            savedOrder
                    );
                } catch (Exception e) {
                    log.warn("Cảnh báo khi trừ điểm thưởng loyalty: {}", e.getMessage());
                }
            }

            // 2. Tích điểm trên số tiền thực trả
            try {
                BigDecimal earnBase = savedOrder.getFinalAmount() != null ? savedOrder.getFinalAmount() : savedOrder.getTotalAmount();
                loyaltyService.processOrderLoyaltyEarn(
                        customer.getId(),
                        savedOrder.getOrderCode(),
                        earnBase,
                        savedOrder
                );
            } catch (Exception e) {
                log.warn("Cảnh báo khi tích điểm tự động: {}", e.getMessage());
            }
        }

        // Transactional Outbox Pattern: Save Event to Outbox table in the SAME DB Transaction
        try {
            if (outboxService != null) {
                org.example.storemanager.shared.event.payload.OrderCreatedEventPayload payload = 
                        org.example.storemanager.shared.event.payload.OrderCreatedEventPayload.builder()
                        .orderId(savedOrder.getId().toString())
                        .customerId(customer != null ? customer.getId() : (savedOrder.getCustomer() != null ? savedOrder.getCustomer().getId() : null))
                        .branchId(branch != null ? branch.getId() : (savedOrder.getBranch() != null ? savedOrder.getBranch().getId() : null))
                        .totalAmount(savedOrder.getTotalAmount())
                        .createdAt(savedOrder.getCreatedAt() != null ? savedOrder.getCreatedAt() : LocalDateTime.now())
                        .build();

                org.example.storemanager.shared.event.base.DomainEvent<org.example.storemanager.shared.event.payload.OrderCreatedEventPayload> domainEvent = 
                        org.example.storemanager.shared.event.base.DomainEvent.create(
                                "ORDER_CREATED", "SALE_ORDER", 
                                savedOrder.getId().toString(), 
                                payload
                        );

                outboxService.saveEventToOutbox(domainEvent);
            }
        } catch (Exception e) {
            log.warn("Outbox save failed: {}", e.getMessage());
        }

        return mapToResponse(savedOrder, details);
    }

    @Override
    public SaleOrderResponse updateOrder(Long id, UpdateSaleOrderRequest request) {
        SaleOrder order = saleOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SaleOrder", "id", id));

        if ("COMPLETED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể chỉnh sửa đơn bán hàng ở trạng thái " + order.getStatus());
        }

        Customer customer = customerRepository.findByIdAndIsDeletedFalse(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        String username = getCurrentUsername();

        order.setOrderDate(request.getOrderDate());
        order.setExpectedDelivery(request.getExpectedDelivery());
        order.setStatus(request.getStatus());
        order.setCustomer(customer);
        order.setBranch(branch);
        order.setNote(request.getNote());
        order.setUpdatedBy(username);

        // Soft delete old details
        List<SaleOrderDetail> oldDetails = saleOrderDetailRepository.findByOrderIdAndIsDeletedFalse(id);
        for (SaleOrderDetail detail : oldDetails) {
            detail.setIsDeleted(true);
            detail.setDeletedBy(username);
            detail.setDeletedAt(LocalDateTime.now());
        }
        saleOrderDetailRepository.saveAll(oldDetails);

        // Add new details
        BigDecimal subTotalSum = BigDecimal.ZERO;
        BigDecimal taxSum = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<SaleOrderDetail> newDetails = new ArrayList<>();

        for (SaleOrderDetailRequest detailReq : request.getDetails()) {
            ProductVariant variant = productVariantRepository.findByIdAndIsDeletedFalse(detailReq.getProductVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", detailReq.getProductVariantId()));

            BigDecimal qty = detailReq.getQuantity() != null ? detailReq.getQuantity() : BigDecimal.ONE;
            BigDecimal price = detailReq.getUnitPriceSnapshot() != null ? detailReq.getUnitPriceSnapshot() : BigDecimal.ZERO;
            BigDecimal detailDiscount = detailReq.getDiscountAmount() != null ? detailReq.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal subTotal = qty.multiply(price).subtract(detailDiscount);
            if (subTotal.compareTo(BigDecimal.ZERO) < 0) subTotal = BigDecimal.ZERO;

            BigDecimal detailTaxRate = detailReq.getTaxRate() != null
                    ? detailReq.getTaxRate()
                    : getTaxRateForProduct(variant.getProduct());

            BigDecimal detailTaxAmount = detailReq.getTaxAmount() != null
                    ? detailReq.getTaxAmount()
                    : subTotal.multiply(detailTaxRate).setScale(2, java.math.RoundingMode.HALF_UP);

            BigDecimal detailTotal = subTotal.add(detailTaxAmount);

            subTotalSum = subTotalSum.add(subTotal);
            taxSum = taxSum.add(detailTaxAmount);
            totalAmount = totalAmount.add(detailTotal);

            SaleOrderDetail detail = SaleOrderDetail.builder()
                    .order(order)
                    .productVariant(variant)
                    .productNameSnapshot(variant.getProduct().getName())
                    .skuSnapshot(variant.getSku())
                    .barcodeSnapshot(variant.getBarcode())
                    .variantDescriptionSnapshot(variant.getVariantCode())
                    .quantity(qty)
                    .unitPrice(price)
                    .unitPriceSnapshot(price)
                    .discountAmount(detailDiscount)
                    .subTotal(subTotal)
                    .taxRate(detailTaxRate)
                    .taxAmount(detailTaxAmount)
                    .totalAmount(detailTotal)
                    .build();

            detail.setIsDeleted(false);
            detail.setCreatedBy(username);
            newDetails.add(detail);
        }

        order.setSubTotal(subTotalSum);
        order.setTaxAmount(taxSum);
        order.setTotalAmount(totalAmount);
        order.setFinalAmount(totalAmount); // Keep final_amount in sync

        if (request.getPaymentStatus() != null) {
            order.setPaymentStatus(request.getPaymentStatus());
        }
        if (request.getPaidAmount() != null) {
            order.setPaidAmount(request.getPaidAmount());
        } else if ("PAID".equalsIgnoreCase(order.getPaymentStatus())) {
            order.setPaidAmount(totalAmount);
        }

        SaleOrder savedOrder = saleOrderRepository.save(order);
        saleOrderDetailRepository.saveAll(newDetails);

        if ("COMPLETED".equalsIgnoreCase(savedOrder.getStatus())) {
            autoGenerateInvoiceAndReceiptIfCompleted(savedOrder, newDetails);
        }

        return mapToResponse(savedOrder, newDetails);
    }

    @Override
    public SaleOrderResponse updateStatus(Long id, String status) {
        return updateStatus(id, status, null, null, null, null, null);
    }

    @Override
    public SaleOrderResponse updateStatus(Long id, String status, String carrier, String trackingCode, String shipperName, String shipperPhone) {
        return updateStatus(id, status, null, carrier, trackingCode, shipperName, shipperPhone);
    }

    @Override
    public SaleOrderResponse updateStatus(Long id, String status, Long branchId, String carrier, String trackingCode, String shipperName, String shipperPhone) {
        return updateStatus(id, status, null, branchId, carrier, trackingCode, shipperName, shipperPhone);
    }

    @Override
    public SaleOrderResponse updateStatus(Long id, String status, String paymentStatus, Long branchId, String carrier, String trackingCode, String shipperName, String shipperPhone) {
        SaleOrder order = saleOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SaleOrder", "id", id));

        String oldStatus = order.getStatus();
        // Normalize status: map FE/logistics aliases to DB-accepted values
        String normalizedStatus = normalizeOrderStatus(status);
        order.setStatus(normalizedStatus);
        String effectiveStatus = normalizedStatus; // used for delivery logic below

        if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
            order.setPaymentStatus(paymentStatus.trim().toUpperCase());
        } else if ("COMPLETED".equalsIgnoreCase(normalizedStatus) && (order.getPaymentStatus() == null || "UNPAID".equalsIgnoreCase(order.getPaymentStatus()))) {
            order.setPaymentStatus("PAID");
        }

        // Rule: Khi khách chọn phương thức Chuyển khoản (CK) thì admin/quản lý phải duyệt đơn (PAID) trước khi duyệt kho đóng gói
        if ("CONFIRMED".equalsIgnoreCase(effectiveStatus) || "PROCESSING".equalsIgnoreCase(effectiveStatus)) {
            String pm = order.getPaymentMethodCode();
            boolean isBankTransfer = pm != null && (
                    pm.toUpperCase().contains("BANK") ||
                    pm.toUpperCase().contains("TRANSFER") ||
                    pm.toUpperCase().contains("CK") ||
                    pm.toUpperCase().contains("CHUYEN_KHOAN") ||
                    pm.toUpperCase().contains("VIETQR")
            );
            if (isBankTransfer && !"PAID".equalsIgnoreCase(order.getPaymentStatus())) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Đơn hàng thanh toán chuyển khoản cần được Quản lý duyệt thanh toán (PAID) trước khi duyệt kho đóng gói!"
                );
            }
        }

        if (branchId != null) {
            Branch branch = branchRepository.findByIdAndIsDeletedFalse(branchId).orElse(null);
            if (branch != null) {
                order.setBranch(branch);
            }
        }

        boolean isShippingUpdated = false;
        if (carrier != null && !carrier.trim().isEmpty()) {
            order.setCarrier(carrier);
            isShippingUpdated = true;
        }
        if (trackingCode != null && !trackingCode.trim().isEmpty()) {
            order.setTrackingCode(trackingCode);
            isShippingUpdated = true;
            String url = buildTrackingUrl(carrier != null ? carrier : order.getCarrier(), trackingCode);
            order.setTrackingUrl(url);
        }
        if (shipperName != null && !shipperName.trim().isEmpty()) {
            order.setShipperName(shipperName);
            isShippingUpdated = true;
        }
        if (shipperPhone != null && !shipperPhone.trim().isEmpty()) {
            order.setShipperPhone(shipperPhone);
            isShippingUpdated = true;
        }

        String delStatus = "UNASSIGNED";
        if ("CONFIRMED".equalsIgnoreCase(effectiveStatus) || "PROCESSING".equalsIgnoreCase(effectiveStatus)) {
            delStatus = "READY_FOR_PICKUP";
        } else if ("DELIVERING".equalsIgnoreCase(effectiveStatus) || "SHIPPED".equalsIgnoreCase(effectiveStatus)) {
            delStatus = "IN_TRANSIT";
        } else if ("COMPLETED".equalsIgnoreCase(effectiveStatus) || "DELIVERED".equalsIgnoreCase(effectiveStatus)) {
            delStatus = "DELIVERED";
            // Đơn giao thành công hoặc hoàn tất: tự động cập nhật đã thanh toán cho đơn COD / chưa thanh toán
            if (order.getPaymentStatus() == null || "UNPAID".equalsIgnoreCase(order.getPaymentStatus()) || "PENDING".equalsIgnoreCase(order.getPaymentStatus())) {
                String pm = order.getPaymentMethodCode();
                if (pm == null || pm.toLowerCase().contains("cod") || pm.toLowerCase().contains("tiền mặt") || "COMPLETED".equalsIgnoreCase(effectiveStatus)) {
                    order.setPaymentStatus("PAID");
                }
            }
        } else if ("CANCELLED".equalsIgnoreCase(effectiveStatus)) {
            delStatus = "CANCELLED";
        }
        order.setDeliveryStatus(delStatus);

        String currentUser = getCurrentUsername();
        if (currentUser == null || currentUser.trim().isEmpty()) currentUser = "System Admin";

        if (isShippingUpdated || "DELIVERING".equalsIgnoreCase(effectiveStatus) || "SHIPPED".equalsIgnoreCase(effectiveStatus) || "CONFIRMED".equalsIgnoreCase(effectiveStatus)) {
            order.setAssignedAt(LocalDateTime.now());
            order.setAssignedBy(currentUser);
        }
        order.setUpdatedBy(currentUser);

        SaleOrder savedOrder = saleOrderRepository.save(order);

        if (isShippingUpdated || !normalizedStatus.equalsIgnoreCase(oldStatus)) {
            try {
                org.example.storemanager.modules.logistics.entity.DeliveryAssignmentHistory history =
                    org.example.storemanager.modules.logistics.entity.DeliveryAssignmentHistory.builder()
                        .orderId(savedOrder.getId())
                        .orderCode(savedOrder.getOrderCode())
                        .carrierId(savedOrder.getCarrierId())
                        .carrierName(savedOrder.getCarrier())
                        .shipperId(savedOrder.getDriverId())
                        .shipperName(savedOrder.getShipperName())
                        .shipperPhone(savedOrder.getShipperPhone())
                        .trackingCode(savedOrder.getTrackingCode())
                        .trackingUrl(savedOrder.getTrackingUrl())
                        .deliveryStatus(savedOrder.getDeliveryStatus())
                        .actionType(isShippingUpdated ? "ASSIGNED" : "STATUS_CHANGE")
                        .assignedAt(LocalDateTime.now())
                        .assignedBy(currentUser)
                        .note("Trạng thái: " + oldStatus + " -> " + status)
                        .build();
                history.setIsDeleted(false);
                deliveryAssignmentHistoryRepository.save(history);
            } catch (Exception ex) {
                System.err.println("DeliveryAssignmentHistory save failed: " + ex.getMessage());
            }
        }

        List<SaleOrderDetail> details = saleOrderDetailRepository.findByOrderIdAndIsDeletedFalse(id);
        
        // Tự động trừ tồn kho thực tế nếu chuyển sang COMPLETED và trước đó không phải COMPLETED
        if ("COMPLETED".equalsIgnoreCase(savedOrder.getStatus()) && !"COMPLETED".equalsIgnoreCase(oldStatus)) {
            try {
                org.example.storemanager.modules.wms.entity.WarehouseZone defaultZone = 
                        warehouseService.getOrCreateDefaultZone(savedOrder.getBranch());
                for (SaleOrderDetail detail : details) {
                    ProductVariant pv = detail.getProductVariant();
                    if (pv == null && detail.getSkuSnapshot() != null) {
                        pv = productVariantRepository.findBySkuAndIsDeletedFalse(detail.getSkuSnapshot()).orElse(null);
                    }
                    if (pv != null && pv.getProduct() != null) {
                        inventoryService.deductStock(
                                defaultZone.getId(),
                                savedOrder.getBranch().getId(),
                                pv.getProduct().getId(),
                                null,
                                null,
                                detail.getQuantity(),
                                "EXPORT",
                                savedOrder.getOrderCode(),
                                savedOrder.getId()
                        );
                    }
                }
            } catch (Exception e) {
                System.err.println("Cảnh báo khi trừ tồn kho đơn hàng khi chuyển trạng thái: " + e.getMessage());
            }
        }

        // Tự động hoàn tồn kho nếu trước đó đã COMPLETED mà bị CANCELLED hoặc RETURNED
        if (("CANCELLED".equalsIgnoreCase(savedOrder.getStatus()) || "RETURNED".equalsIgnoreCase(savedOrder.getStatus()))
                && "COMPLETED".equalsIgnoreCase(oldStatus)) {
            try {
                org.example.storemanager.modules.wms.entity.WarehouseZone defaultZone = 
                        warehouseService.getOrCreateDefaultZone(savedOrder.getBranch());
                for (SaleOrderDetail detail : details) {
                    ProductVariant pv = detail.getProductVariant();
                    if (pv != null && pv.getProduct() != null) {
                        inventoryService.addStock(
                                defaultZone.getId(),
                                savedOrder.getBranch().getId(),
                                pv.getProduct().getId(),
                                null,
                                null,
                                detail.getQuantity(),
                                "RESTOCK",
                                savedOrder.getOrderCode(),
                                savedOrder.getId()
                        );
                    }
                }
            } catch (Exception e) {
                System.err.println("Cảnh báo khi hoàn tồn kho đơn hàng bị hủy/trả: " + e.getMessage());
            }

            // Xử lý hủy đơn / hoàn tiền khi đơn COMPLETED bị hủy
            handleOrderCancellation(savedOrder, currentUser);
        }

        // Tự động tích điểm cho khách khi chuyển đơn sang COMPLETED
        if (savedOrder.getCustomer() != null && "COMPLETED".equalsIgnoreCase(savedOrder.getStatus()) && !"COMPLETED".equalsIgnoreCase(oldStatus)) {
            try {
                loyaltyService.processOrderLoyaltyEarn(
                        savedOrder.getCustomer().getId(),
                        savedOrder.getOrderCode(),
                        savedOrder.getFinalAmount() != null ? savedOrder.getFinalAmount() : savedOrder.getTotalAmount(),
                        savedOrder
                );
            } catch (Exception e) {
                System.err.println("Cảnh báo khi tích điểm tự động khi chuyển trạng thái: " + e.getMessage());
            }
        }

        // Tự động tạo hóa đơn xuất và phiếu thu khi đơn hàng chuyển sang COMPLETED
        if ("COMPLETED".equalsIgnoreCase(savedOrder.getStatus()) && !"COMPLETED".equalsIgnoreCase(oldStatus)) {
            autoGenerateInvoiceAndReceiptIfCompleted(savedOrder, details);
        }

        return mapToResponse(savedOrder, details);
    }

    private String buildTrackingUrl(String carrier, String code) {
        if (code == null || code.trim().isEmpty()) return null;
        String c = (carrier != null ? carrier.toLowerCase() : "");
        if (c.contains("viettel")) {
            return "https://viettelpost.com.vn/tra-cuu-hanh-trinh?code=" + code.trim();
        } else if (c.contains("ghtk") || c.contains("tiết kiệm")) {
            return "https://i.giaohangtietkiem.vn/" + code.trim();
        } else if (c.contains("ghn") || c.contains("nhanh")) {
            return "https://donhang.ghn.vn/?order_code=" + code.trim();
        } else if (c.contains("shopee") || c.contains("spx")) {
            return "https://spx.vn/track?" + code.trim();
        }
        return "https://viettelpost.com.vn/tra-cuu-hanh-trinh?code=" + code.trim();
    }

    /**
     * Normalize incoming status from FE/Logistics to DB-constraint-compliant values.
     * DB constraint: PENDING, CONFIRMED, DELIVERING, COMPLETED, CANCELLED
     */
    private String normalizeOrderStatus(String status) {
        if (status == null) return "PENDING";
        switch (status.toUpperCase().trim()) {
            case "SHIPPED":
            case "IN_TRANSIT":
                return "DELIVERING";
            case "DELIVERED":
                return "COMPLETED";
            case "PROCESSING":
                return "CONFIRMED";
            default:
                return status.toUpperCase().trim();
        }
    }

    @Override
    public void deleteOrder(Long id) {
        SaleOrder order = saleOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SaleOrder", "id", id));

        if ("COMPLETED".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể xóa đơn bán hàng ở trạng thái COMPLETED");
        }

        String username = getCurrentUsername();
        order.setIsDeleted(true);
        order.setDeletedBy(username);
        order.setDeletedAt(LocalDateTime.now());
        saleOrderRepository.save(order);

        List<SaleOrderDetail> details = saleOrderDetailRepository.findByOrderIdAndIsDeletedFalse(id);
        for (SaleOrderDetail detail : details) {
            detail.setIsDeleted(true);
            detail.setDeletedBy(username);
            detail.setDeletedAt(LocalDateTime.now());
        }
        saleOrderDetailRepository.saveAll(details);
    }

    @Override
    @Transactional
    public SaleOrderResponse cancelOnlineOrder(Long id, String reason) {
        SaleOrder order = saleOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SaleOrder", "id", id));

        String currentStatus = order.getStatus() != null ? order.getStatus().toUpperCase() : "PENDING";
        String currentDelStatus = order.getDeliveryStatus() != null ? order.getDeliveryStatus().toUpperCase() : "UNASSIGNED";

        // Nếu đơn đã giao cho ĐVVC hoặc đã hoàn tất thì không cho phép hủy
        if ("SHIPPED".equals(currentStatus) || "DELIVERING".equals(currentStatus) || "IN_TRANSIT".equals(currentDelStatus) ||
                "DELIVERED".equals(currentStatus) || "COMPLETED".equals(currentStatus)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Đơn hàng đã được giao cho đơn vị vận chuyển hoặc đã hoàn tất, không thể hủy!"
            );
        }

        order.setStatus("CANCELLED");
        order.setDeliveryStatus("CANCELLED");
        String cancelNote = (reason != null && !reason.trim().isEmpty()) ? reason.trim() : "Khách hàng hủy trên Web Online";
        order.setNote((order.getNote() != null ? order.getNote() + " | " : "") + "[Khách hủy đơn: " + cancelNote + "]");
        order.setUpdatedBy("Khách hàng Online");
        order.setUpdatedAt(LocalDateTime.now());

        SaleOrder savedOrder = saleOrderRepository.save(order);
        List<SaleOrderDetail> details = saleOrderDetailRepository.findByOrderIdAndIsDeletedFalse(savedOrder.getId());
        return mapToResponse(savedOrder, details);
    }

    @Override
    @Transactional(readOnly = true)
    public SaleOrderResponse getOrderById(Long id) {
        SaleOrder order = saleOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SaleOrder", "id", id));

        List<SaleOrderDetail> details = saleOrderDetailRepository.findByOrderIdAndIsDeletedFalse(id);
        return mapToResponse(order, details);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleOrderResponse> getAllOrders(String search, String status, Long branchId, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sorting);
        Page<SaleOrder> pageResult = saleOrderRepository.findAllOrders(search, status, branchId, includeDeleted, pageable);
        
        List<SaleOrder> orders = pageResult.getContent();
        List<Long> orderIds = orders.stream().map(SaleOrder::getId).collect(Collectors.toList());
        List<SaleOrderDetail> allDetails = orderIds.isEmpty() ? java.util.Collections.emptyList() :
                saleOrderDetailRepository.findByOrderIdInAndIsDeletedFalse(orderIds);
        java.util.Map<Long, List<SaleOrderDetail>> detailsMap = allDetails.stream()
                .filter(d -> d.getOrder() != null)
                .collect(Collectors.groupingBy(d -> d.getOrder().getId()));

        return orders.stream()
                .map(o -> mapToResponse(o, detailsMap.getOrDefault(o.getId(), java.util.Collections.emptyList())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SaleOrderResponse> getOrdersPaginated(String search, String status, Long branchId, int page, int size, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<SaleOrder> pageResult = saleOrderRepository.findAllOrders(search, status, branchId, includeDeleted, pageable);

        List<SaleOrder> orders = pageResult.getContent();
        List<Long> orderIds = orders.stream().map(SaleOrder::getId).collect(Collectors.toList());
        List<SaleOrderDetail> allDetails = orderIds.isEmpty() ? java.util.Collections.emptyList() :
                saleOrderDetailRepository.findByOrderIdInAndIsDeletedFalse(orderIds);
        java.util.Map<Long, List<SaleOrderDetail>> detailsMap = allDetails.stream()
                .filter(d -> d.getOrder() != null)
                .collect(Collectors.groupingBy(d -> d.getOrder().getId()));

        List<SaleOrderResponse> content = orders.stream()
                .map(o -> mapToResponse(o, detailsMap.getOrDefault(o.getId(), java.util.Collections.emptyList())))
                .collect(Collectors.toList());

        return PageResponse.<SaleOrderResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                return auth.getName();
            }
        } catch (Exception ignored) { }
        return null; // Allow anonymous (online store orders)
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "updatedAt", "id");
        }
        String[] parts = sortParam.split(",");
        String property = parts[0];
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    private SaleOrderResponse mapToResponse(SaleOrder o, List<SaleOrderDetail> details) {
        List<SaleOrderDetailResponse> detailsResponse = details.stream()
                .map(d -> {
                    String imgUrl = null;
                    Long prodId = null;
                    if (d.getProductVariant() != null) {
                        imgUrl = d.getProductVariant().getImageUrl();
                        if (d.getProductVariant().getProduct() != null) {
                            prodId = d.getProductVariant().getProduct().getId();
                            if (imgUrl == null || imgUrl.isBlank()) {
                                imgUrl = d.getProductVariant().getProduct().getMainImageUrl();
                            }
                        }
                    }
                    return SaleOrderDetailResponse.builder()
                            .id(d.getId())
                            .productVariantId(d.getProductVariant() != null ? d.getProductVariant().getId() : null)
                            .productId(prodId)
                            .variantCode(d.getProductVariant() != null ? d.getProductVariant().getVariantCode() : null)
                            .skuSnapshot(d.getSkuSnapshot())
                            .barcodeSnapshot(d.getBarcodeSnapshot())
                            .productNameSnapshot(d.getProductNameSnapshot())
                            .variantDescriptionSnapshot(d.getVariantDescriptionSnapshot())
                            .imageUrl(imgUrl)
                            .quantity(d.getQuantity())
                            .unitPriceSnapshot(d.getUnitPriceSnapshot())
                            .discountAmount(d.getDiscountAmount())
                            .subTotal(d.getSubTotal())
                            .taxRate(d.getTaxRate())
                            .taxAmount(d.getTaxAmount())
                            .totalAmount(d.getTotalAmount())
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal finalAmt = o.getFinalAmount() != null ? o.getFinalAmount() : (o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO);
        BigDecimal paidAmt = o.getPaidAmount() != null ? o.getPaidAmount() : BigDecimal.ZERO;
        if ("PAID".equalsIgnoreCase(o.getPaymentStatus())) {
            paidAmt = finalAmt;
        }
        BigDecimal remDebt = finalAmt.subtract(paidAmt).max(BigDecimal.ZERO);

        return SaleOrderResponse.builder()
                .id(o.getId())
                .orderCode(o.getOrderCode())
                .orderDate(o.getOrderDate())
                .expectedDelivery(o.getExpectedDelivery())
                .subTotal(o.getSubTotal() != null ? o.getSubTotal() : o.getTotalAmount())
                .taxAmount(o.getTaxAmount() != null ? o.getTaxAmount() : BigDecimal.ZERO)
                .totalAmount(o.getTotalAmount())
                .finalAmount(finalAmt)
                .paidAmount(paidAmt)
                .remainingDebt(remDebt)
                .status(o.getStatus())
                .customerId(o.getCustomer() != null ? o.getCustomer().getId() : null)
                .customerName(o.getCustomerName() != null ? o.getCustomerName() : (o.getCustomer() != null ? o.getCustomer().getName() : null))
                .customerPhone(o.getCustomerPhone())
                .shippingAddress(o.getShippingAddress())
                .orderOrigin(o.getOrderOrigin())
                .paymentStatus(o.getPaymentStatus())
                .branchId(o.getBranch() != null ? o.getBranch().getId() : null)
                .branchName(o.getBranch() != null ? o.getBranch().getBranchName() : null)
                .note(o.getNote())
                .createdAt(o.getCreatedAt())
                .createdBy(o.getCreatedBy())
                .carrierId(o.getCarrierId())
                .carrier(o.getCarrier())
                .driverId(o.getDriverId())
                .trackingCode(o.getTrackingCode())
                .trackingUrl(o.getTrackingUrl())
                .shipperName(o.getShipperName())
                .shipperPhone(o.getShipperPhone())
                .deliveryStatus(o.getDeliveryStatus() != null ? o.getDeliveryStatus() : "UNASSIGNED")
                .assignedAt(o.getAssignedAt())
                .assignedBy(o.getAssignedBy())
                .paymentMethodId(o.getPaymentMethodId())
                .paymentMethodCode(o.getPaymentMethodCode())
                .paymentMethod(o.getPaymentMethodCode())
                .details(detailsResponse)
                .build();
    }

    /**
     * Tự động tạo Hóa đơn xuất bán (ExportInvoice + ExportInvoiceDetail) và Phiếu thu (ReceiptVoucher)
     * khi đơn hàng bán đạt trạng thái Hoàn thành (COMPLETED).
     */
    private void autoGenerateInvoiceAndReceiptIfCompleted(SaleOrder savedOrder, List<SaleOrderDetail> details) {
        if (savedOrder == null || !"COMPLETED".equalsIgnoreCase(savedOrder.getStatus())) {
            return;
        }

        try {
            // 1. Tự động sinh mã hóa đơn dựa trên mã đơn hàng
            String orderCode = savedOrder.getOrderCode();
            String invoiceCode = (orderCode != null && orderCode.startsWith("ORD-"))
                    ? orderCode.replaceFirst("ORD-", "INV-")
                    : documentSequenceService.generateExportInvoiceCode();

            ExportInvoice savedInvoice = null;
            if (!exportInvoiceRepository.existsByInvoiceCodeAndIsDeletedFalse(invoiceCode)) {
                String creator = savedOrder.getCreatedBy();
                if (creator == null || creator.trim().isEmpty()) {
                    try {
                        creator = getCurrentUsername();
                    } catch (Exception ignored) {
                        creator = "System";
                    }
                }

                org.example.storemanager.modules.system.entity.PosSession posSession = null;
                if (savedOrder.getPosSessionId() != null) {
                    posSession = posSessionRepository.findByIdAndIsDeletedFalse(savedOrder.getPosSessionId()).orElse(null);
                }

                BigDecimal subTotal = savedOrder.getSubTotal() != null ? savedOrder.getSubTotal() : BigDecimal.ZERO;
                BigDecimal discount = savedOrder.getVoucherDiscountAmount() != null ? savedOrder.getVoucherDiscountAmount() : BigDecimal.ZERO;
                BigDecimal tax = savedOrder.getTaxAmount() != null ? savedOrder.getTaxAmount() : BigDecimal.ZERO;
                BigDecimal totalAmount = savedOrder.getFinalAmount() != null ? savedOrder.getFinalAmount()
                        : (savedOrder.getTotalAmount() != null ? savedOrder.getTotalAmount() : BigDecimal.ZERO);

                String invStatus = "COMPLETED";
                if ("UNPAID".equalsIgnoreCase(savedOrder.getPaymentStatus())) {
                    invStatus = "UNPAID";
                } else if ("PARTIAL".equalsIgnoreCase(savedOrder.getPaymentStatus())) {
                    invStatus = "PARTIAL_PAID";
                }

                ExportInvoice invoice = ExportInvoice.builder()
                        .invoiceCode(invoiceCode)
                        .invoiceDate(savedOrder.getOrderDate() != null ? savedOrder.getOrderDate() : LocalDateTime.now())
                        .subTotal(subTotal)
                        .discount(discount)
                        .tax(tax)
                        .totalAmount(totalAmount)
                        .status(invStatus)
                        .customer(savedOrder.getCustomer())
                        .branch(savedOrder.getBranch())
                        .posSession(posSession)
                        .note("Hóa đơn xuất tự động cho đơn hàng " + savedOrder.getOrderCode())
                        .build();
                invoice.setIsDeleted(false);
                invoice.setCreatedBy(creator);

                savedInvoice = exportInvoiceRepository.save(invoice);

                // Tạo danh sách ExportInvoiceDetail
                List<ExportInvoiceDetail> invDetails = new ArrayList<>();
                if (details != null) {
                    for (SaleOrderDetail d : details) {
                        if (Boolean.TRUE.equals(d.getIsDeleted())) continue;

                        Product product = null;
                        if (d.getProductVariant() != null) {
                            product = d.getProductVariant().getProduct();
                            if (product == null && d.getProductVariant().getId() != null) {
                                ProductVariant pv = productVariantRepository.findByIdAndIsDeletedFalse(d.getProductVariant().getId()).orElse(null);
                                if (pv != null) product = pv.getProduct();
                            }
                        }
                        if (product == null && d.getSkuSnapshot() != null) {
                            ProductVariant pv = productVariantRepository.findBySkuAndIsDeletedFalse(d.getSkuSnapshot()).orElse(null);
                            if (pv != null) product = pv.getProduct();
                        }
                        if (product == null) {
                            product = productRepository.findAll().stream()
                                    .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                                    .findFirst().orElse(null);
                        }

                        if (product != null) {
                            BigDecimal qty = d.getQuantity() != null ? d.getQuantity() : BigDecimal.ONE;
                            BigDecimal uPrice = d.getUnitPrice() != null ? d.getUnitPrice()
                                    : (d.getUnitPriceSnapshot() != null ? d.getUnitPriceSnapshot() : BigDecimal.ZERO);
                            BigDecimal dDiscount = d.getDiscountAmount() != null ? d.getDiscountAmount() : BigDecimal.ZERO;
                            BigDecimal dSubTotal = d.getSubTotal() != null ? d.getSubTotal() : qty.multiply(uPrice).subtract(dDiscount);
                            BigDecimal dTaxRate = d.getTaxRate() != null ? d.getTaxRate() : BigDecimal.ZERO;
                            BigDecimal dTaxAmount = d.getTaxAmount() != null ? d.getTaxAmount() : BigDecimal.ZERO;
                            BigDecimal dTotal = d.getTotalAmount() != null ? d.getTotalAmount() : dSubTotal.add(dTaxAmount);

                            ExportInvoiceDetail invDetail = ExportInvoiceDetail.builder()
                                    .invoice(savedInvoice)
                                    .product(product)
                                    .quantity(qty)
                                    .unitPrice(uPrice)
                                    .discount(dDiscount)
                                    .subTotal(dSubTotal)
                                    .taxRate(dTaxRate)
                                    .taxAmount(dTaxAmount)
                                    .totalAmount(dTotal)
                                    .build();
                            invDetail.setIsDeleted(false);
                            invDetail.setCreatedBy(creator);
                            invDetails.add(invDetail);
                        }
                    }
                }
                if (!invDetails.isEmpty()) {
                    exportInvoiceDetailRepository.saveAll(invDetails);
                }
                log.info("Tự động tạo hóa đơn xuất bán {} thành công cho đơn hàng {}", invoiceCode, savedOrder.getOrderCode());
            }

            // 2. Tự động sinh Phiếu thu (ReceiptVoucher) và/hoặc Sổ nợ (DebtLedger) dựa trên thanh toán thực tế
            BigDecimal finalAmount = savedOrder.getFinalAmount() != null ? savedOrder.getFinalAmount()
                    : (savedOrder.getTotalAmount() != null ? savedOrder.getTotalAmount() : BigDecimal.ZERO);

            String paymentStatus = savedOrder.getPaymentStatus() != null ? savedOrder.getPaymentStatus().toUpperCase() : "UNPAID";
            BigDecimal paidAmount = savedOrder.getPaidAmount() != null ? savedOrder.getPaidAmount() : BigDecimal.ZERO;

            if ("PAID".equalsIgnoreCase(paymentStatus)) {
                paidAmount = finalAmount;
            } else if ("UNPAID".equalsIgnoreCase(paymentStatus)) {
                paidAmount = BigDecimal.ZERO;
            } else if ("PARTIAL".equalsIgnoreCase(paymentStatus)) {
                if (paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    paidAmount = BigDecimal.ZERO;
                } else if (paidAmount.compareTo(finalAmount) > 0) {
                    paidAmount = finalAmount;
                }
            }

            BigDecimal remainingDebt = finalAmount.subtract(paidAmount);
            if (remainingDebt.compareTo(BigDecimal.ZERO) < 0) {
                remainingDebt = BigDecimal.ZERO;
            }

            String payer = savedOrder.getCustomer() != null ? savedOrder.getCustomer().getName() :
                    (savedOrder.getCustomerName() != null ? savedOrder.getCustomerName() : "Khách mua hàng");
            String creator = savedOrder.getCreatedBy() != null ? savedOrder.getCreatedBy() : "System";
            LocalDateTime vDate = savedOrder.getOrderDate() != null && !savedOrder.getOrderDate().isAfter(LocalDateTime.now())
                    ? savedOrder.getOrderDate()
                    : LocalDateTime.now();

            // 2.1. Nếu có thực thu (paidAmount > 0), sinh Phiếu thu (ReceiptVoucher) với mã từ Sequence
            boolean receiptExists = receiptVoucherRepository.existsByInvoiceCodeAndIsDeletedFalse(invoiceCode)
                    || (orderCode != null && receiptVoucherRepository.existsByInvoiceCodeAndIsDeletedFalse(orderCode));

            if (!receiptExists && paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                String vCode = documentSequenceService.generateReceiptCode();
                String pm = savedOrder.getPaymentMethodCode() != null ? savedOrder.getPaymentMethodCode().toUpperCase() : "TIEN_MAT";
                String fundAccount = (pm.contains("BANK") || pm.contains("TRANSFER") || pm.contains("CK") || pm.contains("VIETQR"))
                        ? "Techcombank - 1902838392 (Công ty StoreManager)"
                        : "Quỹ tiền mặt (Cash)";

                ReceiptVoucher rv = ReceiptVoucher.builder()
                        .voucherCode(vCode)
                        .voucherDate(vDate)
                        .amount(paidAmount)
                        .payerName(payer)
                        .status("COMPLETED")
                        .paymentMethod(pm)
                        .fundAccountName(fundAccount)
                        .invoiceCode(invoiceCode)
                        .handler(creator)
                        .notes("Thu tiền tự động khi đơn hàng " + savedOrder.getOrderCode() + " hoàn tất thành công (" + paymentStatus + ")")
                        .category("Bán hàng")
                        .branch(savedOrder.getBranch())
                        .build();
                rv.setIsDeleted(false);
                rv.setCreatedBy(creator);
                receiptVoucherRepository.save(rv);
                log.info("Tự động tạo phiếu thu {} thành công cho đơn hàng {} với số tiền {}", vCode, savedOrder.getOrderCode(), paidAmount);
            }

            // 2.2. Nếu còn dư nợ (remainingDebt > 0) và có thông tin khách hàng, ghi nhận vào Sổ nợ (DebtLedger)
            if (remainingDebt.compareTo(BigDecimal.ZERO) > 0 && savedOrder.getCustomer() != null) {
                List<DebtLedger> existingDebts = debtLedgerRepository.findByRefCodeAndIsDeletedFalse(invoiceCode);
                if (existingDebts.isEmpty()) {
                    DebtLedger debt = DebtLedger.builder()
                            .transactionDate(vDate)
                            .refCode(invoiceCode)
                            .increase(remainingDebt)
                            .decrease(BigDecimal.ZERO)
                            .balance(remainingDebt)
                            .partnerId(savedOrder.getCustomer().getId())
                            .entityName(payer)
                            .entityType("CUSTOMER")
                            .status("NORMAL")
                            .notes("Ghi nhận công nợ đơn hàng " + savedOrder.getOrderCode() + " (Còn nợ: " + remainingDebt + ")")
                            .build();
                    debt.setIsDeleted(false);
                    debt.setCreatedBy(creator);
                    debtLedgerRepository.save(debt);
                    log.info("Tự động ghi nhận công nợ {} cho khách hàng {} của đơn hàng {}", remainingDebt, payer, savedOrder.getOrderCode());
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi tự động tạo hóa đơn xuất / phiếu thu cho đơn hàng {}: {}", savedOrder.getOrderCode(), e.getMessage(), e);
            if (e instanceof org.springframework.web.server.ResponseStatusException) {
                throw (org.springframework.web.server.ResponseStatusException) e;
            }
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi tự động tạo hóa đơn xuất / phiếu thu: " + e.getMessage(), e
            );
        }
    }

    /**
     * Xử lý hủy đơn hàng hoàn tất (COMPLETED -> CANCELLED):
     * - Hủy hóa đơn xuất (ExportInvoice.status = CANCELLED)
     * - Hoàn tiền đã thu (sinh PaymentVoucher hoàn tiền tự động)
     * - Tất toán công nợ trên Sổ nợ (DebtLedger.status = SETTLED, balance = 0)
     */
    private void handleOrderCancellation(SaleOrder savedOrder, String currentUser) {
        if (savedOrder == null) return;
        try {
            String orderCode = savedOrder.getOrderCode();
            String invoiceCode = (orderCode != null && orderCode.startsWith("ORD-"))
                    ? orderCode.replaceFirst("ORD-", "INV-")
                    : ("INV-" + (orderCode != null ? orderCode : ""));

            // 1. Chuyển trạng thái hóa đơn xuất sang CANCELLED
            exportInvoiceRepository.findByInvoiceCodeAndIsDeletedFalse(invoiceCode).ifPresent(inv -> {
                inv.setStatus("CANCELLED");
                inv.setUpdatedBy(currentUser);
                exportInvoiceRepository.save(inv);
                log.info("Hóa đơn {} đã được hủy tự động theo đơn hàng {}", invoiceCode, orderCode);
            });

            // 2. Tìm các phiếu thu đã lập cho đơn hàng này để hoàn tiền
            List<ReceiptVoucher> receipts = receiptVoucherRepository.findAllByInvoiceCodeAndIsDeletedFalse(invoiceCode);
            if (receipts.isEmpty() && orderCode != null) {
                receipts = receiptVoucherRepository.findAllByInvoiceCodeAndIsDeletedFalse(orderCode);
            }

            BigDecimal totalCollected = receipts.stream()
                    .filter(r -> "COMPLETED".equalsIgnoreCase(r.getStatus()))
                    .map(ReceiptVoucher::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalCollected.compareTo(BigDecimal.ZERO) > 0) {
                String refundVoucherCode = documentSequenceService.generatePaymentCode();
                String receiver = savedOrder.getCustomer() != null ? savedOrder.getCustomer().getName()
                        : (savedOrder.getCustomerName() != null ? savedOrder.getCustomerName() : "Khách hàng");
                String pm = savedOrder.getPaymentMethodCode() != null ? savedOrder.getPaymentMethodCode().toUpperCase() : "TIEN_MAT";
                String fundAccount = (pm.contains("BANK") || pm.contains("TRANSFER") || pm.contains("CK") || pm.contains("VIETQR"))
                        ? "Techcombank - 1902838392 (Công ty StoreManager)"
                        : "Quỹ tiền mặt (Cash)";

                org.example.storemanager.modules.finance.entity.PaymentVoucher pv = org.example.storemanager.modules.finance.entity.PaymentVoucher.builder()
                        .voucherCode(refundVoucherCode)
                        .voucherDate(LocalDateTime.now())
                        .amount(totalCollected)
                        .receiverName(receiver)
                        .status("COMPLETED")
                        .invoiceCode(invoiceCode)
                        .paymentMethod(pm)
                        .fundAccountName(fundAccount)
                        .handler(currentUser)
                        .notes("Hoàn tiền tự động do hủy đơn hàng " + orderCode + " (Số tiền: " + totalCollected + ")")
                        .branch(savedOrder.getBranch())
                        .build();
                pv.setIsDeleted(false);
                pv.setCreatedBy(currentUser);
                paymentVoucherRepository.save(pv);
                log.info("Tự động sinh phiếu chi hoàn tiền {} cho đơn hàng bị hủy {}", refundVoucherCode, orderCode);
            }

            // 3. Điều chỉnh Sổ nợ (DebtLedger) nếu có ghi nhận nợ cho đơn hàng này
            List<DebtLedger> debts = debtLedgerRepository.findByRefCodeAndIsDeletedFalse(invoiceCode);
            if (debts.isEmpty() && orderCode != null) {
                debts = debtLedgerRepository.findByRefCodeAndIsDeletedFalse(orderCode);
            }
            for (DebtLedger d : debts) {
                d.setStatus("SETTLED");
                d.setDecrease(d.getIncrease());
                d.setBalance(BigDecimal.ZERO);
                d.setNotes((d.getNotes() != null ? d.getNotes() + " | " : "") + "Đã hủy do đơn hàng bị hủy (" + currentUser + ")");
                d.setUpdatedBy(currentUser);
                debtLedgerRepository.save(d);
            }
        } catch (Exception e) {
            log.error("Lỗi khi xử lý hủy đơn hàng hoàn tất {}: {}", savedOrder.getOrderCode(), e.getMessage(), e);
            if (e instanceof org.springframework.web.server.ResponseStatusException) {
                throw (org.springframework.web.server.ResponseStatusException) e;
            }
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi xử lý hủy đơn hàng hoàn tất: " + e.getMessage(), e
            );
        }
    }

    private BigDecimal getTaxRateForProduct(org.example.storemanager.modules.catalog.entity.Product product) {
        if (product == null) {
            return BigDecimal.valueOf(0.08);
        }
        return product.getEffectiveVatRate();
    }
}
