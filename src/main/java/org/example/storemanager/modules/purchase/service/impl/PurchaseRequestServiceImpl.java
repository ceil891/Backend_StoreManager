package org.example.storemanager.modules.purchase.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.purchase.dto.request.CreatePurchaseRequest;
import org.example.storemanager.modules.purchase.dto.request.UpdatePurchaseRequest;
import org.example.storemanager.modules.purchase.dto.request.PurchaseRequestDetailRequest;
import org.example.storemanager.modules.purchase.dto.response.PurchaseRequestDetailResponse;
import org.example.storemanager.modules.purchase.dto.response.PurchaseRequestResponse;
import org.example.storemanager.modules.purchase.dto.response.PurchaseOrderResponse;
import org.example.storemanager.modules.purchase.dto.response.PurchaseOrderDetailResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.wms.entity.PurchaseRequest;
import org.example.storemanager.modules.wms.entity.PurchaseRequestDetail;
import org.example.storemanager.modules.sales.entity.PurchaseOrder;
import org.example.storemanager.modules.sales.entity.PurchaseOrderDetail;
import org.example.storemanager.modules.partnerarea.entity.Supplier;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.wms.repository.PurchaseRequestRepository;
import org.example.storemanager.modules.wms.repository.PurchaseRequestDetailRepository;
import org.example.storemanager.modules.sales.repository.PurchaseOrderRepository;
import org.example.storemanager.modules.sales.repository.PurchaseOrderDetailRepository;
import org.example.storemanager.modules.system.repository.BranchRepository;
import org.example.storemanager.modules.catalog.repository.ProductRepository;
import org.example.storemanager.modules.partnerarea.repository.SupplierRepository;
import org.example.storemanager.modules.purchase.service.PurchaseRequestService;
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
public class PurchaseRequestServiceImpl implements PurchaseRequestService {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final PurchaseRequestDetailRepository purchaseRequestDetailRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderDetailRepository purchaseOrderDetailRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    @Override
    public PurchaseRequestResponse createRequest(CreatePurchaseRequest request) {
        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        String username = getCurrentUsername();

        PurchaseRequest pr = PurchaseRequest.builder()
                .requestCode(request.getRequestCode())
                .requestDate(request.getRequestDate())
                .reason(request.getReason())
                .status(request.getStatus())
                .branch(branch)
                .build();

        pr.setIsDeleted(false);
        pr.setCreatedBy(username);
        pr.setNote(request.getNote());

        PurchaseRequest savedPr = purchaseRequestRepository.save(pr);
        List<PurchaseRequestDetail> details = new ArrayList<>();

        for (PurchaseRequestDetailRequest detailReq : request.getDetails()) {
            Product product = productRepository.findByIdAndIsDeletedFalse(detailReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", detailReq.getProductId()));

            PurchaseRequestDetail detail = PurchaseRequestDetail.builder()
                    .purchaseRequest(savedPr)
                    .product(product)
                    .quantity(detailReq.getQuantity())
                    .estimatedPrice(detailReq.getEstimatedPrice())
                    .build();

            detail.setIsDeleted(false);
            detail.setCreatedBy(username);
            details.add(detail);
        }

        purchaseRequestDetailRepository.saveAll(details);

        return mapToResponse(savedPr, details);
    }

    @Override
    public PurchaseRequestResponse updateRequest(Long id, UpdatePurchaseRequest request) {
        PurchaseRequest pr = purchaseRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequest", "id", id));

        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        String username = getCurrentUsername();

        pr.setRequestDate(request.getRequestDate());
        pr.setReason(request.getReason());
        pr.setStatus(request.getStatus());
        pr.setBranch(branch);
        pr.setNote(request.getNote());
        pr.setUpdatedBy(username);

        // Soft delete old details
        List<PurchaseRequestDetail> oldDetails = purchaseRequestDetailRepository.findByPurchaseRequestIdAndIsDeletedFalse(id);
        for (PurchaseRequestDetail detail : oldDetails) {
            detail.setIsDeleted(true);
            detail.setDeletedBy(username);
            detail.setDeletedAt(LocalDateTime.now());
        }
        purchaseRequestDetailRepository.saveAll(oldDetails);

        // Add new details
        List<PurchaseRequestDetail> newDetails = new ArrayList<>();
        for (PurchaseRequestDetailRequest detailReq : request.getDetails()) {
            Product product = productRepository.findByIdAndIsDeletedFalse(detailReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", detailReq.getProductId()));

            PurchaseRequestDetail detail = PurchaseRequestDetail.builder()
                    .purchaseRequest(pr)
                    .product(product)
                    .quantity(detailReq.getQuantity())
                    .estimatedPrice(detailReq.getEstimatedPrice())
                    .build();

            detail.setIsDeleted(false);
            detail.setCreatedBy(username);
            newDetails.add(detail);
        }

        PurchaseRequest savedPr = purchaseRequestRepository.save(pr);
        purchaseRequestDetailRepository.saveAll(newDetails);

        return mapToResponse(savedPr, newDetails);
    }

    @Override
    public PurchaseRequestResponse updateStatus(Long id, String status) {
        PurchaseRequest pr = purchaseRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequest", "id", id));

        pr.setStatus(status);
        pr.setUpdatedBy(getCurrentUsername());

        PurchaseRequest savedPr = purchaseRequestRepository.save(pr);
        List<PurchaseRequestDetail> details = purchaseRequestDetailRepository.findByPurchaseRequestIdAndIsDeletedFalse(id);
        return mapToResponse(savedPr, details);
    }

    @Override
    public void deleteRequest(Long id) {
        PurchaseRequest pr = purchaseRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequest", "id", id));

        String username = getCurrentUsername();
        pr.setIsDeleted(true);
        pr.setDeletedBy(username);
        pr.setDeletedAt(LocalDateTime.now());
        purchaseRequestRepository.save(pr);

        List<PurchaseRequestDetail> details = purchaseRequestDetailRepository.findByPurchaseRequestIdAndIsDeletedFalse(id);
        for (PurchaseRequestDetail detail : details) {
            detail.setIsDeleted(true);
            detail.setDeletedBy(username);
            detail.setDeletedAt(LocalDateTime.now());
        }
        purchaseRequestDetailRepository.saveAll(details);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseRequestResponse getRequestById(Long id) {
        PurchaseRequest pr = purchaseRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequest", "id", id));

        List<PurchaseRequestDetail> details = purchaseRequestDetailRepository.findByPurchaseRequestIdAndIsDeletedFalse(id);
        return mapToResponse(pr, details);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseRequestResponse> getAllRequests(String search, String status, Long branchId, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sorting);
        Page<PurchaseRequest> pageResult = purchaseRequestRepository.findAllRequests(search, status, branchId, includeDeleted, pageable);
        return pageResult.getContent().stream()
                .map(pr -> {
                    List<PurchaseRequestDetail> details = purchaseRequestDetailRepository.findByPurchaseRequestIdAndIsDeletedFalse(pr.getId());
                    return mapToResponse(pr, details);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PurchaseRequestResponse> getRequestsPaginated(String search, String status, Long branchId, int page, int size, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<PurchaseRequest> pageResult = purchaseRequestRepository.findAllRequests(search, status, branchId, includeDeleted, pageable);

        List<PurchaseRequestResponse> content = pageResult.getContent().stream()
                .map(pr -> {
                    List<PurchaseRequestDetail> details = purchaseRequestDetailRepository.findByPurchaseRequestIdAndIsDeletedFalse(pr.getId());
                    return mapToResponse(pr, details);
                })
                .collect(Collectors.toList());

        return PageResponse.<PurchaseRequestResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    @Override
    public PurchaseRequestResponse submitRequest(Long id) {
        return updateStatus(id, "SUBMITTED");
    }

    @Override
    public PurchaseRequestResponse approveRequest(Long id) {
        return updateStatus(id, "APPROVED");
    }

    @Override
    public PurchaseRequestResponse rejectRequest(Long id) {
        return updateStatus(id, "REJECTED");
    }

    @Override
    public PurchaseRequestResponse cancelRequest(Long id) {
        return updateStatus(id, "CANCELLED");
    }

    @Override
    public PurchaseOrderResponse convertToOrder(Long id, Long supplierId) {
        PurchaseRequest pr = purchaseRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequest", "id", id));

        if (!"APPROVED".equals(pr.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yêu cầu mua hàng phải được APPROVED trước khi chuyển đổi");
        }

        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", supplierId));

        String username = getCurrentUsername();
        String poCode = "PO-PR-" + pr.getRequestCode() + "-" + System.currentTimeMillis() % 1000;

        PurchaseOrder po = PurchaseOrder.builder()
                .poCode(poCode)
                .poDate(LocalDateTime.now())
                .expectedDate(LocalDateTime.now().plusDays(7)) // Mặc định giao sau 7 ngày
                .status("DRAFT")
                .supplier(supplier)
                .branch(pr.getBranch())
                .purchaseRequest(pr)
                .build();

        po.setIsDeleted(false);
        po.setCreatedBy(username);
        po.setNote("Chuyển đổi từ yêu cầu mua hàng " + pr.getRequestCode());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<PurchaseOrderDetail> poDetails = new ArrayList<>();
        List<PurchaseRequestDetail> prDetails = purchaseRequestDetailRepository.findByPurchaseRequestIdAndIsDeletedFalse(id);

        for (PurchaseRequestDetail prDetail : prDetails) {
            BigDecimal estPrice = prDetail.getEstimatedPrice() != null ? prDetail.getEstimatedPrice() : BigDecimal.ZERO;
            BigDecimal subTotal = prDetail.getQuantity().multiply(estPrice);
            totalAmount = totalAmount.add(subTotal);

            PurchaseOrderDetail poDetail = PurchaseOrderDetail.builder()
                    .purchaseOrder(po)
                    .product(prDetail.getProduct())
                    .quantity(prDetail.getQuantity())
                    .unitPrice(estPrice)
                    .subTotal(subTotal)
                    .build();

            poDetail.setIsDeleted(false);
            poDetail.setCreatedBy(username);
            poDetails.add(poDetail);
        }

        po.setTotalAmount(totalAmount);
        PurchaseOrder savedPo = purchaseOrderRepository.save(po);
        purchaseOrderDetailRepository.saveAll(poDetails);

        // Cập nhật trạng thái Yêu cầu mua hàng thành CONVERTED
        pr.setStatus("CONVERTED");
        purchaseRequestRepository.save(pr);

        return mapToOrderResponse(savedPo, poDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getOrders(Long id) {
        List<PurchaseOrder> orders = purchaseOrderRepository.findByPurchaseRequestIdAndIsDeletedFalse(id);
        return orders.stream()
                .map(o -> {
                    List<PurchaseOrderDetail> details = purchaseOrderDetailRepository.findByPurchaseOrderIdAndIsDeletedFalse(o.getId());
                    return mapToOrderResponse(o, details);
                })
                .collect(Collectors.toList());
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

    private PurchaseRequestResponse mapToResponse(PurchaseRequest pr, List<PurchaseRequestDetail> details) {
        List<PurchaseRequestDetailResponse> detailsResponse = details.stream()
                .map(d -> PurchaseRequestDetailResponse.builder()
                        .id(d.getId())
                        .productId(d.getProduct().getId())
                        .productCode(d.getProduct().getProductCode())
                        .productName(d.getProduct().getName())
                        .quantity(d.getQuantity())
                        .estimatedPrice(d.getEstimatedPrice())
                        .build())
                .collect(Collectors.toList());

        return PurchaseRequestResponse.builder()
                .id(pr.getId())
                .requestCode(pr.getRequestCode())
                .requestDate(pr.getRequestDate())
                .reason(pr.getReason())
                .status(pr.getStatus())
                .branchId(pr.getBranch().getId())
                .branchName(pr.getBranch().getBranchName())
                .note(pr.getNote())
                .createdAt(pr.getCreatedAt())
                .createdBy(pr.getCreatedBy())
                .details(detailsResponse)
                .build();
    }

    private PurchaseOrderResponse mapToOrderResponse(PurchaseOrder po, List<PurchaseOrderDetail> details) {
        List<PurchaseOrderDetailResponse> detailsResponse = details.stream()
                .map(d -> PurchaseOrderDetailResponse.builder()
                        .id(d.getId())
                        .productId(d.getProduct().getId())
                        .productCode(d.getProduct().getProductCode())
                        .productName(d.getProduct().getName())
                        .quantity(d.getQuantity())
                        .unitPrice(d.getUnitPrice())
                        .subTotal(d.getSubTotal())
                        .build())
                .collect(Collectors.toList());

        return PurchaseOrderResponse.builder()
                .id(po.getId())
                .poCode(po.getPoCode())
                .poDate(po.getPoDate())
                .expectedDate(po.getExpectedDate())
                .totalAmount(po.getTotalAmount())
                .status(po.getStatus())
                .supplierId(po.getSupplier().getId())
                .supplierName(po.getSupplier().getName())
                .branchId(po.getBranch().getId())
                .branchName(po.getBranch().getBranchName())
                .note(po.getNote())
                .createdAt(po.getCreatedAt())
                .createdBy(po.getCreatedBy())
                .details(detailsResponse)
                .build();
    }
}
