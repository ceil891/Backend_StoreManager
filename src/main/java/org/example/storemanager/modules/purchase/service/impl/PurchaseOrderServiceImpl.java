package org.example.storemanager.modules.purchase.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.purchase.dto.request.CreatePurchaseOrderRequest;
import org.example.storemanager.modules.purchase.dto.request.UpdatePurchaseOrderRequest;
import org.example.storemanager.modules.purchase.dto.request.CalculatePurchaseOrderRequest;
import org.example.storemanager.modules.purchase.dto.request.PurchaseOrderDetailRequest;
import org.example.storemanager.modules.purchase.dto.response.CalculatePurchaseOrderResponse;
import org.example.storemanager.modules.purchase.dto.response.PurchaseOrderDetailResponse;
import org.example.storemanager.modules.purchase.dto.response.PurchaseOrderResponse;
import org.example.storemanager.modules.inventory.dto.ImportReceiptDTO;
import org.example.storemanager.modules.inventory.dto.ImportReceiptDetailDTO;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.sales.entity.PurchaseOrder;
import org.example.storemanager.modules.sales.entity.PurchaseOrderDetail;
import org.example.storemanager.modules.inventory.entity.ImportReceipt;
import org.example.storemanager.modules.inventory.entity.ImportReceiptDetail;
import org.example.storemanager.modules.partnerarea.entity.Supplier;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.catalog.entity.ProductVariant;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.sales.repository.PurchaseOrderRepository;
import org.example.storemanager.modules.sales.repository.PurchaseOrderDetailRepository;
import org.example.storemanager.modules.inventory.repository.ImportReceiptRepository;
import org.example.storemanager.modules.inventory.repository.ImportReceiptDetailRepository;
import org.example.storemanager.modules.partnerarea.repository.SupplierRepository;
import org.example.storemanager.modules.system.repository.BranchRepository;
import org.example.storemanager.modules.catalog.repository.ProductRepository;
import org.example.storemanager.modules.catalog.repository.ProductVariantRepository;
import org.example.storemanager.modules.purchase.service.PurchaseOrderService;
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
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderDetailRepository purchaseOrderDetailRepository;
    private final ImportReceiptRepository importReceiptRepository;
    private final ImportReceiptDetailRepository importReceiptDetailRepository;
    private final SupplierRepository supplierRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public PurchaseOrderResponse createOrder(CreatePurchaseOrderRequest request) {
        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));

        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        String username = getCurrentUsername();

        PurchaseOrder po = PurchaseOrder.builder()
                .poCode(request.getPoCode())
                .poDate(request.getPoDate())
                .expectedDate(request.getExpectedDate())
                .status(request.getStatus())
                .supplier(supplier)
                .branch(branch)
                .build();

        po.setIsDeleted(false);
        po.setCreatedBy(username);
        po.setNote(request.getNote());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<PurchaseOrderDetail> details = new ArrayList<>();

        for (PurchaseOrderDetailRequest detailReq : request.getDetails()) {
            Product product = productRepository.findByIdAndIsDeletedFalse(detailReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", detailReq.getProductId()));

            BigDecimal subTotal = detailReq.getQuantity().multiply(detailReq.getUnitPrice());
            totalAmount = totalAmount.add(subTotal);

            PurchaseOrderDetail detail = PurchaseOrderDetail.builder()
                    .purchaseOrder(po)
                    .product(product)
                    .quantity(detailReq.getQuantity())
                    .unitPrice(detailReq.getUnitPrice())
                    .subTotal(subTotal)
                    .build();

            detail.setIsDeleted(false);
            detail.setCreatedBy(username);
            details.add(detail);
        }

        po.setTotalAmount(totalAmount);
        PurchaseOrder savedPo = purchaseOrderRepository.save(po);
        purchaseOrderDetailRepository.saveAll(details);

        return mapToResponse(savedPo, details);
    }

    @Override
    public PurchaseOrderResponse updateOrder(Long id, UpdatePurchaseOrderRequest request) {
        PurchaseOrder po = purchaseOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));

        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));

        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        String username = getCurrentUsername();

        po.setPoDate(request.getPoDate());
        po.setExpectedDate(request.getExpectedDate());
        po.setStatus(request.getStatus());
        po.setSupplier(supplier);
        po.setBranch(branch);
        po.setNote(request.getNote());
        po.setUpdatedBy(username);

        // Soft delete old details
        List<PurchaseOrderDetail> oldDetails = purchaseOrderDetailRepository.findByPurchaseOrderIdAndIsDeletedFalse(id);
        for (PurchaseOrderDetail detail : oldDetails) {
            detail.setIsDeleted(true);
            detail.setDeletedBy(username);
            detail.setDeletedAt(LocalDateTime.now());
        }
        purchaseOrderDetailRepository.saveAll(oldDetails);

        // Add new details
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<PurchaseOrderDetail> newDetails = new ArrayList<>();

        for (PurchaseOrderDetailRequest detailReq : request.getDetails()) {
            Product product = productRepository.findByIdAndIsDeletedFalse(detailReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", detailReq.getProductId()));

            BigDecimal subTotal = detailReq.getQuantity().multiply(detailReq.getUnitPrice());
            totalAmount = totalAmount.add(subTotal);

            PurchaseOrderDetail detail = PurchaseOrderDetail.builder()
                    .purchaseOrder(po)
                    .product(product)
                    .quantity(detailReq.getQuantity())
                    .unitPrice(detailReq.getUnitPrice())
                    .subTotal(subTotal)
                    .build();

            detail.setIsDeleted(false);
            detail.setCreatedBy(username);
            newDetails.add(detail);
        }

        po.setTotalAmount(totalAmount);
        PurchaseOrder savedPo = purchaseOrderRepository.save(po);
        purchaseOrderDetailRepository.saveAll(newDetails);

        return mapToResponse(savedPo, newDetails);
    }

    @Override
    public PurchaseOrderResponse updateStatus(Long id, String status) {
        PurchaseOrder po = purchaseOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));

        po.setStatus(status);
        po.setUpdatedBy(getCurrentUsername());

        PurchaseOrder savedPo = purchaseOrderRepository.save(po);
        List<PurchaseOrderDetail> details = purchaseOrderDetailRepository.findByPurchaseOrderIdAndIsDeletedFalse(id);
        return mapToResponse(savedPo, details);
    }

    @Override
    public void deleteOrder(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));

        String username = getCurrentUsername();
        po.setIsDeleted(true);
        po.setDeletedBy(username);
        po.setDeletedAt(LocalDateTime.now());
        purchaseOrderRepository.save(po);

        List<PurchaseOrderDetail> details = purchaseOrderDetailRepository.findByPurchaseOrderIdAndIsDeletedFalse(id);
        for (PurchaseOrderDetail detail : details) {
            detail.setIsDeleted(true);
            detail.setDeletedBy(username);
            detail.setDeletedAt(LocalDateTime.now());
        }
        purchaseOrderDetailRepository.saveAll(details);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderResponse getOrderById(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));

        List<PurchaseOrderDetail> details = purchaseOrderDetailRepository.findByPurchaseOrderIdAndIsDeletedFalse(id);
        return mapToResponse(po, details);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getAllOrders(String search, String status, Long branchId, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sorting);
        Page<PurchaseOrder> pageResult = purchaseOrderRepository.findAllOrders(search, status, branchId, includeDeleted, pageable);
        return pageResult.getContent().stream()
                .map(po -> {
                    List<PurchaseOrderDetail> details = purchaseOrderDetailRepository.findByPurchaseOrderIdAndIsDeletedFalse(po.getId());
                    return mapToResponse(po, details);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PurchaseOrderResponse> getOrdersPaginated(String search, String status, Long branchId, int page, int size, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<PurchaseOrder> pageResult = purchaseOrderRepository.findAllOrders(search, status, branchId, includeDeleted, pageable);

        List<PurchaseOrderResponse> content = pageResult.getContent().stream()
                .map(po -> {
                    List<PurchaseOrderDetail> details = purchaseOrderDetailRepository.findByPurchaseOrderIdAndIsDeletedFalse(po.getId());
                    return mapToResponse(po, details);
                })
                .collect(Collectors.toList());

        return PageResponse.<PurchaseOrderResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    @Override
    public PurchaseOrderResponse submitOrder(Long id) {
        return updateStatus(id, "PENDING_APPROVAL");
    }

    @Override
    public PurchaseOrderResponse approveOrder(Long id) {
        return updateStatus(id, "APPROVED");
    }

    @Override
    public PurchaseOrderResponse rejectOrder(Long id) {
        return updateStatus(id, "REJECTED");
    }

    @Override
    public PurchaseOrderResponse sendToSupplier(Long id) {
        return updateStatus(id, "SENT_TO_SUPPLIER");
    }

    @Override
    public PurchaseOrderResponse confirmOrder(Long id) {
        return updateStatus(id, "CONFIRMED");
    }

    @Override
    public PurchaseOrderResponse cancelOrder(Long id) {
        return updateStatus(id, "CANCELLED");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImportReceiptDTO> getReceipts(Long id) {
        List<ImportReceipt> receipts = importReceiptRepository.findByPurchaseOrderIdAndIsDeletedFalse(id);
        return receipts.stream()
                .map(r -> {
                    List<ImportReceiptDetail> details = importReceiptDetailRepository.findByReceiptIdAndIsDeletedFalse(r.getId());
                    return mapToImportReceiptResponse(r, details);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ImportReceiptDTO createReceipt(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));

        if (!"APPROVED".equals(po.getStatus()) && !"CONFIRMED".equals(po.getStatus()) && !"SENT_TO_SUPPLIER".equals(po.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ tạo được phiếu nhập cho đơn mua hàng ở trạng thái APPROVED, SENT_TO_SUPPLIER hoặc CONFIRMED");
        }

        String username = getCurrentUsername();
        String receiptCode = "IR-" + po.getPoCode() + "-" + System.currentTimeMillis() % 1000;

        ImportReceipt receipt = ImportReceipt.builder()
                .receiptCode(receiptCode)
                .receiptDate(LocalDateTime.now())
                .totalAmount(po.getTotalAmount())
                .discount(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .status("PENDING")
                .branch(po.getBranch())
                .supplier(po.getSupplier())
                .purchaseOrder(po)
                .build();

        receipt.setIsDeleted(false);
        receipt.setCreatedBy(username);
        ImportReceipt savedReceipt = importReceiptRepository.save(receipt);

        List<PurchaseOrderDetail> poDetails = purchaseOrderDetailRepository.findByPurchaseOrderIdAndIsDeletedFalse(id);
        List<ImportReceiptDetail> receiptDetails = new ArrayList<>();

        for (PurchaseOrderDetail poDetail : poDetails) {
            Product product = poDetail.getProduct();
            ProductVariant variant = productVariantRepository.findByProductIdAndIsDeletedFalse(product.getId())
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                            "Sản phẩm " + product.getName() + " chưa có phiên bản (variant) nào để nhập kho."));

            ImportReceiptDetail receiptDetail = ImportReceiptDetail.builder()
                    .receipt(savedReceipt)
                    .productVariant(variant)
                    .productNameSnapshot(product.getName())
                    .skuSnapshot(variant.getSku())
                    .barcodeSnapshot(variant.getBarcode())
                    .unitCostSnapshot(poDetail.getUnitPrice())
                    .quantity(poDetail.getQuantity())
                    .subTotal(poDetail.getSubTotal())
                    .build();

            receiptDetail.setIsDeleted(false);
            receiptDetail.setCreatedBy(username);
            receiptDetails.add(receiptDetail);
        }

        importReceiptDetailRepository.saveAll(receiptDetails);

        // Cập nhật trạng thái đơn mua hàng sang RECEIVED
        po.setStatus("RECEIVED");
        purchaseOrderRepository.save(po);

        return mapToImportReceiptResponse(savedReceipt, receiptDetails);
    }

    @Override
    public PurchaseOrderResponse duplicateOrder(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));

        List<PurchaseOrderDetail> poDetails = purchaseOrderDetailRepository.findByPurchaseOrderIdAndIsDeletedFalse(id);

        String username = getCurrentUsername();
        String newCode = po.getPoCode() + "-DUP-" + System.currentTimeMillis() % 1000;

        PurchaseOrder newPo = PurchaseOrder.builder()
                .poCode(newCode)
                .poDate(LocalDateTime.now())
                .expectedDate(po.getExpectedDate())
                .status("DRAFT")
                .supplier(po.getSupplier())
                .branch(po.getBranch())
                .totalAmount(po.getTotalAmount())
                .build();

        newPo.setIsDeleted(false);
        newPo.setCreatedBy(username);
        newPo.setNote("Bản sao của đơn mua " + po.getPoCode() + ". " + (po.getNote() != null ? po.getNote() : ""));
        PurchaseOrder savedNewPo = purchaseOrderRepository.save(newPo);

        List<PurchaseOrderDetail> newDetails = poDetails.stream()
                .map(d -> {
                    PurchaseOrderDetail nd = PurchaseOrderDetail.builder()
                            .purchaseOrder(savedNewPo)
                            .product(d.getProduct())
                            .quantity(d.getQuantity())
                            .unitPrice(d.getUnitPrice())
                            .subTotal(d.getSubTotal())
                            .build();
                    nd.setIsDeleted(false);
                    nd.setCreatedBy(username);
                    return nd;
                })
                .collect(Collectors.toList());

        purchaseOrderDetailRepository.saveAll(newDetails);

        return mapToResponse(savedNewPo, newDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public CalculatePurchaseOrderResponse calculateOrder(CalculatePurchaseOrderRequest request) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;

        if (request.getItems() != null) {
            for (CalculatePurchaseOrderRequest.CalculateItem item : request.getItems()) {
                BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
                BigDecimal price = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal discountPercent = item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO;

                BigDecimal lineTotal = qty.multiply(price);
                subtotal = subtotal.add(lineTotal);

                BigDecimal lineDiscount = lineTotal.multiply(discountPercent).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
                totalDiscountAmount = totalDiscountAmount.add(lineDiscount);
            }
        }

        BigDecimal taxableAmount = subtotal.subtract(totalDiscountAmount);
        BigDecimal taxPercent = request.getTaxRate() != null ? request.getTaxRate() : BigDecimal.ZERO;
        BigDecimal taxAmount = taxableAmount.multiply(taxPercent).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);

        BigDecimal shipping = request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO;
        BigDecimal totalAmount = taxableAmount.add(taxAmount).add(shipping);

        return CalculatePurchaseOrderResponse.builder()
                .subtotal(subtotal)
                .discountAmount(totalDiscountAmount)
                .taxAmount(taxAmount)
                .shippingFee(shipping)
                .totalAmount(totalAmount)
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

    private PurchaseOrderResponse mapToResponse(PurchaseOrder po, List<PurchaseOrderDetail> details) {
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

    private ImportReceiptDTO mapToImportReceiptResponse(ImportReceipt r, List<ImportReceiptDetail> details) {
        List<ImportReceiptDetailDTO> lines = details.stream()
                .map(d -> ImportReceiptDetailDTO.builder()
                        .id(d.getId())
                        .productVariantId(d.getProductVariant().getId())
                        .productName(d.getProductNameSnapshot())
                        .sku(d.getSkuSnapshot())
                        .barcode(d.getBarcodeSnapshot())
                        .quantity(d.getQuantity())
                        .unitCost(d.getUnitCostSnapshot())
                        .subTotal(d.getSubTotal())
                        .batchNumber(d.getBatchNumber())
                        .expiryDate(d.getExpiryDate())
                        .targetBinId(d.getTargetBin() != null ? d.getTargetBin().getId() : null)
                        .targetBinCode(d.getTargetBin() != null ? d.getTargetBin().getBinCode() : null)
                        .build())
                .collect(Collectors.toList());

        return ImportReceiptDTO.builder()
                .id(r.getId())
                .receiptCode(r.getReceiptCode())
                .receiptDate(r.getReceiptDate())
                .totalAmount(r.getTotalAmount())
                .discount(r.getDiscount())
                .tax(r.getTax())
                .status(r.getStatus())
                .branchId(r.getBranch().getId())
                .branchName(r.getBranch().getBranchName())
                .supplierId(r.getSupplier().getId())
                .supplierName(r.getSupplier().getName())
                .purchaseOrderId(r.getPurchaseOrder() != null ? r.getPurchaseOrder().getId() : null)
                .purchaseOrderCode(r.getPurchaseOrder() != null ? r.getPurchaseOrder().getPoCode() : null)
                .createdBy(r.getCreatedBy())
                .receiptLines(lines)
                .build();
    }
}
