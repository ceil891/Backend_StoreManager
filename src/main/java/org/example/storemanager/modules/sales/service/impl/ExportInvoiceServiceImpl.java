package org.example.storemanager.modules.sales.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.sales.dto.request.CreateExportInvoiceRequest;
import org.example.storemanager.modules.sales.dto.request.ExportInvoiceDetailRequest;
import org.example.storemanager.modules.sales.dto.request.UpdateExportInvoiceRequest;
import org.example.storemanager.modules.sales.dto.response.ExportInvoiceDetailResponse;
import org.example.storemanager.modules.sales.dto.response.ExportInvoiceResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.sales.entity.ExportInvoice;
import org.example.storemanager.modules.sales.entity.ExportInvoiceDetail;
import org.example.storemanager.modules.partnerarea.entity.Customer;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.system.entity.PosSession;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.inventory.entity.ProductBatch;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.sales.repository.ExportInvoiceRepository;
import org.example.storemanager.modules.sales.repository.ExportInvoiceDetailRepository;
import org.example.storemanager.modules.partnerarea.repository.CustomerRepository;
import org.example.storemanager.modules.system.repository.BranchRepository;
import org.example.storemanager.modules.system.repository.PosSessionRepository;
import org.example.storemanager.modules.catalog.repository.ProductRepository;
import org.example.storemanager.modules.inventory.repository.ProductBatchRepository;
import org.example.storemanager.modules.sales.service.ExportInvoiceService;
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
public class ExportInvoiceServiceImpl implements ExportInvoiceService {

    private final ExportInvoiceRepository exportInvoiceRepository;
    private final ExportInvoiceDetailRepository exportInvoiceDetailRepository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final PosSessionRepository posSessionRepository;
    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;

    @Override
    public ExportInvoiceResponse createInvoice(CreateExportInvoiceRequest request) {
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findByIdAndIsDeletedFalse(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));
        }

        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        PosSession posSession = null;
        if (request.getPosSessionId() != null) {
            posSession = posSessionRepository.findByIdAndIsDeletedFalse(request.getPosSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("PosSession", "id", request.getPosSessionId()));
        }

        String username = getCurrentUsername();

        String code = (request.getInvoiceCode() != null && !request.getInvoiceCode().trim().isEmpty())
                ? request.getInvoiceCode()
                : "HD" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        ExportInvoice invoice = ExportInvoice.builder()
                .invoiceCode(code)
                .invoiceDate(request.getInvoiceDate() != null ? request.getInvoiceDate() : LocalDateTime.now())
                .status(request.getStatus() != null ? request.getStatus() : "COMPLETED")
                .customer(customer)
                .branch(branch)
                .posSession(posSession)
                .build();

        invoice.setIsDeleted(false);
        invoice.setCreatedBy(username);
        invoice.setNote(request.getNote());

        BigDecimal subTotalSum = BigDecimal.ZERO;
        List<ExportInvoiceDetail> details = new ArrayList<>();

        for (ExportInvoiceDetailRequest detailReq : request.getDetails()) {
            Product product = productRepository.findByIdAndIsDeletedFalse(detailReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", detailReq.getProductId()));

            ProductBatch batch = null;
            if (detailReq.getBatchId() != null) {
                batch = productBatchRepository.findByIdAndIsDeletedFalse(detailReq.getBatchId())
                        .orElseThrow(() -> new ResourceNotFoundException("ProductBatch", "id", detailReq.getBatchId()));
                
                // Deduct batch remaining units
                if (batch.getRemainingUnits() != null && detailReq.getQuantity() != null) {
                    BigDecimal remaining = batch.getRemainingUnits().subtract(detailReq.getQuantity());
                    batch.setRemainingUnits(remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining);
                    productBatchRepository.save(batch);
                }
            }

            BigDecimal discount = detailReq.getDiscount() != null ? detailReq.getDiscount() : BigDecimal.ZERO;
            BigDecimal subTotal = detailReq.getQuantity().multiply(detailReq.getUnitPrice().subtract(discount));
            subTotalSum = subTotalSum.add(subTotal);

            ExportInvoiceDetail detail = ExportInvoiceDetail.builder()
                    .invoice(invoice)
                    .product(product)
                    .batch(batch)
                    .quantity(detailReq.getQuantity())
                    .unitPrice(detailReq.getUnitPrice())
                    .discount(discount)
                    .subTotal(subTotal)
                    .taxRate(getTaxRateForProduct(product))
                    .build();

            detail.setIsDeleted(false);
            detail.setCreatedBy(username);
            details.add(detail);
        }

        BigDecimal masterDiscount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal masterTax = request.getTax() != null ? request.getTax() : BigDecimal.ZERO;
        BigDecimal totalAmount = subTotalSum.subtract(masterDiscount).add(masterTax);

        invoice.setSubTotal(subTotalSum);
        invoice.setDiscount(masterDiscount);
        invoice.setTax(masterTax);
        invoice.setTotalAmount(totalAmount);

        ExportInvoice savedInvoice = exportInvoiceRepository.save(invoice);
        exportInvoiceDetailRepository.saveAll(details);

        return mapToResponse(savedInvoice, details);
    }

    @Override
    public ExportInvoiceResponse updateInvoice(Long id, UpdateExportInvoiceRequest request) {
        ExportInvoice invoice = exportInvoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExportInvoice", "id", id));

        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findByIdAndIsDeletedFalse(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));
        }

        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        PosSession posSession = null;
        if (request.getPosSessionId() != null) {
            posSession = posSessionRepository.findByIdAndIsDeletedFalse(request.getPosSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("PosSession", "id", request.getPosSessionId()));
        }

        String username = getCurrentUsername();

        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setStatus(request.getStatus());
        invoice.setCustomer(customer);
        invoice.setBranch(branch);
        invoice.setPosSession(posSession);
        invoice.setNote(request.getNote());
        invoice.setUpdatedBy(username);

        // Soft delete old details
        List<ExportInvoiceDetail> oldDetails = exportInvoiceDetailRepository.findByInvoiceIdAndIsDeletedFalse(id);
        for (ExportInvoiceDetail detail : oldDetails) {
            detail.setIsDeleted(true);
            detail.setDeletedBy(username);
            detail.setDeletedAt(LocalDateTime.now());
        }
        exportInvoiceDetailRepository.saveAll(oldDetails);

        // Add new details
        BigDecimal subTotalSum = BigDecimal.ZERO;
        List<ExportInvoiceDetail> newDetails = new ArrayList<>();

        for (ExportInvoiceDetailRequest detailReq : request.getDetails()) {
            Product product = productRepository.findByIdAndIsDeletedFalse(detailReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", detailReq.getProductId()));

            ProductBatch batch = null;
            if (detailReq.getBatchId() != null) {
                batch = productBatchRepository.findByIdAndIsDeletedFalse(detailReq.getBatchId())
                        .orElseThrow(() -> new ResourceNotFoundException("ProductBatch", "id", detailReq.getBatchId()));
            }

            BigDecimal discount = detailReq.getDiscount() != null ? detailReq.getDiscount() : BigDecimal.ZERO;
            BigDecimal subTotal = detailReq.getQuantity().multiply(detailReq.getUnitPrice().subtract(discount));
            subTotalSum = subTotalSum.add(subTotal);

            ExportInvoiceDetail detail = ExportInvoiceDetail.builder()
                    .invoice(invoice)
                    .product(product)
                    .batch(batch)
                    .quantity(detailReq.getQuantity())
                    .unitPrice(detailReq.getUnitPrice())
                    .discount(discount)
                    .subTotal(subTotal)
                    .taxRate(getTaxRateForProduct(product))
                    .build();

            detail.setIsDeleted(false);
            detail.setCreatedBy(username);
            newDetails.add(detail);
        }

        BigDecimal masterDiscount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal masterTax = request.getTax() != null ? request.getTax() : BigDecimal.ZERO;
        BigDecimal totalAmount = subTotalSum.subtract(masterDiscount).add(masterTax);

        invoice.setSubTotal(subTotalSum);
        invoice.setDiscount(masterDiscount);
        invoice.setTax(masterTax);
        invoice.setTotalAmount(totalAmount);

        ExportInvoice savedInvoice = exportInvoiceRepository.save(invoice);
        exportInvoiceDetailRepository.saveAll(newDetails);

        return mapToResponse(savedInvoice, newDetails);
    }

    @Override
    public ExportInvoiceResponse updateStatus(Long id, String status) {
        ExportInvoice invoice = exportInvoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExportInvoice", "id", id));

        invoice.setStatus(status);
        invoice.setUpdatedBy(getCurrentUsername());

        ExportInvoice savedInvoice = exportInvoiceRepository.save(invoice);
        List<ExportInvoiceDetail> details = exportInvoiceDetailRepository.findByInvoiceIdAndIsDeletedFalse(id);
        return mapToResponse(savedInvoice, details);
    }

    @Override
    public void deleteInvoice(Long id) {
        ExportInvoice invoice = exportInvoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExportInvoice", "id", id));

        String username = getCurrentUsername();
        invoice.setIsDeleted(true);
        invoice.setDeletedBy(username);
        invoice.setDeletedAt(LocalDateTime.now());
        exportInvoiceRepository.save(invoice);

        List<ExportInvoiceDetail> details = exportInvoiceDetailRepository.findByInvoiceIdAndIsDeletedFalse(id);
        for (ExportInvoiceDetail detail : details) {
            detail.setIsDeleted(true);
            detail.setDeletedBy(username);
            detail.setDeletedAt(LocalDateTime.now());
        }
        exportInvoiceDetailRepository.saveAll(details);
    }

    @Override
    @Transactional(readOnly = true)
    public ExportInvoiceResponse getInvoiceById(Long id) {
        ExportInvoice invoice = exportInvoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExportInvoice", "id", id));

        List<ExportInvoiceDetail> details = exportInvoiceDetailRepository.findByInvoiceIdAndIsDeletedFalse(id);
        return mapToResponse(invoice, details);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExportInvoiceResponse> getAllInvoices(String search, String status, Long branchId, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sorting);
        Page<ExportInvoice> pageResult = exportInvoiceRepository.findAllInvoices(search, status, branchId, includeDeleted, pageable);
        return pageResult.getContent().stream()
                .map(i -> {
                    List<ExportInvoiceDetail> details = exportInvoiceDetailRepository.findByInvoiceIdAndIsDeletedFalse(i.getId());
                    return mapToResponse(i, details);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ExportInvoiceResponse> getInvoicesPaginated(String search, String status, Long branchId, int page, int size, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<ExportInvoice> pageResult = exportInvoiceRepository.findAllInvoices(search, status, branchId, includeDeleted, pageable);

        List<ExportInvoiceResponse> content = pageResult.getContent().stream()
                .map(i -> {
                    List<ExportInvoiceDetail> details = exportInvoiceDetailRepository.findByInvoiceIdAndIsDeletedFalse(i.getId());
                    return mapToResponse(i, details);
                })
                .collect(Collectors.toList());

        return PageResponse.<ExportInvoiceResponse>builder()
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

    private ExportInvoiceResponse mapToResponse(ExportInvoice i, List<ExportInvoiceDetail> details) {
        List<ExportInvoiceDetailResponse> detailsResponse = details.stream()
                .map(d -> ExportInvoiceDetailResponse.builder()
                        .id(d.getId())
                        .productId(d.getProduct().getId())
                        .productCode(d.getProduct().getProductCode())
                        .productName(d.getProduct().getName())
                        .batchId(d.getBatch() != null ? d.getBatch().getId() : null)
                        .batchCode(d.getBatch() != null ? d.getBatch().getBatchNumber() : null)
                        .quantity(d.getQuantity())
                        .unitPrice(d.getUnitPrice())
                        .discount(d.getDiscount())
                        .subTotal(d.getSubTotal())
                        .build())
                .collect(Collectors.toList());

        return ExportInvoiceResponse.builder()
                .id(i.getId())
                .invoiceCode(i.getInvoiceCode())
                .invoiceDate(i.getInvoiceDate())
                .subTotal(i.getSubTotal())
                .discount(i.getDiscount())
                .tax(i.getTax())
                .totalAmount(i.getTotalAmount())
                .status(i.getStatus())
                .customerId(i.getCustomer() != null ? i.getCustomer().getId() : null)
                .customerName(i.getCustomer() != null ? i.getCustomer().getName() : null)
                .branchId(i.getBranch().getId())
                .branchName(i.getBranch().getBranchName())
                .posSessionId(i.getPosSession() != null ? i.getPosSession().getId() : null)
                .note(i.getNote())
                .createdAt(i.getCreatedAt())
                .createdBy(i.getCreatedBy())
                .details(detailsResponse)
                .build();
    }

    private BigDecimal getTaxRateForProduct(Product product) {
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
