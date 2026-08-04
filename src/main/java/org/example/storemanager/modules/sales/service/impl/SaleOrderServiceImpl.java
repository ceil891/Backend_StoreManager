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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public SaleOrderResponse createOrder(CreateSaleOrderRequest request) {
        Customer customer = customerRepository.findByIdAndIsDeletedFalse(request.getCustomerId())
                .orElseGet(() -> customerRepository.findAll().stream().filter(c -> Boolean.FALSE.equals(c.getIsDeleted())).findFirst().orElse(null));

        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseGet(() -> branchRepository.findAll().stream().filter(b -> Boolean.FALSE.equals(b.getIsDeleted())).findFirst().orElse(null));

        String username = getCurrentUsername();

        SaleOrder order = SaleOrder.builder()
                .orderCode(request.getOrderCode())
                .orderDate(request.getOrderDate() != null ? request.getOrderDate() : LocalDateTime.now())
                .expectedDelivery(request.getExpectedDelivery())
                .status(request.getStatus() != null ? request.getStatus() : "PENDING")
                .customer(customer)
                .branch(branch)
                .build();

        order.setIsDeleted(false);
        order.setCreatedBy(username != null ? username : "ONLINE_STORE");
        String origin = request.getOrderOrigin();
        if (origin == null || origin.trim().isEmpty() || "ONLINE_STORE".equalsIgnoreCase(origin)) {
            origin = "ONLINE";
        }
        order.setOrderOrigin(origin);
        order.setPaymentStatus(request.getPaymentStatus() != null && !request.getPaymentStatus().trim().isEmpty() ? request.getPaymentStatus() : "UNPAID");
        order.setNote(request.getNote());
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setShippingAddress(request.getShippingAddress());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<SaleOrderDetail> details = new ArrayList<>();

        if (request.getDetails() != null) {
            for (SaleOrderDetailRequest detailReq : request.getDetails()) {
                ProductVariant variant = productVariantRepository.findByIdAndIsDeletedFalse(detailReq.getProductVariantId())
                        .orElseGet(() -> productVariantRepository.findAll().stream().filter(v -> Boolean.FALSE.equals(v.getIsDeleted())).findFirst().orElse(null));

                if (variant != null) {
                    BigDecimal subTotal = detailReq.getQuantity().multiply(detailReq.getUnitPriceSnapshot());
                    totalAmount = totalAmount.add(subTotal);

                    SaleOrderDetail detail = SaleOrderDetail.builder()
                            .order(order)
                            .productVariant(variant)
                            .productNameSnapshot(variant.getProduct() != null ? variant.getProduct().getName() : "Sản phẩm Online")
                            .skuSnapshot(variant.getSku() != null ? variant.getSku() : "SKU-ONLINE")
                            .barcodeSnapshot(variant.getBarcode())
                            .variantDescriptionSnapshot(variant.getVariantCode())
                            .quantity(detailReq.getQuantity())
                            .unitPrice(detailReq.getUnitPriceSnapshot())
                            .unitPriceSnapshot(detailReq.getUnitPriceSnapshot())
                            .subTotal(subTotal)
                            .totalAmount(subTotal)
                            .taxRate(getTaxRateForProduct(variant.getProduct()))
                            .build();

                    detail.setIsDeleted(false);
                    detail.setCreatedBy(username != null ? username : "ONLINE_STORE");
                    details.add(detail);
                }
            }
        }

        order.setTotalAmount(totalAmount);
        order.setFinalAmount(totalAmount); // final_amount NOT NULL — set same as totalAmount for online orders
        SaleOrder savedOrder = saleOrderRepository.save(order);
        saleOrderDetailRepository.saveAll(details);

        // Tự động trừ tồn kho thực tế nếu đơn hàng đã hoàn tất (COMPLETED)
        if ("COMPLETED".equalsIgnoreCase(savedOrder.getStatus())) {
            try {
                org.example.storemanager.modules.wms.entity.WarehouseZone defaultZone = 
                        warehouseService.getOrCreateDefaultZone(branch);
                for (SaleOrderDetail detail : details) {
                    ProductVariant pv = detail.getProductVariant();
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
            } catch (Exception e) {
                // Log warning nếu có lỗi khi trừ tồn kho nhưng vẫn cho đơn tạo thành công
                System.err.println("Cảnh báo khi trừ tồn kho đơn hàng: " + e.getMessage());
            }
        }

        // Transactional Outbox Pattern: Save Event to Outbox table in the SAME DB Transaction
        org.example.storemanager.shared.event.payload.OrderCreatedEventPayload payload = 
                org.example.storemanager.shared.event.payload.OrderCreatedEventPayload.builder()
                .orderId(savedOrder.getId().toString())
                .customerId(customer.getId())
                .branchId(branch.getId())
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
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<SaleOrderDetail> newDetails = new ArrayList<>();

        for (SaleOrderDetailRequest detailReq : request.getDetails()) {
            ProductVariant variant = productVariantRepository.findByIdAndIsDeletedFalse(detailReq.getProductVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", detailReq.getProductVariantId()));

            BigDecimal subTotal = detailReq.getQuantity().multiply(detailReq.getUnitPriceSnapshot());
            totalAmount = totalAmount.add(subTotal);

            SaleOrderDetail detail = SaleOrderDetail.builder()
                    .order(order)
                    .productVariant(variant)
                    .productNameSnapshot(variant.getProduct().getName())
                    .skuSnapshot(variant.getSku())
                    .barcodeSnapshot(variant.getBarcode())
                    .variantDescriptionSnapshot(variant.getVariantCode())
                    .quantity(detailReq.getQuantity())
                    .unitPrice(detailReq.getUnitPriceSnapshot())
                    .unitPriceSnapshot(detailReq.getUnitPriceSnapshot())
                    .subTotal(subTotal)
                    .totalAmount(subTotal)
                    .taxRate(getTaxRateForProduct(variant.getProduct()))
                    .build();

            detail.setIsDeleted(false);
            detail.setCreatedBy(username);
            newDetails.add(detail);
        }

        order.setTotalAmount(totalAmount);
        order.setFinalAmount(totalAmount); // Keep final_amount in sync
        SaleOrder savedOrder = saleOrderRepository.save(order);
        saleOrderDetailRepository.saveAll(newDetails);

        return mapToResponse(savedOrder, newDetails);
    }

    @Override
    public SaleOrderResponse updateStatus(Long id, String status) {
        return updateStatus(id, status, null, null, null, null);
    }

    @Override
    public SaleOrderResponse updateStatus(Long id, String status, String carrier, String trackingCode, String shipperName, String shipperPhone) {
        SaleOrder order = saleOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SaleOrder", "id", id));

        String oldStatus = order.getStatus();
        // Normalize status: map FE/logistics aliases to DB-accepted values
        String normalizedStatus = normalizeOrderStatus(status);
        order.setStatus(normalizedStatus);
        String effectiveStatus = normalizedStatus; // used for delivery logic below

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
        return pageResult.getContent().stream()
                .map(o -> {
                    List<SaleOrderDetail> details = saleOrderDetailRepository.findByOrderIdAndIsDeletedFalse(o.getId());
                    return mapToResponse(o, details);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SaleOrderResponse> getOrdersPaginated(String search, String status, Long branchId, int page, int size, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<SaleOrder> pageResult = saleOrderRepository.findAllOrders(search, status, branchId, includeDeleted, pageable);

        List<SaleOrderResponse> content = pageResult.getContent().stream()
                .map(o -> {
                    List<SaleOrderDetail> details = saleOrderDetailRepository.findByOrderIdAndIsDeletedFalse(o.getId());
                    return mapToResponse(o, details);
                })
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
            return Sort.by("id").descending();
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
                .map(d -> SaleOrderDetailResponse.builder()
                        .id(d.getId())
                        .productVariantId(d.getProductVariant().getId())
                        .variantCode(d.getProductVariant().getVariantCode())
                        .skuSnapshot(d.getSkuSnapshot())
                        .barcodeSnapshot(d.getBarcodeSnapshot())
                        .productNameSnapshot(d.getProductNameSnapshot())
                        .variantDescriptionSnapshot(d.getVariantDescriptionSnapshot())
                        .quantity(d.getQuantity())
                        .unitPriceSnapshot(d.getUnitPriceSnapshot())
                        .subTotal(d.getSubTotal())
                        .build())
                .collect(Collectors.toList());

        return SaleOrderResponse.builder()
                .id(o.getId())
                .orderCode(o.getOrderCode())
                .orderDate(o.getOrderDate())
                .expectedDelivery(o.getExpectedDelivery())
                .totalAmount(o.getTotalAmount())
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
                .details(detailsResponse)
                .build();
    }

    private BigDecimal getTaxRateForProduct(org.example.storemanager.modules.catalog.entity.Product product) {
        if (product.getCategory() != null && product.getCategory().getTaxClass() != null) {
            switch (product.getCategory().getTaxClass()) {
                case VAT_5: return BigDecimal.valueOf(0.05);
                case VAT_8: return BigDecimal.valueOf(0.08);
                case VAT_10: return BigDecimal.valueOf(0.10);
                case EXEMPT: return BigDecimal.ZERO;
            }
        }
        return BigDecimal.valueOf(0.08); // fallback
    }
}
