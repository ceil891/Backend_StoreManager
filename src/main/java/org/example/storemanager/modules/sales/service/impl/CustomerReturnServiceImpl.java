package org.example.storemanager.modules.sales.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.sales.dto.request.CreateCustomerReturnRequest;
import org.example.storemanager.modules.sales.dto.request.CustomerReturnDetailRequest;
import org.example.storemanager.modules.sales.dto.request.UpdateCustomerReturnRequest;
import org.example.storemanager.modules.sales.dto.response.CustomerReturnDetailResponse;
import org.example.storemanager.modules.sales.dto.response.CustomerReturnResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.sales.entity.CustomerReturn;
import org.example.storemanager.modules.sales.entity.CustomerReturnDetail;
import org.example.storemanager.modules.sales.entity.ExportInvoice;
import org.example.storemanager.modules.partnerarea.entity.Customer;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.sales.repository.CustomerReturnRepository;
import org.example.storemanager.modules.sales.repository.CustomerReturnDetailRepository;
import org.example.storemanager.modules.sales.repository.ExportInvoiceRepository;
import org.example.storemanager.modules.partnerarea.repository.CustomerRepository;
import org.example.storemanager.modules.system.repository.BranchRepository;
import org.example.storemanager.modules.catalog.repository.ProductRepository;
import org.example.storemanager.modules.sales.service.CustomerReturnService;
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
public class CustomerReturnServiceImpl implements CustomerReturnService {

    private final CustomerReturnRepository customerReturnRepository;
    private final CustomerReturnDetailRepository customerReturnDetailRepository;
    private final ExportInvoiceRepository exportInvoiceRepository;
    private final org.example.storemanager.modules.sales.repository.SaleOrderRepository saleOrderRepository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final org.example.storemanager.modules.catalog.repository.ProductVariantRepository productVariantRepository;
    private final org.example.storemanager.modules.crm.service.LoyaltyService loyaltyService;
    private final org.example.storemanager.modules.inventory.service.InventoryService inventoryService;
    private final org.example.storemanager.modules.wms.service.WarehouseService warehouseService;

    @Override
    public CustomerReturnResponse createReturn(CreateCustomerReturnRequest request) {
        String username = getCurrentUsername();

        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findByIdAndIsDeletedFalse(request.getCustomerId()).orElse(null);
        }

        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId()).orElse(null);
        }
        if (branch == null) {
            branch = branchRepository.findAll().stream().filter(b -> !Boolean.TRUE.equals(b.getIsDeleted())).findFirst().orElse(null);
        }

        ExportInvoice invoice = null;
        if (request.getInvoiceId() != null) {
            invoice = exportInvoiceRepository.findByIdAndIsDeletedFalse(request.getInvoiceId()).orElse(null);
        }

        org.example.storemanager.modules.sales.entity.SaleOrder order = null;
        if (request.getOrderId() != null) {
            order = saleOrderRepository.findByIdAndIsDeletedFalse(request.getOrderId()).orElse(null);
        }

        CustomerReturn customerReturn = CustomerReturn.builder()
                .returnCode(request.getReturnCode() != null ? request.getReturnCode() : "RET-" + System.currentTimeMillis())
                .returnRequestId(request.getReturnRequestId())
                .returnRequestCode(request.getReturnRequestCode())
                .returnDate(request.getReturnDate() != null ? request.getReturnDate() : LocalDateTime.now())
                .reason(request.getReason())
                .status(request.getStatus() != null ? request.getStatus() : "COMPLETED")
                .customer(customer)
                .invoice(invoice)
                .order(order)
                .branch(branch)
                .build();

        customerReturn.setIsDeleted(false);
        customerReturn.setCreatedBy(username);
        customerReturn.setNote(request.getNote());

        BigDecimal totalRefund = BigDecimal.ZERO;
        List<CustomerReturnDetail> details = new ArrayList<>();

        if (request.getDetails() != null) {
            for (CustomerReturnDetailRequest detailReq : request.getDetails()) {
                Product product = null;
                if (detailReq.getProductId() != null) {
                    product = productRepository.findByIdAndIsDeletedFalse(detailReq.getProductId()).orElse(null);
                    if (product == null) {
                        // Kiểm tra nếu detailReq.getProductId() là ID của ProductVariant
                        org.example.storemanager.modules.catalog.entity.ProductVariant pv =
                                productVariantRepository.findByIdAndIsDeletedFalse(detailReq.getProductId()).orElse(null);
                        if (pv != null && pv.getProduct() != null) {
                            product = pv.getProduct();
                        }
                    }
                }
                if (product == null) {
                    product = productRepository.findAll().stream()
                            .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                            .findFirst().orElse(null);
                }
                if (product == null) {
                    throw new ResourceNotFoundException("Product", "id", detailReq.getProductId());
                }

                BigDecimal refundPrice = detailReq.getRefundPrice() != null ? detailReq.getRefundPrice() : (product.getBasePrice() != null ? product.getBasePrice() : BigDecimal.ZERO);
                BigDecimal quantity = detailReq.getQuantity() != null ? detailReq.getQuantity() : BigDecimal.ONE;
                BigDecimal subTotal = quantity.multiply(refundPrice);
                totalRefund = totalRefund.add(subTotal);

                CustomerReturnDetail detail = CustomerReturnDetail.builder()
                        .customerReturn(customerReturn)
                        .product(product)
                        .quantity(quantity)
                        .refundPrice(refundPrice)
                        .subTotal(subTotal)
                        .build();

                detail.setIsDeleted(false);
                detail.setCreatedBy(username);
                details.add(detail);
            }
        }

        customerReturn.setTotalRefund(totalRefund);
        CustomerReturn savedReturn = customerReturnRepository.save(customerReturn);
        customerReturnDetailRepository.saveAll(details);

        // Tự động hoàn tồn kho nếu trả hàng đã hoàn tất (COMPLETED)
        if ("COMPLETED".equalsIgnoreCase(savedReturn.getStatus()) && branch != null) {
            try {
                org.example.storemanager.modules.wms.entity.WarehouseZone defaultZone = 
                        warehouseService.getOrCreateDefaultZone(branch);
                for (CustomerReturnDetail d : details) {
                    if (d.getProduct() != null) {
                        inventoryService.addStock(
                                defaultZone.getId(),
                                branch.getId(),
                                d.getProduct().getId(),
                                null,
                                null,
                                d.getQuantity(),
                                "RETURN",
                                savedReturn.getReturnCode(),
                                savedReturn.getId()
                        );
                    }
                }
            } catch (Exception e) {
                System.err.println("Cảnh báo khi hoàn tồn kho hàng trả lại: " + e.getMessage());
            }
        }

        // Thu hồi điểm tích lũy của khách hàng tương ứng số tiền hoàn
        if (customer != null && (invoice != null || order != null)) {
            try {
                String refCode = invoice != null ? invoice.getInvoiceCode() : (order != null ? order.getOrderCode() : "RET-" + savedReturn.getReturnCode());
                BigDecimal origAmount = BigDecimal.ONE;
                if (invoice != null && invoice.getTotalAmount() != null) {
                    origAmount = invoice.getTotalAmount();
                } else if (order != null) {
                    origAmount = order.getFinalAmount() != null ? order.getFinalAmount() : (order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ONE);
                }
                loyaltyService.processOrderRefund(
                        customer.getId(),
                        savedReturn.getReturnCode(),
                        refCode,
                        totalRefund,
                        origAmount
                );
            } catch (Exception e) {
                System.err.println("Cảnh báo khi thu hồi điểm đơn hoàn: " + e.getMessage());
            }
        }

        return mapToResponse(savedReturn, details);
    }

    @Override
    public CustomerReturnResponse updateReturn(Long id, UpdateCustomerReturnRequest request) {
        CustomerReturn customerReturn = customerReturnRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerReturn", "id", id));

        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findByIdAndIsDeletedFalse(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));
        }

        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        ExportInvoice invoice = null;
        if (request.getInvoiceId() != null) {
            invoice = exportInvoiceRepository.findByIdAndIsDeletedFalse(request.getInvoiceId()).orElse(null);
        }
        if (invoice == null) {
            invoice = customerReturn.getInvoice();
        }

        String username = getCurrentUsername();

        customerReturn.setReturnDate(request.getReturnDate());
        customerReturn.setReason(request.getReason());
        customerReturn.setStatus(request.getStatus());
        customerReturn.setCustomer(customer);
        customerReturn.setInvoice(invoice);
        customerReturn.setBranch(branch);
        customerReturn.setNote(request.getNote());
        customerReturn.setUpdatedBy(username);

        // Soft delete old details
        List<CustomerReturnDetail> oldDetails = customerReturnDetailRepository.findByCustomerReturnIdAndIsDeletedFalse(id);
        for (CustomerReturnDetail detail : oldDetails) {
            detail.setIsDeleted(true);
            detail.setDeletedBy(username);
            detail.setDeletedAt(LocalDateTime.now());
        }
        customerReturnDetailRepository.saveAll(oldDetails);

        // Add new details
        BigDecimal totalRefund = BigDecimal.ZERO;
        List<CustomerReturnDetail> newDetails = new ArrayList<>();

        for (CustomerReturnDetailRequest detailReq : request.getDetails()) {
            Product product = productRepository.findByIdAndIsDeletedFalse(detailReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", detailReq.getProductId()));

            BigDecimal subTotal = detailReq.getQuantity().multiply(detailReq.getRefundPrice());
            totalRefund = totalRefund.add(subTotal);

            CustomerReturnDetail detail = CustomerReturnDetail.builder()
                    .customerReturn(customerReturn)
                    .product(product)
                    .quantity(detailReq.getQuantity())
                    .refundPrice(detailReq.getRefundPrice())
                    .subTotal(subTotal)
                    .build();

            detail.setIsDeleted(false);
            detail.setCreatedBy(username);
            newDetails.add(detail);
        }

        customerReturn.setTotalRefund(totalRefund);
        CustomerReturn savedReturn = customerReturnRepository.save(customerReturn);
        customerReturnDetailRepository.saveAll(newDetails);

        return mapToResponse(savedReturn, newDetails);
    }

    @Override
    public CustomerReturnResponse updateStatus(Long id, String status) {
        CustomerReturn customerReturn = customerReturnRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerReturn", "id", id));

        String oldStatus = customerReturn.getStatus();
        customerReturn.setStatus(status);
        customerReturn.setUpdatedBy(getCurrentUsername());

        CustomerReturn savedReturn = customerReturnRepository.save(customerReturn);
        List<CustomerReturnDetail> details = customerReturnDetailRepository.findByCustomerReturnIdAndIsDeletedFalse(id);

        // Kích hoạt hoàn tồn kho thực tế nếu chuyển sang COMPLETED hoặc STOCK_IN và trước đó chưa hoàn kho
        if (("COMPLETED".equalsIgnoreCase(status) || "STOCK_IN".equalsIgnoreCase(status))
                && !"COMPLETED".equalsIgnoreCase(oldStatus) && !"STOCK_IN".equalsIgnoreCase(oldStatus)) {
            Branch branch = customerReturn.getBranch();
            if (branch == null && customerReturn.getInvoice() != null && customerReturn.getInvoice().getBranch() != null) {
                branch = customerReturn.getInvoice().getBranch();
            }
            if (branch == null) {
                branch = branchRepository.findAll().stream().filter(b -> !Boolean.TRUE.equals(b.getIsDeleted())).findFirst().orElse(null);
            }
            if (branch != null) {
                try {
                    org.example.storemanager.modules.wms.entity.WarehouseZone defaultZone = 
                            warehouseService.getOrCreateDefaultZone(branch);
                    for (CustomerReturnDetail d : details) {
                        if (d.getProduct() != null) {
                            inventoryService.addStock(
                                    defaultZone.getId(),
                                    branch.getId(),
                                    d.getProduct().getId(),
                                    null,
                                    null,
                                    d.getQuantity() != null ? d.getQuantity() : BigDecimal.ONE,
                                    "RETURN",
                                    savedReturn.getReturnCode(),
                                    savedReturn.getId()
                            );
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Cảnh báo khi hoàn tồn kho hàng trả lại: " + e.getMessage());
                }
            }
        }

        return mapToResponse(savedReturn, details);
    }

    @Override
    public void deleteReturn(Long id) {
        CustomerReturn customerReturn = customerReturnRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerReturn", "id", id));

        String username = getCurrentUsername();
        customerReturn.setIsDeleted(true);
        customerReturn.setDeletedBy(username);
        customerReturn.setDeletedAt(LocalDateTime.now());
        customerReturnRepository.save(customerReturn);

        List<CustomerReturnDetail> details = customerReturnDetailRepository.findByCustomerReturnIdAndIsDeletedFalse(id);
        for (CustomerReturnDetail detail : details) {
            detail.setIsDeleted(true);
            detail.setDeletedBy(username);
            detail.setDeletedAt(LocalDateTime.now());
        }
        customerReturnDetailRepository.saveAll(details);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerReturnResponse getReturnById(Long id) {
        CustomerReturn customerReturn = customerReturnRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerReturn", "id", id));

        List<CustomerReturnDetail> details = customerReturnDetailRepository.findByCustomerReturnIdAndIsDeletedFalse(id);
        return mapToResponse(customerReturn, details);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerReturnResponse> getAllReturns(String search, String status, Long branchId, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sorting);
        Page<CustomerReturn> pageResult = customerReturnRepository.findAllReturns(search, status, branchId, includeDeleted, pageable);
        return pageResult.getContent().stream()
                .map(r -> {
                    List<CustomerReturnDetail> details = customerReturnDetailRepository.findByCustomerReturnIdAndIsDeletedFalse(r.getId());
                    return mapToResponse(r, details);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerReturnResponse> getReturnsPaginated(String search, String status, Long branchId, int page, int size, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<CustomerReturn> pageResult = customerReturnRepository.findAllReturns(search, status, branchId, includeDeleted, pageable);

        List<CustomerReturnResponse> content = pageResult.getContent().stream()
                .map(r -> {
                    List<CustomerReturnDetail> details = customerReturnDetailRepository.findByCustomerReturnIdAndIsDeletedFalse(r.getId());
                    return mapToResponse(r, details);
                })
                .collect(Collectors.toList());

        return PageResponse.<CustomerReturnResponse>builder()
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

    private CustomerReturnResponse mapToResponse(CustomerReturn r, List<CustomerReturnDetail> details) {
        List<CustomerReturnDetailResponse> detailsResponse = details.stream()
                .map(d -> CustomerReturnDetailResponse.builder()
                        .id(d.getId())
                        .productId(d.getProduct().getId())
                        .productCode(d.getProduct().getProductCode())
                        .productName(d.getProduct().getName())
                        .quantity(d.getQuantity())
                        .refundPrice(d.getRefundPrice())
                        .subTotal(d.getSubTotal())
                        .build())
                .collect(Collectors.toList());

        return CustomerReturnResponse.builder()
                .id(r.getId())
                .returnCode(r.getReturnCode())
                .returnRequestId(r.getReturnRequestId())
                .returnRequestCode(r.getReturnRequestCode())
                .returnDate(r.getReturnDate())
                .totalRefund(r.getTotalRefund())
                .reason(r.getReason())
                .status(r.getStatus())
                .customerId(r.getCustomer() != null ? r.getCustomer().getId() : null)
                .customerName(r.getCustomer() != null ? r.getCustomer().getName() : null)
                .invoiceId(r.getInvoice() != null ? r.getInvoice().getId() : null)
                .invoiceCode(r.getInvoice() != null ? r.getInvoice().getInvoiceCode() : null)
                .orderId(r.getOrder() != null ? r.getOrder().getId() : null)
                .orderCode(r.getOrder() != null ? r.getOrder().getOrderCode() : null)
                .branchId(r.getBranch() != null ? r.getBranch().getId() : null)
                .branchName(r.getBranch() != null ? r.getBranch().getBranchName() : null)
                .note(r.getNote())
                .createdAt(r.getCreatedAt())
                .createdBy(r.getCreatedBy())
                .details(detailsResponse)
                .build();
    }
}
