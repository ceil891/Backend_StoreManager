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
    private final org.example.storemanager.modules.crm.service.LoyaltyService loyaltyService;

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
        if (branch == null) {
            branch = Branch.builder()
                    .branchName("Chi nhánh chính")
                    .branchCode("BR-MAIN")
                    .build();
            branch.setIsDeleted(false);
            branch.setCreatedBy(username);
            branch = branchRepository.save(branch);
        }

        ExportInvoice invoice = null;
        if (request.getInvoiceId() != null) {
            invoice = exportInvoiceRepository.findByIdAndIsDeletedFalse(request.getInvoiceId()).orElse(null);
        }
        if (invoice == null) {
            List<ExportInvoice> allInvoices = exportInvoiceRepository.findAll();
            if (!allInvoices.isEmpty()) {
                invoice = allInvoices.get(0);
            } else {
                invoice = ExportInvoice.builder()
                        .invoiceCode("INV-RET-" + (request.getReturnCode() != null ? request.getReturnCode() : System.currentTimeMillis()))
                        .invoiceDate(LocalDateTime.now())
                        .status("ISSUED")
                        .customer(customer)
                        .branch(branch)
                        .totalAmount(BigDecimal.ZERO)
                        .build();
                invoice.setIsDeleted(false);
                invoice.setCreatedBy(username != null ? username : "SYSTEM");
                invoice = exportInvoiceRepository.save(invoice);
            }
        }

        CustomerReturn customerReturn = CustomerReturn.builder()
                .returnCode(request.getReturnCode())
                .returnRequestId(request.getReturnRequestId())
                .returnRequestCode(request.getReturnRequestCode())
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
            Product product = null;
            if (detailReq.getProductId() != null) {
                product = productRepository.findByIdAndIsDeletedFalse(detailReq.getProductId()).orElse(null);
            }
            if (product == null) {
                product = productRepository.findAll().stream().filter(p -> !Boolean.TRUE.equals(p.getIsDeleted())).findFirst().orElse(null);
            }
            if (product == null) {
                continue;
            }

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

        // Thu hồi điểm tích lũy của khách hàng tương ứng số tiền hoàn
        if (customer != null && invoice != null) {
            try {
                BigDecimal origAmount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ONE;
                loyaltyService.processOrderRefund(
                        customer.getId(),
                        savedReturn.getReturnCode(),
                        invoice.getInvoiceCode(),
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
        if (invoice == null) {
            List<ExportInvoice> allInvoices = exportInvoiceRepository.findAll();
            if (!allInvoices.isEmpty()) {
                invoice = allInvoices.get(0);
            }
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
                .returnRequestId(r.getReturnRequestId())
                .returnRequestCode(r.getReturnRequestCode())
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
