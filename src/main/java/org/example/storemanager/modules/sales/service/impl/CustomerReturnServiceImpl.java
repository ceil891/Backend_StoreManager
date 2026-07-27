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
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    @Override
    public CustomerReturnResponse createReturn(CreateCustomerReturnRequest request) {
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findByIdAndIsDeletedFalse(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));
        }

        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        ExportInvoice invoice = exportInvoiceRepository.findByIdAndIsDeletedFalse(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("ExportInvoice", "id", request.getInvoiceId()));

        String username = getCurrentUsername();

        CustomerReturn customerReturn = CustomerReturn.builder()
                .returnCode(request.getReturnCode())
                .returnDate(request.getReturnDate())
                .reason(request.getReason())
                .status(request.getStatus())
                .customer(customer)
                .invoice(invoice)
                .branch(branch)
                .build();

        customerReturn.setIsDeleted(false);
        customerReturn.setCreatedBy(username);
        customerReturn.setNote(request.getNote());

        BigDecimal totalRefund = BigDecimal.ZERO;
        List<CustomerReturnDetail> details = new ArrayList<>();

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
            details.add(detail);
        }

        customerReturn.setTotalRefund(totalRefund);
        CustomerReturn savedReturn = customerReturnRepository.save(customerReturn);
        customerReturnDetailRepository.saveAll(details);

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

        ExportInvoice invoice = exportInvoiceRepository.findByIdAndIsDeletedFalse(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("ExportInvoice", "id", request.getInvoiceId()));

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

        customerReturn.setStatus(status);
        customerReturn.setUpdatedBy(getCurrentUsername());

        CustomerReturn savedReturn = customerReturnRepository.save(customerReturn);
        List<CustomerReturnDetail> details = customerReturnDetailRepository.findByCustomerReturnIdAndIsDeletedFalse(id);
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
                .returnDate(r.getReturnDate())
                .totalRefund(r.getTotalRefund())
                .reason(r.getReason())
                .status(r.getStatus())
                .customerId(r.getCustomer() != null ? r.getCustomer().getId() : null)
                .customerName(r.getCustomer() != null ? r.getCustomer().getName() : null)
                .invoiceId(r.getInvoice().getId())
                .invoiceCode(r.getInvoice().getInvoiceCode())
                .branchId(r.getBranch().getId())
                .branchName(r.getBranch().getBranchName())
                .note(r.getNote())
                .createdAt(r.getCreatedAt())
                .createdBy(r.getCreatedBy())
                .details(detailsResponse)
                .build();
    }
}
