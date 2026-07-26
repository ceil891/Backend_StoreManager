package org.example.storemanager.service.sales.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.sales.CreateSaleOrderRequest;
import org.example.storemanager.dto.request.sales.SaleOrderDetailRequest;
import org.example.storemanager.dto.request.sales.UpdateSaleOrderRequest;
import org.example.storemanager.dto.response.sales.SaleOrderDetailResponse;
import org.example.storemanager.dto.response.sales.SaleOrderResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.entity.sales.SaleOrder;
import org.example.storemanager.entity.sales.SaleOrderDetail;
import org.example.storemanager.entity.partnerarea.Customer;
import org.example.storemanager.entity.system.Branch;
import org.example.storemanager.entity.catalog.ProductVariant;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.sales.SaleOrderRepository;
import org.example.storemanager.repository.sales.SaleOrderDetailRepository;
import org.example.storemanager.repository.partnerarea.CustomerRepository;
import org.example.storemanager.repository.system.BranchRepository;
import org.example.storemanager.repository.catalog.ProductVariantRepository;
import org.example.storemanager.service.sales.SaleOrderService;
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

    @Override
    public SaleOrderResponse createOrder(CreateSaleOrderRequest request) {
        Customer customer = customerRepository.findByIdAndIsDeletedFalse(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        String username = getCurrentUsername();

        SaleOrder order = SaleOrder.builder()
                .orderCode(request.getOrderCode())
                .orderDate(request.getOrderDate())
                .expectedDelivery(request.getExpectedDelivery())
                .status(request.getStatus())
                .customer(customer)
                .branch(branch)
                .build();

        order.setIsDeleted(false);
        order.setCreatedBy(username);
        order.setNote(request.getNote());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<SaleOrderDetail> details = new ArrayList<>();

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
                    .unitPriceSnapshot(detailReq.getUnitPriceSnapshot())
                    .subTotal(subTotal)
                    .taxRate(getTaxRateForProduct(variant.getProduct()))
                    .build();

            detail.setIsDeleted(false);
            detail.setCreatedBy(username);
            details.add(detail);
        }

        order.setTotalAmount(totalAmount);
        SaleOrder savedOrder = saleOrderRepository.save(order);
        saleOrderDetailRepository.saveAll(details);

        return mapToResponse(savedOrder, details);
    }

    @Override
    public SaleOrderResponse updateOrder(Long id, UpdateSaleOrderRequest request) {
        SaleOrder order = saleOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SaleOrder", "id", id));

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
                    .unitPriceSnapshot(detailReq.getUnitPriceSnapshot())
                    .subTotal(subTotal)
                    .taxRate(getTaxRateForProduct(variant.getProduct()))
                    .build();

            detail.setIsDeleted(false);
            detail.setCreatedBy(username);
            newDetails.add(detail);
        }

        order.setTotalAmount(totalAmount);
        SaleOrder savedOrder = saleOrderRepository.save(order);
        saleOrderDetailRepository.saveAll(newDetails);

        return mapToResponse(savedOrder, newDetails);
    }

    @Override
    public SaleOrderResponse updateStatus(Long id, String status) {
        SaleOrder order = saleOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SaleOrder", "id", id));

        order.setStatus(status);
        order.setUpdatedBy(getCurrentUsername());

        SaleOrder savedOrder = saleOrderRepository.save(order);
        List<SaleOrderDetail> details = saleOrderDetailRepository.findByOrderIdAndIsDeletedFalse(id);
        return mapToResponse(savedOrder, details);
    }

    @Override
    public void deleteOrder(Long id) {
        SaleOrder order = saleOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SaleOrder", "id", id));

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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Người dùng chưa đăng nhập hoặc token không hợp lệ");
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
                .customerId(o.getCustomer().getId())
                .customerName(o.getCustomer().getName())
                .branchId(o.getBranch().getId())
                .branchName(o.getBranch().getBranchName())
                .note(o.getNote())
                .createdAt(o.getCreatedAt())
                .createdBy(o.getCreatedBy())
                .details(detailsResponse)
                .build();
    }

    private BigDecimal getTaxRateForProduct(org.example.storemanager.entity.catalog.Product product) {
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
