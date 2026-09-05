package org.example.storemanager.modules.purchase.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.purchase.dto.request.CreatePurchaseInvoiceRequest;
import org.example.storemanager.modules.purchase.dto.request.UpdatePurchaseInvoiceRequest;
import org.example.storemanager.modules.purchase.dto.response.PurchaseInvoiceResponse;
import org.example.storemanager.modules.purchase.entity.PurchaseInvoice;
import org.example.storemanager.modules.purchase.repository.PurchaseInvoiceRepository;
import org.example.storemanager.modules.purchase.service.PurchaseInvoiceService;
import org.example.storemanager.modules.partnerarea.entity.Supplier;
import org.example.storemanager.modules.partnerarea.repository.SupplierRepository;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.system.repository.BranchRepository;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.example.storemanager.modules.finance.entity.PaymentVoucher;
import org.example.storemanager.modules.finance.repository.PaymentVoucherRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class PurchaseInvoiceServiceImpl implements PurchaseInvoiceService {

    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final SupplierRepository supplierRepository;
    private final BranchRepository branchRepository;
    private final PaymentVoucherRepository paymentVoucherRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseInvoiceResponse> getAllInvoices() {
        return purchaseInvoiceRepository.findByIsDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseInvoiceResponse getInvoiceById(Long id) {
        PurchaseInvoice inv = purchaseInvoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseInvoice", "id", id));
        return mapToResponse(inv);
    }

    @Override
    public PurchaseInvoiceResponse createInvoice(CreatePurchaseInvoiceRequest request) {
        if (purchaseInvoiceRepository.existsByInvoiceCodeAndIsDeletedFalse(request.getInvoiceCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã hóa đơn " + request.getInvoiceCode() + " đã tồn tại");
        }

        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));

        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId()).orElse(null);
        }

        BigDecimal subTotal = request.getSubTotal() != null ? request.getSubTotal() : BigDecimal.ZERO;
        BigDecimal vatAmount = request.getVatAmount() != null ? request.getVatAmount() : BigDecimal.ZERO;
        BigDecimal discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = request.getTotalAmount() != null ? request.getTotalAmount() : subTotal.add(vatAmount).subtract(discountAmount);

        PurchaseInvoice inv = PurchaseInvoice.builder()
                .invoiceCode(request.getInvoiceCode())
                .poCode(request.getPoCode())
                .poId(request.getPoId())
                .supplier(supplier)
                .branch(branch)
                .invoiceDate(request.getInvoiceDate())
                .dueDate(request.getDueDate() != null ? request.getDueDate() : request.getInvoiceDate())
                .subTotal(subTotal)
                .vatAmount(vatAmount)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .status(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus() : "CHO_THANH_TOAN")
                .paymentTerms(request.getPaymentTerms())
                .build();

        inv.setIsDeleted(false);
        inv.setCreatedBy(getCurrentUsername());
        inv.setNote(request.getNote());

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (org.example.storemanager.modules.purchase.dto.PurchaseInvoiceItemDTO itemDto : request.getItems()) {
                org.example.storemanager.modules.purchase.entity.PurchaseInvoiceItem item = org.example.storemanager.modules.purchase.entity.PurchaseInvoiceItem.builder()
                        .purchaseInvoice(inv)
                        .productId(itemDto.getProductId())
                        .productVariantId(itemDto.getProductVariantId())
                        .productName(itemDto.getProductName())
                        .sku(itemDto.getSku())
                        .unitName(itemDto.getUnitName())
                        .quantity(itemDto.getQuantity() != null ? itemDto.getQuantity() : BigDecimal.ONE)
                        .unitPrice(itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : BigDecimal.ZERO)
                        .vatRate(itemDto.getVatRate() != null ? itemDto.getVatRate() : BigDecimal.ZERO)
                        .vatAmount(itemDto.getVatAmount() != null ? itemDto.getVatAmount() : BigDecimal.ZERO)
                        .totalAmount(itemDto.getTotalAmount() != null ? itemDto.getTotalAmount() : BigDecimal.ZERO)
                        .build();
                item.setIsDeleted(false);
                item.setCreatedBy(getCurrentUsername());
                inv.getItems().add(item);
            }
        }

        PurchaseInvoice saved = purchaseInvoiceRepository.save(inv);
        return mapToResponse(saved);
    }

    @Override
    public PurchaseInvoiceResponse updateInvoice(Long id, UpdatePurchaseInvoiceRequest request) {
        PurchaseInvoice inv = purchaseInvoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseInvoice", "id", id));

        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));
            inv.setSupplier(supplier);
        }

        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId()).orElse(null);
            inv.setBranch(branch);
        }

        if (request.getPoCode() != null) inv.setPoCode(request.getPoCode());
        if (request.getPoId() != null) inv.setPoId(request.getPoId());
        if (request.getInvoiceDate() != null) inv.setInvoiceDate(request.getInvoiceDate());
        if (request.getDueDate() != null) inv.setDueDate(request.getDueDate());
        if (request.getSubTotal() != null) inv.setSubTotal(request.getSubTotal());
        if (request.getVatAmount() != null) inv.setVatAmount(request.getVatAmount());
        if (request.getDiscountAmount() != null) inv.setDiscountAmount(request.getDiscountAmount());
        if (request.getTotalAmount() != null) inv.setTotalAmount(request.getTotalAmount());
        if (request.getStatus() != null) inv.setStatus(request.getStatus());
        if (request.getPaymentTerms() != null) inv.setPaymentTerms(request.getPaymentTerms());
        if (request.getNote() != null) inv.setNote(request.getNote());

        if (request.getItems() != null) {
            inv.getItems().clear();
            for (org.example.storemanager.modules.purchase.dto.PurchaseInvoiceItemDTO itemDto : request.getItems()) {
                org.example.storemanager.modules.purchase.entity.PurchaseInvoiceItem item = org.example.storemanager.modules.purchase.entity.PurchaseInvoiceItem.builder()
                        .purchaseInvoice(inv)
                        .productId(itemDto.getProductId())
                        .productVariantId(itemDto.getProductVariantId())
                        .productName(itemDto.getProductName())
                        .sku(itemDto.getSku())
                        .unitName(itemDto.getUnitName())
                        .quantity(itemDto.getQuantity() != null ? itemDto.getQuantity() : BigDecimal.ONE)
                        .unitPrice(itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : BigDecimal.ZERO)
                        .vatRate(itemDto.getVatRate() != null ? itemDto.getVatRate() : BigDecimal.ZERO)
                        .vatAmount(itemDto.getVatAmount() != null ? itemDto.getVatAmount() : BigDecimal.ZERO)
                        .totalAmount(itemDto.getTotalAmount() != null ? itemDto.getTotalAmount() : BigDecimal.ZERO)
                        .build();
                item.setIsDeleted(false);
                item.setCreatedBy(getCurrentUsername());
                inv.getItems().add(item);
            }
        }

        inv.setUpdatedBy(getCurrentUsername());
        PurchaseInvoice saved = purchaseInvoiceRepository.save(inv);
        return mapToResponse(saved);
    }

    @Override
    public void deleteInvoice(Long id) {
        PurchaseInvoice inv = purchaseInvoiceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseInvoice", "id", id));
        inv.setIsDeleted(true);
        inv.setDeletedBy(getCurrentUsername());
        inv.setDeletedAt(LocalDateTime.now());
        purchaseInvoiceRepository.save(inv);
    }

    private PurchaseInvoiceResponse mapToResponse(PurchaseInvoice inv) {
        List<org.example.storemanager.modules.purchase.dto.PurchaseInvoiceItemDTO> itemDTOs = new ArrayList<>();
        if (inv.getItems() != null) {
            itemDTOs = inv.getItems().stream()
                    .filter(item -> !Boolean.TRUE.equals(item.getIsDeleted()))
                    .map(item -> org.example.storemanager.modules.purchase.dto.PurchaseInvoiceItemDTO.builder()
                            .id(item.getId())
                            .productId(item.getProductId())
                            .productVariantId(item.getProductVariantId())
                            .productName(item.getProductName())
                            .sku(item.getSku())
                            .unitName(item.getUnitName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .vatRate(item.getVatRate())
                            .vatAmount(item.getVatAmount())
                            .totalAmount(item.getTotalAmount())
                            .build())
                    .collect(Collectors.toList());
        }

        BigDecimal total = inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal paid = BigDecimal.ZERO;

        if ("DA_THANH_TOAN".equalsIgnoreCase(inv.getStatus()) || "PAID".equalsIgnoreCase(inv.getStatus())) {
            paid = total;
        } else {
            if (inv.getInvoiceCode() != null && !inv.getInvoiceCode().isBlank()) {
                List<PaymentVoucher> vouchers = paymentVoucherRepository.findByInvoiceCodeAndIsDeletedFalse(inv.getInvoiceCode());
                for (PaymentVoucher pv : vouchers) {
                    if ("COMPLETED".equalsIgnoreCase(pv.getStatus()) && pv.getAmount() != null) {
                        paid = paid.add(pv.getAmount());
                    }
                }
            }
            if (paid.compareTo(BigDecimal.ZERO) == 0 && inv.getPoCode() != null && !inv.getPoCode().isBlank()) {
                List<PaymentVoucher> poVouchers = paymentVoucherRepository.findByInvoiceCodeAndIsDeletedFalse(inv.getPoCode());
                for (PaymentVoucher pv : poVouchers) {
                    if ("COMPLETED".equalsIgnoreCase(pv.getStatus()) && pv.getAmount() != null) {
                        paid = paid.add(pv.getAmount());
                    }
                }
            }
        }

        BigDecimal remaining = total.subtract(paid).max(BigDecimal.ZERO);
        String finalStatus = inv.getStatus();
        if (paid.compareTo(total) >= 0 && total.compareTo(BigDecimal.ZERO) > 0) {
            finalStatus = "DA_THANH_TOAN";
        } else if (paid.compareTo(BigDecimal.ZERO) > 0 && remaining.compareTo(BigDecimal.ZERO) > 0) {
            finalStatus = "PARTIAL_PAID";
        }

        return PurchaseInvoiceResponse.builder()
                .id(inv.getId())
                .invoiceCode(inv.getInvoiceCode())
                .poCode(inv.getPoCode())
                .poId(inv.getPoId())
                .supplierId(inv.getSupplier() != null ? inv.getSupplier().getId() : null)
                .supplierName(inv.getSupplier() != null ? inv.getSupplier().getName() : null)
                .branchId(inv.getBranch() != null ? inv.getBranch().getId() : null)
                .branchName(inv.getBranch() != null ? inv.getBranch().getBranchName() : null)
                .invoiceDate(inv.getInvoiceDate())
                .dueDate(inv.getDueDate())
                .subTotal(inv.getSubTotal())
                .vatAmount(inv.getVatAmount())
                .discountAmount(inv.getDiscountAmount())
                .totalAmount(total)
                .paidAmount(paid)
                .remainingDebt(remaining)
                .status(finalStatus)
                .paymentTerms(inv.getPaymentTerms())
                .note(inv.getNote())
                .createdAt(inv.getCreatedAt())
                .createdBy(inv.getCreatedBy())
                .items(itemDTOs)
                .build();
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}
