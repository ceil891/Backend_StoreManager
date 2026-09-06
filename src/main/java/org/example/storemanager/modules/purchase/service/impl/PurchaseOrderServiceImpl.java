package org.example.storemanager.modules.purchase.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.example.storemanager.modules.purchase.entity.PurchaseInvoice;
import org.example.storemanager.modules.purchase.entity.PurchaseInvoiceItem;
import org.example.storemanager.modules.purchase.repository.PurchaseInvoiceRepository;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.example.storemanager.modules.inventory.service.InventoryService;
import org.springframework.context.annotation.Lazy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderDetailRepository purchaseOrderDetailRepository;
    private final ImportReceiptRepository importReceiptRepository;
    private final ImportReceiptDetailRepository importReceiptDetailRepository;
    private final SupplierRepository supplierRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final org.example.storemanager.modules.finance.repository.PaymentVoucherRepository paymentVoucherRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final PlatformTransactionManager transactionManager;
    @Lazy
    private final InventoryService inventoryService;

    @Override
    public PurchaseOrderResponse createOrder(CreatePurchaseOrderRequest request) {
        if (request.getPoDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày đặt hàng không được để trống");
        }
        if (request.getExpectedDate() != null && request.getExpectedDate().toLocalDate().isBefore(request.getPoDate().toLocalDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày nhận hàng dự kiến không được nhỏ hơn Ngày lập đơn");
        }

        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));

        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        String username = getCurrentUsername();

        PurchaseOrder po = PurchaseOrder.builder()
                .poCode(request.getPoCode())
                .poDate(request.getPoDate())
                .expectedDate(request.getExpectedDate())
                .status(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus() : "DRAFT")
                .paymentStatus(request.getPaymentStatus() != null && !request.getPaymentStatus().isBlank() ? request.getPaymentStatus() : "UNPAID")
                .advanceAmount(request.getAdvanceAmount() != null ? request.getAdvanceAmount() : BigDecimal.ZERO)
                .paymentTerms(request.getPaymentTerms())
                .shippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO)
                .vatRate(request.getVatRate())
                .vatAmount(request.getVatAmount())
                .discountAmount(request.getDiscountAmount())
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

        // Auto create PaymentVoucher (Phiếu chi) under the same transaction block if PO is marked fully paid or partially paid on creation
        if ("PAID".equals(request.getPaymentStatus()) || "PARTIAL_ADVANCE".equals(request.getPaymentStatus())) {
            BigDecimal amount = "PAID".equals(request.getPaymentStatus()) ? totalAmount : (request.getAdvanceAmount() != null ? request.getAdvanceAmount() : totalAmount.divide(BigDecimal.valueOf(2)));
            String dateStr = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd").format(java.time.LocalDate.now());
            String voucherCode = "PAY-PUR-" + dateStr + "-" + String.format("%03d", (int)(Math.random() * 900 + 100));
            
            org.example.storemanager.modules.finance.entity.PaymentVoucher pv = org.example.storemanager.modules.finance.entity.PaymentVoucher.builder()
                    .voucherCode(voucherCode)
                    .voucherDate(LocalDateTime.now())
                    .amount(amount)
                    .receiverName(supplier.getName())
                    .status("COMPLETED")
                    .invoiceCode(savedPo.getPoCode())
                    .paymentMethod("CHUYEN_KHOAN")
                    .fundAccountName("Techcombank - 1902838392 (Công ty StoreManager)")
                    .handler(username)
                    .notes("Thanh toán tự động khi tạo Đơn mua hàng " + savedPo.getPoCode())
                    .build();
            pv.setIsDeleted(false);
            pv.setCreatedBy(username);
            
            paymentVoucherRepository.save(pv);
        }

        return mapToResponse(savedPo, details);
    }

    @Override
    public PurchaseOrderResponse updateOrder(Long id, UpdatePurchaseOrderRequest request) {
        if (request.getPoDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày đặt hàng không được để trống");
        }
        if (request.getExpectedDate() != null && request.getExpectedDate().toLocalDate().isBefore(request.getPoDate().toLocalDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày nhận hàng dự kiến không được nhỏ hơn Ngày lập đơn");
        }

        PurchaseOrder po = purchaseOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));

        if ("COMPLETED".equals(po.getStatus()) || "CANCELLED".equals(po.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể chỉnh sửa đơn mua hàng ở trạng thái " + po.getStatus());
        }

        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));

        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        String username = getCurrentUsername();

        po.setPoDate(request.getPoDate());
        po.setExpectedDate(request.getExpectedDate());
        if (request.getStatus() != null) {
            validateStatusTransition(po.getStatus(), request.getStatus());
            po.setStatus(request.getStatus());
        }
        if (request.getPaymentStatus() != null) {
            po.setPaymentStatus(request.getPaymentStatus());
        }
        if (request.getAdvanceAmount() != null) {
            po.setAdvanceAmount(request.getAdvanceAmount());
        }
        if (request.getPaymentTerms() != null) {
            po.setPaymentTerms(request.getPaymentTerms());
        }
        if (request.getShippingFee() != null) {
            po.setShippingFee(request.getShippingFee());
        }
        if (request.getVatRate() != null) {
            po.setVatRate(request.getVatRate());
        }
        if (request.getVatAmount() != null) {
            po.setVatAmount(request.getVatAmount());
        }
        if (request.getDiscountAmount() != null) {
            po.setDiscountAmount(request.getDiscountAmount());
        }
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

        validateStatusTransition(po.getStatus(), status);
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

        if (!"DRAFT".equals(po.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ được phép xóa đơn hàng ở trạng thái Bản nháp (DRAFT)");
        }

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
        PurchaseOrderResponse res = updateStatus(id, "APPROVED");

        // Ticket 38: Tự động sinh Hóa đơn mua hàng (Purchase Invoice - nguồn vào) khi PO được phê duyệt
        // Cô lập hoàn toàn trong transaction riêng biệt (REQUIRES_NEW) để không bao giờ làm hỏng việc duyệt PO
        try {
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            txTemplate.execute(status -> {
                try {
                    PurchaseOrder po = purchaseOrderRepository.findByIdAndIsDeletedFalse(id).orElse(null);
                    if (po != null && purchaseInvoiceRepository != null) {
                        boolean invoiceExists = purchaseInvoiceRepository.findByIsDeletedFalseOrderByCreatedAtDesc()
                                .stream()
                                .anyMatch(inv -> (inv.getPoId() != null && inv.getPoId().equals(po.getId()))
                                        || (inv.getPoCode() != null && inv.getPoCode().equalsIgnoreCase(po.getPoCode())));

                        if (!invoiceExists) {
                            List<PurchaseOrderDetail> poDetails = purchaseOrderDetailRepository.findByPurchaseOrderIdAndIsDeletedFalse(po.getId());
                            BigDecimal subTotal = po.getTotalAmount() != null ? po.getTotalAmount() : BigDecimal.ZERO;
                            BigDecimal vatAmount = po.getVatAmount() != null ? po.getVatAmount() : BigDecimal.ZERO;
                            BigDecimal discountAmount = po.getDiscountAmount() != null ? po.getDiscountAmount() : BigDecimal.ZERO;

                            String baseInvoiceCode = "INV-" + po.getPoCode();
                            String invoiceCode = baseInvoiceCode;
                            if (purchaseInvoiceRepository.existsByInvoiceCodeAndIsDeletedFalse(invoiceCode)) {
                                invoiceCode = baseInvoiceCode + "-" + (System.currentTimeMillis() % 10000);
                            }

                            PurchaseInvoice inv = PurchaseInvoice.builder()
                                    .invoiceCode(invoiceCode)
                                    .poCode(po.getPoCode())
                                    .poId(po.getId())
                                    .supplier(po.getSupplier())
                                    .branch(po.getBranch())
                                    .invoiceDate(LocalDateTime.now())
                                    .dueDate(po.getExpectedDate() != null ? po.getExpectedDate() : LocalDateTime.now().plusDays(30))
                                    .subTotal(subTotal)
                                    .vatAmount(vatAmount)
                                    .discountAmount(discountAmount)
                                    .totalAmount(subTotal.add(vatAmount).subtract(discountAmount))
                                    .status("CHO_THANH_TOAN")
                                    .paymentTerms(po.getPaymentTerms() != null ? po.getPaymentTerms() : "Net 30")
                                    .items(new ArrayList<>())
                                    .build();
                            inv.setIsDeleted(false);
                            inv.setCreatedBy(getCurrentUsername());
                            inv.setNote("Tự động sinh từ đơn mua hàng " + po.getPoCode());

                            for (PurchaseOrderDetail pod : poDetails) {
                                String prodName = pod.getProduct() != null ? pod.getProduct().getName() : "Sản phẩm";
                                String sku = pod.getProduct() != null ? (pod.getProduct().getProductCode() != null ? pod.getProduct().getProductCode() : pod.getProduct().getBarcode()) : "";
                                String unitName = (pod.getProduct() != null && pod.getProduct().getBaseUnit() != null) ? pod.getProduct().getBaseUnit().getUnitName() : "Cái";

                                PurchaseInvoiceItem item = PurchaseInvoiceItem.builder()
                                        .purchaseInvoice(inv)
                                        .productId(pod.getProduct() != null ? pod.getProduct().getId() : null)
                                        .productName(prodName)
                                        .sku(sku != null ? sku : "")
                                        .unitName(unitName != null ? unitName : "Cái")
                                        .quantity(pod.getQuantity() != null ? pod.getQuantity() : BigDecimal.ONE)
                                        .unitPrice(pod.getUnitPrice() != null ? pod.getUnitPrice() : BigDecimal.ZERO)
                                        .vatRate(BigDecimal.ZERO)
                                        .vatAmount(BigDecimal.ZERO)
                                        .totalAmount(pod.getSubTotal() != null ? pod.getSubTotal() : BigDecimal.ZERO)
                                        .build();
                                item.setIsDeleted(false);
                                item.setCreatedBy(getCurrentUsername());
                                inv.getItems().add(item);
                            }
                            purchaseInvoiceRepository.save(inv);
                            log.info("[PurchaseOrder] Auto-generated PurchaseInvoice {} for approved PO {}", inv.getInvoiceCode(), po.getPoCode());
                        }
                    }
                } catch (Exception innerEx) {
                    log.warn("[PurchaseOrder] Inner error auto-generating PurchaseInvoice (safe rollback): {}", innerEx.getMessage());
                    status.setRollbackOnly();
                }
                return null;
            });
        } catch (Exception e) {
            log.warn("[PurchaseOrder] Failed to auto-generate PurchaseInvoice on PO approve: {}", e.getMessage());
        }

        return res;
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

        if (!"APPROVED".equals(po.getStatus()) && !"CONFIRMED".equals(po.getStatus()) 
                && !"SENT_TO_SUPPLIER".equals(po.getStatus()) && !"DISPATCHED".equals(po.getStatus()) 
                && !"IN_TRANSIT".equals(po.getStatus())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, 
                    "Chỉ tạo được phiếu nhập cho đơn mua hàng ở trạng thái APPROVED, SENT_TO_SUPPLIER, CONFIRMED hoặc DISPATCHED/IN_TRANSIT");
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
                    .orElseGet(() -> {
                        String sku = (product.getProductCode() != null && !product.getProductCode().isBlank()) 
                                ? product.getProductCode() 
                                : ("SKU-" + product.getId());
                        if (productVariantRepository.findBySkuAndIsDeletedFalse(sku).isPresent()) {
                            sku = sku + "-" + (System.currentTimeMillis() % 10000);
                        }
                        String varCode = "VAR-" + (product.getProductCode() != null ? product.getProductCode() : ("P" + product.getId()));
                        if (productVariantRepository.findByVariantCodeAndIsDeletedFalse(varCode).isPresent()) {
                            varCode = varCode + "-" + (System.currentTimeMillis() % 10000);
                        }
                        ProductVariant newVariant = ProductVariant.builder()
                                .variantCode(varCode)
                                .sku(sku)
                                .barcode(product.getBarcode() != null ? product.getBarcode() : sku)
                                .price(product.getBasePrice() != null ? product.getBasePrice() : poDetail.getUnitPrice())
                                .status(org.example.storemanager.shared.enums.catalog.VariantStatus.ACTIVE)
                                .isActive(true)
                                .product(product)
                                .build();
                        newVariant.setIsDeleted(false);
                        newVariant.setCreatedBy(username);
                        return productVariantRepository.save(newVariant);
                    });

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

        // Tự động hoàn thành phiếu nhập kho và cộng tồn kho vật lý (SizeInventory & StockLedger)
        try {
            if (inventoryService != null) {
                ImportReceiptDTO completedDto = inventoryService.completeImportReceipt(savedReceipt.getId());
                po.setStatus("RECEIVED");
                purchaseOrderRepository.save(po);
                return completedDto;
            }
        } catch (Exception e) {
            log.warn("[PurchaseOrder] Auto-complete import receipt failed, fallback to pending: {}", e.getMessage());
        }

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

    private String calculatePaymentStatus(PurchaseOrder po) {
        List<org.example.storemanager.modules.finance.entity.PaymentVoucher> allVouchers = 
                paymentVoucherRepository.findAll().stream()
                    .filter(v -> !Boolean.TRUE.equals(v.getIsDeleted()) && v.getInvoiceCode() != null)
                    .filter(v -> {
                        String code = v.getInvoiceCode().trim();
                        return (po.getPoCode() != null && code.toLowerCase().contains(po.getPoCode().toLowerCase()))
                            || code.equalsIgnoreCase("INV-MH-" + po.getId())
                            || code.equalsIgnoreCase(String.valueOf(po.getId()));
                    })
                    .collect(Collectors.toList());

        if (allVouchers.isEmpty()) {
            return po.getPaymentStatus() != null ? po.getPaymentStatus() : "UNPAID";
        }
        
        BigDecimal totalPaid = allVouchers.stream()
                .filter(v -> "COMPLETED".equalsIgnoreCase(v.getStatus()) 
                        || "APPROVED".equalsIgnoreCase(v.getStatus()) 
                        || "DA_THANH_TOAN".equalsIgnoreCase(v.getStatus()))
                .map(v -> v.getAmount() != null ? v.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        po.setAdvanceAmount(totalPaid);
        BigDecimal totalAmt = po.getTotalAmount() != null ? po.getTotalAmount() : BigDecimal.ZERO;

        if (totalPaid.compareTo(BigDecimal.ZERO) == 0) {
            return "UNPAID";
        } else if (totalAmt.compareTo(BigDecimal.ZERO) > 0 && totalPaid.compareTo(totalAmt) >= 0) {
            return "PAID";
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            return "PARTIAL_ADVANCE";
        } else {
            return "UNPAID";
        }
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

        String computedPaymentStatus = calculatePaymentStatus(po);
        if (!computedPaymentStatus.equals(po.getPaymentStatus())) {
            po.setPaymentStatus(computedPaymentStatus);
            purchaseOrderRepository.save(po);
        }

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
                .paymentStatus(computedPaymentStatus)
                .advanceAmount(po.getAdvanceAmount() != null ? po.getAdvanceAmount() : BigDecimal.ZERO)
                .paymentTerms(po.getPaymentTerms())
                .shippingFee(po.getShippingFee())
                .vatRate(po.getVatRate())
                .vatAmount(po.getVatAmount())
                .discountAmount(po.getDiscountAmount())
                .createdAt(po.getCreatedAt())
                .createdBy(po.getCreatedBy())
                .details(detailsResponse)
                .build();
    }

    private void validateStatusTransition(String currentStatus, String targetStatus) {
        if (currentStatus == null || targetStatus == null || currentStatus.equals(targetStatus)) {
            return;
        }
        
        boolean valid = false;
        switch (currentStatus) {
            case "DRAFT":
                if ("PENDING_APPROVAL".equals(targetStatus) || "APPROVED".equals(targetStatus) || "CANCELLED".equals(targetStatus)) {
                    valid = true;
                }
                break;
            case "PENDING_APPROVAL":
                if ("APPROVED".equals(targetStatus) || "CANCELLED".equals(targetStatus) || "DRAFT".equals(targetStatus) || "REJECTED".equals(targetStatus)) {
                    valid = true;
                }
                break;
            case "REJECTED":
                if ("DRAFT".equals(targetStatus) || "PENDING_APPROVAL".equals(targetStatus) || "CANCELLED".equals(targetStatus)) {
                    valid = true;
                }
                break;
            case "APPROVED":
            case "SENT_TO_SUPPLIER":
            case "CONFIRMED":
                if ("SENT_TO_SUPPLIER".equals(targetStatus) || "CONFIRMED".equals(targetStatus) 
                        || "DISPATCHED".equals(targetStatus) || "IN_TRANSIT".equals(targetStatus) 
                        || "DELIVERED".equals(targetStatus) || "RECEIVED".equals(targetStatus) || "COMPLETED".equals(targetStatus)
                        || "CANCELLED".equals(targetStatus)) {
                    valid = true;
                }
                break;
            case "DISPATCHED":
            case "IN_TRANSIT":
                if ("DELIVERED".equals(targetStatus) || "RECEIVED".equals(targetStatus) || "COMPLETED".equals(targetStatus)) {
                    valid = true;
                }
                break;
            case "DELIVERED":
            case "RECEIVED":
                if ("RECEIVED".equals(targetStatus) || "COMPLETED".equals(targetStatus) || "PAID".equals(targetStatus)) {
                    valid = true;
                }
                break;
        }
        
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Chuyển đổi trạng thái từ " + currentStatus + " sang " + targetStatus + " không hợp lệ.");
        }
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
