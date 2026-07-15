package org.example.storemanager.service.sales.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.config.LogActivity;
import org.example.storemanager.dto.request.sales.saleOrder.CreateSaleOrderRequest;
import org.example.storemanager.dto.request.sales.saleOrder.SaleOrderDetailRequest;
import org.example.storemanager.dto.request.sales.saleOrder.UpdateSaleOrderRequest;
import org.example.storemanager.dto.response.sales.saleOrder.SaleOrderDetailResponse;
import org.example.storemanager.dto.response.sales.saleOrder.SaleOrderResponse;
import org.example.storemanager.dto.response.sales.saleOrder.DeleteSaleOrderResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.entity.sales.SaleOrder;
import org.example.storemanager.entity.sales.SaleOrderDetail;
import org.example.storemanager.entity.system.User;
import org.example.storemanager.enums.ErrorCode;
import org.example.storemanager.enums.sales.OrderStatus;
import org.example.storemanager.enums.sales.PaymentStatus;
import org.example.storemanager.exception.BusinessException;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.sales.SaleOrderRepository;
import org.example.storemanager.repository.system.UserRepository;
import org.example.storemanager.service.sales.SaleOrderService;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleOrderServiceImpl implements SaleOrderService {

    private final SaleOrderRepository saleOrderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SaleOrderResponse createOrder(CreateSaleOrderRequest request) {
        String generatedCode = "SO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        String currentUsername = getCurrentUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUsername));

        SaleOrder order = SaleOrder.builder()
                .orderCode(generatedCode)
                .orderDate(LocalDateTime.now())
                .customerId(request.getCustomerId())
                .branchId(request.getBranchId())
                .userId(currentUser.getId())
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .orderOrigin(request.getOrderOrigin())
                .totalAmount(request.getTotalAmount())
                .discountAmount(request.getDiscountAmount())
                .taxAmount(request.getTaxAmount())
                .finalAmount(request.getFinalAmount())
                .isActive(true)
                .build();
        order.setCreatedBy(currentUsername);

        for (SaleOrderDetailRequest reqDetail : request.getDetails()) {
            SaleOrderDetail detail = SaleOrderDetail.builder()
                    .productVariantId(reqDetail.getProductVariantId())
                    .quantity(reqDetail.getQuantity())
                    .price(reqDetail.getPrice())
                    .discountAmount(reqDetail.getDiscountAmount())
                    .totalAmount(reqDetail.getTotalAmount())
                    .build();
            order.addDetail(detail);
        }

        SaleOrder savedOrder = saleOrderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional
    public SaleOrderResponse updateOrder(Long id, UpdateSaleOrderRequest request) {
        SaleOrder order = saleOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SaleOrder", "id", id));

        if (request.getCustomerId() != null) order.setCustomerId(request.getCustomerId());
        if (request.getDiscountAmount() != null) order.setDiscountAmount(request.getDiscountAmount());
        if (request.getTaxAmount() != null) order.setTaxAmount(request.getTaxAmount());
        if (request.getFinalAmount() != null) order.setFinalAmount(request.getFinalAmount());

        return mapToResponse(saleOrderRepository.save(order));
    }

    @Override
    @Transactional
    @LogActivity(actionType = "DELETE", entityName = "SaleOrder", entityClass = SaleOrder.class)
    public DeleteSaleOrderResponse softDeleteOrder(Long id) {
        // Tìm kiếm bản ghi gốc (không care trạng thái isDeleted để tránh lỗi tìm kiếm lặp)
        SaleOrder order = saleOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SaleOrder", "id", id));

        // BỔ SUNG RÀO KIỂM TRA: Nếu đang bật hoạt động (isActive = true) thì báo lỗi, chặn xóa
        if (Boolean.TRUE.equals(order.getIsActive())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Đơn hàng đang trong trạng thái hoạt động. Vui lòng tắt hoạt động trước khi xóa.");
        }

        String currentUsername = getCurrentUsername();
        order.setIsDeleted(true);
        order.setDeletedAt(LocalDateTime.now());
        order.setDeletedBy(currentUsername);

        SaleOrder deletedOrder = saleOrderRepository.save(order);

        // Trả về DTO chứa đầy đủ thông tin truy vết xóa cho Frontend hiển thị
        return DeleteSaleOrderResponse.builder()
                .id(deletedOrder.getId())
                .orderCode(deletedOrder.getOrderCode())
                .isDeleted(deletedOrder.getIsDeleted())
                .deletedAt(deletedOrder.getDeletedAt())
                .deletedBy(deletedOrder.getDeletedBy())
                .build();
    }

    @Override
    @Transactional
    public SaleOrderResponse updateActiveStatus(Long id, Boolean isActive) {
        SaleOrder order = saleOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SaleOrder", "id", id));
        order.setIsActive(isActive);
        return mapToResponse(saleOrderRepository.save(order));
    }

    @Override
    @Transactional
    public SaleOrderResponse updateOrderStatus(Long id, OrderStatus status) {
        SaleOrder order = saleOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SaleOrder", "id", id));
        order.setStatus(status);
        return mapToResponse(saleOrderRepository.save(order));
    }

    @Override
    @Transactional
    public SaleOrderResponse updatePaymentStatus(Long id, PaymentStatus status) {
        SaleOrder order = saleOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SaleOrder", "id", id));
        order.setPaymentStatus(status);
        return mapToResponse(saleOrderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public SaleOrderResponse getOrderById(Long id) {
        // THAY ĐỔI: Dùng findById thay vì findByIdAndIsDeletedFalse để đơn đã xóa mềm vẫn xem chi tiết được
        SaleOrder order = saleOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SaleOrder", "id", id));
        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleOrderResponse> getActiveOrders() {
        return saleOrderRepository.findByIsActiveTrueAndIsDeletedFalse().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SaleOrderResponse> searchOrders(String keyword, OrderStatus status, Long branchId, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SaleOrder> orderPage = saleOrderRepository.searchOrders(keyword, status, branchId, pageable);

        List<SaleOrderResponse> content = orderPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<SaleOrderResponse>builder()
                .content(content)
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .build();
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED, "Người dùng chưa đăng nhập hoặc token không hợp lệ");
    }

    private SaleOrderResponse mapToResponse(SaleOrder order) {
        List<SaleOrderDetailResponse> details = order.getDetails().stream().map(d -> {
            SaleOrderDetailResponse res = new SaleOrderDetailResponse();
            res.setId(d.getId());
            res.setProductVariantId(d.getProductVariantId());
            res.setQuantity(d.getQuantity());
            res.setPrice(d.getPrice());
            res.setDiscountAmount(d.getDiscountAmount());
            res.setTotalAmount(d.getTotalAmount());
            return res;
        }).collect(Collectors.toList());

        return SaleOrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .customerId(order.getCustomerId())
                .branchId(order.getBranchId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .orderOrigin(order.getOrderOrigin())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .finalAmount(order.getFinalAmount())
                .isActive(order.getIsActive())
                .isDeleted(order.getIsDeleted()) // <--- MAP THÊM CỜ TRẠNG THÁI XÓA VÀO ĐÂY
                .createdAt(order.getCreatedAt())
                .createdBy(order.getCreatedBy())
                .details(details)
                .build();
    }
}