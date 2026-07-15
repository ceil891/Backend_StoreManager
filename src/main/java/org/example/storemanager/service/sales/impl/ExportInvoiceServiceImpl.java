package org.example.storemanager.service.sales.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.config.LogActivity;
import org.example.storemanager.dto.request.sales.exportinvoice.CreateExportInvoiceRequest;
import org.example.storemanager.dto.request.sales.exportinvoice.ExportInvoiceDetailRequest;
import org.example.storemanager.dto.request.sales.exportinvoice.UpdateExportInvoiceRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.sales.exportinvoice.DeleteExportInvoiceResponse;
import org.example.storemanager.dto.response.sales.exportinvoice.ExportInvoiceDetailResponse;
import org.example.storemanager.dto.response.sales.exportinvoice.ExportInvoiceResponse;
import org.example.storemanager.entity.catalog.Product;
import org.example.storemanager.entity.partnerarea.Customer;
import org.example.storemanager.entity.sales.ExportInvoice;
import org.example.storemanager.entity.sales.ExportInvoiceDetail;
import org.example.storemanager.entity.system.Branch;
import org.example.storemanager.enums.sales.OrderStatus;
import org.example.storemanager.exception.DuplicateResourceException;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.catalog.ProductRepository;
import org.example.storemanager.repository.partnerarea.CustomerRepository;
import org.example.storemanager.repository.sales.ExportInvoiceDetailRepository;
import org.example.storemanager.repository.sales.ExportInvoiceRepository;
import org.example.storemanager.repository.system.BranchRepository;
import org.example.storemanager.service.sales.ExportInvoiceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportInvoiceServiceImpl implements ExportInvoiceService {

    private final ExportInvoiceRepository exportInvoiceRepository;
    private final ExportInvoiceDetailRepository detailRepository;
    private final BranchRepository branchRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    @LogActivity(actionType = "CREATE", entityName = "ExportInvoice", entityClass = ExportInvoice.class)
    public ExportInvoiceResponse create(CreateExportInvoiceRequest request) {
        if (exportInvoiceRepository.existsByInvoiceCode(request.getInvoiceCode())) {
            throw new DuplicateResourceException("ExportInvoice", "invoiceCode", request.getInvoiceCode());
        }

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        ExportInvoice invoice = new ExportInvoice();
        invoice.setInvoiceCode(request.getInvoiceCode());
        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setSubTotal(request.getSubTotal());
        invoice.setDiscount(request.getDiscount());
        invoice.setTax(request.getTax());
        invoice.setTotalAmount(request.getTotalAmount());


        invoice.setStatus(request.getStatus() != null ? request.getStatus() : OrderStatus.DRAFT);
        invoice.setIsActive(true);
        invoice.setBranch(branch);

        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId()).orElse(null);
            invoice.setCustomer(customer);
        }

        ExportInvoice savedInvoice = exportInvoiceRepository.save(invoice);

        List<ExportInvoiceDetail> details = new ArrayList<>();
        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            for (ExportInvoiceDetailRequest detailReq : request.getDetails()) {
                Product product = productRepository.findById(detailReq.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", detailReq.getProductId()));

                ExportInvoiceDetail detail = new ExportInvoiceDetail();
                detail.setInvoice(savedInvoice);
                detail.setProduct(product);
                detail.setQuantity(detailReq.getQuantity());
                detail.setUnitPrice(detailReq.getUnitPrice());
                detail.setDiscount(detailReq.getDiscount());
                detail.setSubTotal(detailReq.getSubTotal());
                details.add(detail);
            }
            detailRepository.saveAll(details);
        }

        return getById(savedInvoice.getId());
    }

    @Override
    @Transactional
    @LogActivity(actionType = "UPDATE", entityName = "ExportInvoice", entityClass = ExportInvoice.class)
    public ExportInvoiceResponse update(Long id, UpdateExportInvoiceRequest request) {
        ExportInvoice invoice = exportInvoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExportInvoice", "id", id));

        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setDiscount(request.getDiscount());
        invoice.setTax(request.getTax());

        exportInvoiceRepository.save(invoice);
        return getById(id);
    }

    @Override
    @Transactional
    @LogActivity(actionType = "DELETE", entityName = "ExportInvoice", entityClass = ExportInvoice.class)
    public DeleteExportInvoiceResponse delete(Long id) {
        ExportInvoice invoice = exportInvoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExportInvoice", "id", id));

        if (Boolean.TRUE.equals(invoice.getIsActive())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Không thể xóa hóa đơn '" + invoice.getInvoiceCode() + "' vì hóa đơn này vẫn đang hoạt động. Vui lòng tắt hoạt động trước, sau đó mới có thể xóa."
            );
        }

        String username = getCurrentUsername();

        invoice.setIsDeleted(true);
        invoice.setIsActive(false);
        invoice.setDeletedAt(java.time.LocalDateTime.now());
        invoice.setDeletedBy(username);
        invoice.setUpdatedBy(username);

        ExportInvoice deleted = exportInvoiceRepository.save(invoice);

        return DeleteExportInvoiceResponse.builder()
                .id(deleted.getId())
                .invoiceCode(deleted.getInvoiceCode())
                .isDeleted(deleted.getIsDeleted())
                .deletedAt(deleted.getDeletedAt())
                .deletedBy(deleted.getDeletedBy())
                .build();
    }

    @Override
    @Transactional
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "ExportInvoice", entityClass = ExportInvoice.class)
    public ExportInvoiceResponse updateStatus(Long id, Boolean isActive) {
        ExportInvoice invoice = exportInvoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExportInvoice", "id", id));

        invoice.setIsActive(isActive);
        invoice.setUpdatedBy(getCurrentUsername());

        exportInvoiceRepository.save(invoice);
        return getById(id);
    }

    // =========== CẬP NHẬT TRẠNG THÁI NGHIỆP VỤ (ORDER STATUS) ===========
    @Override
    @Transactional
    @LogActivity(actionType = "UPDATE_ORDER_STATUS", entityName = "ExportInvoice", entityClass = ExportInvoice.class)
    public ExportInvoiceResponse updateOrderStatus(Long id, OrderStatus status) {
        ExportInvoice invoice = exportInvoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExportInvoice", "id", id));

        // Ràng buộc logic: Hóa đơn đã tắt hoạt động thì không cho phép đổi trạng thái bán hàng nữa
        if (Boolean.FALSE.equals(invoice.getIsActive())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Không thể thay đổi trạng thái thanh toán vì hóa đơn này đã bị tắt hoạt động.");
        }

        invoice.setStatus(status);
        invoice.setUpdatedBy(getCurrentUsername());

        exportInvoiceRepository.save(invoice);
        return getById(id);
    }
    @Override
    public PageResponse<ExportInvoiceResponse> getAllPaginated(String keyword, Long branchId, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ExportInvoice> invoicePage = exportInvoiceRepository.searchInvoices(keyword, branchId, pageable);

        List<ExportInvoiceResponse> content = invoicePage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                invoicePage.getNumber(),
                invoicePage.getSize(),
                invoicePage.getTotalElements(),
                invoicePage.getTotalPages(),
                invoicePage.isLast()
        );
    }

    @Override
    public List<ExportInvoiceResponse> getActiveList(String status) {
        // ĐÃ SỬA: Convert chuỗi từ URL sang Enum OrderStatus để truy vấn
        OrderStatus enumStatus;
        try {
            enumStatus = OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Trạng thái không hợp lệ: " + status);
        }

        List<ExportInvoice> invoices = exportInvoiceRepository.findByStatusAndIsDeletedFalse(enumStatus);
        return invoices.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public ExportInvoiceResponse getById(Long id) {
        ExportInvoice invoice = exportInvoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExportInvoice", "id", id));

        ExportInvoiceResponse response = mapToResponse(invoice);

        List<ExportInvoiceDetail> details = detailRepository.findByInvoiceId(id);
        List<ExportInvoiceDetailResponse> detailResponses = details.stream().map(d -> {
            ExportInvoiceDetailResponse dr = new ExportInvoiceDetailResponse();
            dr.setId(d.getId());
            dr.setProductId(d.getProduct().getId());
            dr.setProductName(d.getProduct().getName());
            dr.setQuantity(d.getQuantity());
            dr.setUnitPrice(d.getUnitPrice());
            dr.setDiscount(d.getDiscount());
            dr.setSubTotal(d.getSubTotal());
            return dr;
        }).collect(Collectors.toList());

        response.setDetails(detailResponses);
        return response;
    }

    private ExportInvoiceResponse mapToResponse(ExportInvoice invoice) {
        ExportInvoiceResponse response = new ExportInvoiceResponse();
        response.setId(invoice.getId());
        response.setInvoiceCode(invoice.getInvoiceCode());
        response.setInvoiceDate(invoice.getInvoiceDate());
        response.setSubTotal(invoice.getSubTotal());
        response.setDiscount(invoice.getDiscount());
        response.setTax(invoice.getTax());
        response.setTotalAmount(invoice.getTotalAmount());

        // ĐÃ SỬA: Trả về trực tiếp Enum
        response.setStatus(invoice.getStatus());

        response.setBranchId(invoice.getBranch().getId());
        response.setIsActive(invoice.getIsActive());

        if (invoice.getCustomer() != null) {
            response.setCustomerId(invoice.getCustomer().getId());
        }
        if (invoice.getPosSession() != null) {
            response.setPosSessionId(invoice.getPosSession().getId());
        }
        return response;
    }

    private String getCurrentUsername() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "Người dùng chưa đăng nhập hoặc token không hợp lệ");
    }
}