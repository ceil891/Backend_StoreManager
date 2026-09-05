package org.example.storemanager.modules.sales.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.sales.dto.request.CreateQuoteRequest;
import org.example.storemanager.modules.sales.dto.request.QuoteDetailRequest;
import org.example.storemanager.modules.sales.dto.request.UpdateQuoteRequest;
import org.example.storemanager.modules.sales.dto.response.QuoteDetailResponse;
import org.example.storemanager.modules.sales.dto.response.QuoteResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.sales.entity.Quote;
import org.example.storemanager.modules.sales.entity.QuoteDetail;
import org.example.storemanager.modules.sales.entity.SaleOrder;
import org.example.storemanager.modules.sales.entity.SaleOrderDetail;
import org.example.storemanager.modules.partnerarea.entity.Customer;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.catalog.entity.ProductVariant;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.sales.repository.QuoteRepository;
import org.example.storemanager.modules.sales.repository.QuoteDetailRepository;
import org.example.storemanager.modules.sales.repository.SaleOrderRepository;
import org.example.storemanager.modules.sales.repository.SaleOrderDetailRepository;
import org.example.storemanager.modules.partnerarea.repository.CustomerRepository;
import org.example.storemanager.modules.system.repository.BranchRepository;
import org.example.storemanager.modules.catalog.repository.ProductRepository;
import org.example.storemanager.modules.catalog.repository.ProductVariantRepository;
import org.example.storemanager.modules.sales.service.QuoteService;
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
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;
    private final QuoteDetailRepository quoteDetailRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final SaleOrderDetailRepository saleOrderDetailRepository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public QuoteResponse createQuote(CreateQuoteRequest request) {
        Customer customer = customerRepository.findByIdAndIsDeletedFalse(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        String username = getCurrentUsername();

        String uniqueCode = generateUniqueQuoteCode(request.getQuoteCode());

        Quote quote = Quote.builder()
                .quoteCode(uniqueCode)
                .quoteDate(request.getQuoteDate() != null ? request.getQuoteDate() : LocalDateTime.now())
                .validUntil(request.getValidUntil())
                .revision(1) // Auto initial revision
                .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                .paymentTerms(request.getPaymentTerms())
                .deliveryTerms(request.getDeliveryTerms())
                .warrantyTerms(request.getWarrantyTerms())
                .validityTerms(request.getValidityTerms())
                .shippingAddress(request.getShippingAddress())
                .discountType(request.getDiscountType() != null ? request.getDiscountType() : "AMOUNT")
                .discountValue(request.getDiscountValue() != null ? request.getDiscountValue() : BigDecimal.ZERO)
                .shippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO)
                .taxRate(request.getTaxRate() != null ? request.getTaxRate() : BigDecimal.ZERO)
                .status(request.getStatus() != null ? request.getStatus() : "DRAFT")
                .salesPersonId(request.getSalesPersonId())
                .salesPersonName(request.getSalesPersonName() != null ? request.getSalesPersonName() : username)
                .warehouseId(request.getWarehouseId())
                .warehouseName(request.getWarehouseName())
                .customer(customer)
                .branch(branch)
                .attachments(request.getAttachments())
                .build();

        quote.setIsDeleted(false);
        quote.setCreatedBy(username);
        quote.setNote(request.getNote());

        BigDecimal calculatedSubTotal = BigDecimal.ZERO;
        List<QuoteDetail> details = new ArrayList<>();

        for (QuoteDetailRequest detailReq : request.getDetails()) {
            ProductVariant variant = null;
            Product product = null;

            if (detailReq.getProductVariantId() != null) {
                variant = productVariantRepository.findByIdAndIsDeletedFalse(detailReq.getProductVariantId()).orElse(null);
                if (variant != null) {
                    product = variant.getProduct();
                }
            }

            if (product == null && detailReq.getProductId() != null) {
                product = productRepository.findByIdAndIsDeletedFalse(detailReq.getProductId()).orElse(null);
            }

            if (product == null && variant == null) {
                throw new ResourceNotFoundException("Product/Variant", "id", 
                        detailReq.getProductVariantId() != null ? detailReq.getProductVariantId() : detailReq.getProductId());
            }

            BigDecimal qty = detailReq.getQuantity() != null ? detailReq.getQuantity() : BigDecimal.ONE;
            BigDecimal price = detailReq.getUnitPrice() != null ? detailReq.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal rawLineTotal = qty.multiply(price);

            // Calculate line discount
            BigDecimal lineDiscountAmount = BigDecimal.ZERO;
            if ("PERCENT".equalsIgnoreCase(detailReq.getDiscountType()) && detailReq.getDiscountValue() != null) {
                lineDiscountAmount = rawLineTotal.multiply(detailReq.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else if (detailReq.getDiscountValue() != null) {
                lineDiscountAmount = detailReq.getDiscountValue();
            } else if (detailReq.getDiscount() != null) {
                lineDiscountAmount = detailReq.getDiscount();
            }

            BigDecimal lineTaxAmount = BigDecimal.ZERO;
            if (detailReq.getTaxRate() != null && detailReq.getTaxRate().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal taxable = rawLineTotal.subtract(lineDiscountAmount);
                lineTaxAmount = taxable.multiply(detailReq.getTaxRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }

            BigDecimal lineTotal = rawLineTotal.subtract(lineDiscountAmount).add(lineTaxAmount);
            calculatedSubTotal = calculatedSubTotal.add(rawLineTotal);

            QuoteDetail detail = QuoteDetail.builder()
                    .quote(quote)
                    .productVariant(variant)
                    .product(product)
                    .sku(detailReq.getSku() != null ? detailReq.getSku() : (variant != null ? variant.getSku() : (product != null ? product.getProductCode() : "")))
                    .barcode(detailReq.getBarcode() != null ? detailReq.getBarcode() : (variant != null ? variant.getBarcode() : ""))
                    .description(detailReq.getDescription())
                    .unit(detailReq.getUnit())
                    .quantity(qty)
                    .unitPrice(price)
                    .discountType(detailReq.getDiscountType())
                    .discountValue(detailReq.getDiscountValue())
                    .discountAmount(lineDiscountAmount)
                    .taxRate(detailReq.getTaxRate())
                    .taxAmount(lineTaxAmount)
                    .subTotal(rawLineTotal)
                    .totalAmount(lineTotal)
                    .build();

            detail.setIsDeleted(false);
            detail.setCreatedBy(username);
            details.add(detail);
        }

        BigDecimal sumLineDiscounts = details.stream().map(QuoteDetail::getDiscountAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumLineTaxes = details.stream().map(QuoteDetail::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netLineTotal = calculatedSubTotal.subtract(sumLineDiscounts);

        quote.setSubTotal(netLineTotal);

        // Header discount calculation
        BigDecimal headerDiscountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        if ("PERCENT".equalsIgnoreCase(request.getDiscountType()) && request.getDiscountValue() != null) {
            headerDiscountAmount = netLineTotal.multiply(request.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        quote.setDiscountAmount(headerDiscountAmount);

        // Header Tax calculation
        BigDecimal headerTaxAmount = request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO;
        if (request.getTaxRate() != null && request.getTaxRate().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal taxableAmount = netLineTotal.subtract(headerDiscountAmount);
            if (taxableAmount.compareTo(BigDecimal.ZERO) < 0) taxableAmount = BigDecimal.ZERO;
            headerTaxAmount = taxableAmount.multiply(request.getTaxRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        quote.setTaxAmount(headerTaxAmount.add(sumLineTaxes));

        // Formula: total = netLineTotal - headerDiscountAmount + shippingFee + headerTaxAmount + sumLineTaxes
        BigDecimal shippingFee = request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO;
        BigDecimal finalTotal = netLineTotal.subtract(headerDiscountAmount).add(shippingFee).add(headerTaxAmount).add(sumLineTaxes);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) finalTotal = BigDecimal.ZERO;
        quote.setTotalAmount(finalTotal);

        Quote savedQuote = quoteRepository.save(quote);
        quoteDetailRepository.saveAll(details);

        return mapToResponse(savedQuote, details);
    }

    @Override
    public QuoteResponse updateQuote(Long id, UpdateQuoteRequest request) {
        Quote quote = quoteRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", "id", id));

        Customer customer = customerRepository.findByIdAndIsDeletedFalse(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        String username = getCurrentUsername();

        // Auto increment revision when editing quote
        int currentRev = quote.getRevision() == null ? 1 : quote.getRevision();
        quote.setRevision(currentRev + 1);

        quote.setQuoteDate(request.getQuoteDate() != null ? request.getQuoteDate() : quote.getQuoteDate());
        quote.setValidUntil(request.getValidUntil());
        quote.setCurrency(request.getCurrency() != null ? request.getCurrency() : quote.getCurrency());
        quote.setPaymentTerms(request.getPaymentTerms());
        quote.setDeliveryTerms(request.getDeliveryTerms());
        quote.setWarrantyTerms(request.getWarrantyTerms());
        quote.setValidityTerms(request.getValidityTerms());
        quote.setShippingAddress(request.getShippingAddress());
        quote.setDiscountType(request.getDiscountType() != null ? request.getDiscountType() : "AMOUNT");
        quote.setDiscountValue(request.getDiscountValue() != null ? request.getDiscountValue() : BigDecimal.ZERO);
        quote.setShippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO);
        quote.setTaxRate(request.getTaxRate() != null ? request.getTaxRate() : BigDecimal.ZERO);
        quote.setStatus(request.getStatus());
        quote.setSalesPersonId(request.getSalesPersonId());
        if (request.getSalesPersonName() != null) quote.setSalesPersonName(request.getSalesPersonName());
        quote.setWarehouseId(request.getWarehouseId());
        quote.setWarehouseName(request.getWarehouseName());
        quote.setCustomer(customer);
        quote.setBranch(branch);
        quote.setNote(request.getNote());
        quote.setAttachments(request.getAttachments());
        quote.setUpdatedBy(username);

        // Soft delete old details
        List<QuoteDetail> oldDetails = quoteDetailRepository.findByQuoteIdAndIsDeletedFalse(id);
        for (QuoteDetail detail : oldDetails) {
            detail.setIsDeleted(true);
            detail.setDeletedBy(username);
            detail.setDeletedAt(LocalDateTime.now());
        }
        quoteDetailRepository.saveAll(oldDetails);

        // Add new details
        BigDecimal calculatedSubTotal = BigDecimal.ZERO;
        List<QuoteDetail> newDetails = new ArrayList<>();

        for (QuoteDetailRequest detailReq : request.getDetails()) {
            ProductVariant variant = null;
            Product product = null;

            if (detailReq.getProductVariantId() != null) {
                variant = productVariantRepository.findByIdAndIsDeletedFalse(detailReq.getProductVariantId()).orElse(null);
                if (variant != null) {
                    product = variant.getProduct();
                }
            }

            if (product == null && detailReq.getProductId() != null) {
                product = productRepository.findByIdAndIsDeletedFalse(detailReq.getProductId()).orElse(null);
            }

            BigDecimal qty = detailReq.getQuantity() != null ? detailReq.getQuantity() : BigDecimal.ONE;
            BigDecimal price = detailReq.getUnitPrice() != null ? detailReq.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal rawLineTotal = qty.multiply(price);

            BigDecimal lineDiscountAmount = BigDecimal.ZERO;
            if ("PERCENT".equalsIgnoreCase(detailReq.getDiscountType()) && detailReq.getDiscountValue() != null) {
                lineDiscountAmount = rawLineTotal.multiply(detailReq.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else if (detailReq.getDiscountValue() != null) {
                lineDiscountAmount = detailReq.getDiscountValue();
            } else if (detailReq.getDiscount() != null) {
                lineDiscountAmount = detailReq.getDiscount();
            }

            BigDecimal lineTaxAmount = BigDecimal.ZERO;
            if (detailReq.getTaxRate() != null && detailReq.getTaxRate().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal taxable = rawLineTotal.subtract(lineDiscountAmount);
                lineTaxAmount = taxable.multiply(detailReq.getTaxRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }

            BigDecimal lineTotal = rawLineTotal.subtract(lineDiscountAmount).add(lineTaxAmount);
            calculatedSubTotal = calculatedSubTotal.add(rawLineTotal);

            QuoteDetail detail = QuoteDetail.builder()
                    .quote(quote)
                    .productVariant(variant)
                    .product(product)
                    .sku(detailReq.getSku() != null ? detailReq.getSku() : (variant != null ? variant.getSku() : (product != null ? product.getProductCode() : "")))
                    .barcode(detailReq.getBarcode() != null ? detailReq.getBarcode() : (variant != null ? variant.getBarcode() : ""))
                    .description(detailReq.getDescription())
                    .unit(detailReq.getUnit())
                    .quantity(qty)
                    .unitPrice(price)
                    .discountType(detailReq.getDiscountType())
                    .discountValue(detailReq.getDiscountValue())
                    .discountAmount(lineDiscountAmount)
                    .taxRate(detailReq.getTaxRate())
                    .taxAmount(lineTaxAmount)
                    .subTotal(rawLineTotal)
                    .totalAmount(lineTotal)
                    .build();

            detail.setIsDeleted(false);
            detail.setCreatedBy(username);
            newDetails.add(detail);
        }

        BigDecimal sumLineDiscounts = newDetails.stream().map(QuoteDetail::getDiscountAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumLineTaxes = newDetails.stream().map(QuoteDetail::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netLineTotal = calculatedSubTotal.subtract(sumLineDiscounts);

        quote.setSubTotal(netLineTotal);

        BigDecimal headerDiscountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        if ("PERCENT".equalsIgnoreCase(request.getDiscountType()) && request.getDiscountValue() != null) {
            headerDiscountAmount = netLineTotal.multiply(request.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        quote.setDiscountAmount(headerDiscountAmount);

        BigDecimal headerTaxAmount = request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO;
        if (request.getTaxRate() != null && request.getTaxRate().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal taxableAmount = netLineTotal.subtract(headerDiscountAmount);
            if (taxableAmount.compareTo(BigDecimal.ZERO) < 0) taxableAmount = BigDecimal.ZERO;
            headerTaxAmount = taxableAmount.multiply(request.getTaxRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        quote.setTaxAmount(headerTaxAmount.add(sumLineTaxes));

        BigDecimal shippingFee = request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO;
        BigDecimal finalTotal = netLineTotal.subtract(headerDiscountAmount).add(shippingFee).add(headerTaxAmount).add(sumLineTaxes);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) finalTotal = BigDecimal.ZERO;
        quote.setTotalAmount(finalTotal);

        Quote savedQuote = quoteRepository.save(quote);
        quoteDetailRepository.saveAll(newDetails);

        return mapToResponse(savedQuote, newDetails);
    }

    @Override
    public QuoteResponse updateStatus(Long id, String status) {
        Quote quote = quoteRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", "id", id));

        quote.setStatus(status);
        quote.setUpdatedBy(getCurrentUsername());

        Quote savedQuote = quoteRepository.save(quote);
        List<QuoteDetail> details = quoteDetailRepository.findByQuoteIdAndIsDeletedFalse(id);
        return mapToResponse(savedQuote, details);
    }

    @Override
    public QuoteResponse convertToSaleOrder(Long id) {
        Quote quote = quoteRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", "id", id));

        String username = getCurrentUsername();
        String orderCode = "SO-" + (quote.getQuoteCode() != null ? quote.getQuoteCode().replace("QT-", "") : System.currentTimeMillis());

        SaleOrder saleOrder = SaleOrder.builder()
                .orderCode(orderCode)
                .orderDate(LocalDateTime.now())
                .totalAmount(quote.getSubTotal() != null ? quote.getSubTotal() : quote.getTotalAmount())
                .finalAmount(quote.getTotalAmount())
                .status("PENDING")
                .paymentStatus("UNPAID")
                .orderOrigin("MANUAL")
                .customer(quote.getCustomer())
                .customerName(quote.getCustomer() != null ? quote.getCustomer().getName() : "")
                .customerPhone(quote.getCustomer() != null ? quote.getCustomer().getPhone() : "")
                .shippingAddress(quote.getShippingAddress())
                .branch(quote.getBranch())
                .build();

        saleOrder.setCreatedBy(username);
        saleOrder.setIsDeleted(false);
        SaleOrder savedOrder = saleOrderRepository.save(saleOrder);

        List<QuoteDetail> quoteDetails = quoteDetailRepository.findByQuoteIdAndIsDeletedFalse(id);
        List<SaleOrderDetail> orderDetails = new ArrayList<>();

        for (QuoteDetail qd : quoteDetails) {
            ProductVariant variant = qd.getProductVariant();
            if (variant == null && qd.getProduct() != null) {
                variant = productVariantRepository.findByProductIdAndIsDeletedFalse(qd.getProduct().getId())
                        .stream().findFirst().orElse(null);
            }
            SaleOrderDetail sod = SaleOrderDetail.builder()
                    .order(savedOrder)
                    .productVariant(variant)
                    .productNameSnapshot(qd.getProduct() != null ? qd.getProduct().getName() : (variant != null ? variant.getProduct().getName() : "Sản phẩm"))
                    .skuSnapshot(qd.getSku())
                    .barcodeSnapshot(qd.getBarcode())
                    .variantDescriptionSnapshot(qd.getDescription())
                    .quantity(qd.getQuantity())
                    .unitPrice(qd.getUnitPrice())
                    .unitPriceSnapshot(qd.getUnitPrice())
                    .discountAmount(qd.getDiscountAmount() != null ? qd.getDiscountAmount() : BigDecimal.ZERO)
                    .taxAmount(qd.getTaxAmount() != null ? qd.getTaxAmount() : BigDecimal.ZERO)
                    .taxRate(qd.getTaxRate() != null ? qd.getTaxRate() : BigDecimal.ZERO)
                    .subTotal(qd.getSubTotal())
                    .totalAmount(qd.getTotalAmount())
                    .build();
            sod.setIsDeleted(false);
            sod.setCreatedBy(username);
            orderDetails.add(sod);
        }
        saleOrderDetailRepository.saveAll(orderDetails);

        quote.setStatus("ACCEPTED");
        quote.setUpdatedBy(username);
        Quote updatedQuote = quoteRepository.save(quote);

        return mapToResponse(updatedQuote, quoteDetails);
    }

    @Override
    public byte[] generateQuotePdf(Long id) {
        Quote quote = quoteRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", "id", id));
        List<QuoteDetail> details = quoteDetailRepository.findByQuoteIdAndIsDeletedFalse(id);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>BÁO GIÁ ").append(quote.getQuoteCode()).append("</title>");
        html.append("<style>body{font-family:Arial,sans-serif;padding:30px;color:#333;} h1{color:#059669;margin-bottom:5px;} .header{display:flex;justify-content:space-between;border-bottom:2px solid #059669;padding-bottom:15px;} table{width:100%;border-collapse:collapse;margin-top:20px;} th,td{border:1px solid #ddd;padding:8px;text-align:left;} th{background-color:#f3f4f6;} .total-box{float:right;margin-top:20px;width:300px;}</style></head><body>");
        html.append("<div class='header'><div><h1>BẢNG BÁO GIÁ BÁN HÀNG</h1><p>Mã: <strong>").append(quote.getQuoteCode()).append("</strong> (Rev: ").append(quote.getRevision() != null ? quote.getRevision() : 1).append(")</p><p>Ngày: ").append(quote.getQuoteDate() != null ? quote.getQuoteDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "").append("</p></div>");
        html.append("<div><p><strong>Khách hàng:</strong> ").append(quote.getCustomer() != null ? quote.getCustomer().getName() : "").append("</p><p><strong>Chi nhánh:</strong> ").append(quote.getBranch() != null ? quote.getBranch().getBranchName() : "").append("</p></div></div>");
        
        html.append("<table><thead><tr><th>STT</th><th>Mã SKU</th><th>Tên Sản Phẩm / Biến Thể</th><th>ĐVT</th><th>SL</th><th>Đơn Giá</th><th>Chiết khấu</th><th>Thành Tiền</th></tr></thead><tbody>");
        int idx = 1;
        for (QuoteDetail d : details) {
            String pName = d.getProduct() != null ? d.getProduct().getName() : (d.getProductVariant() != null ? d.getProductVariant().getProduct().getName() : "Sản phẩm");
            html.append("<tr><td>").append(idx++).append("</td>")
                .append("<td>").append(d.getSku() != null ? d.getSku() : "").append("</td>")
                .append("<td>").append(pName).append("</td>")
                .append("<td>").append(d.getUnit() != null ? d.getUnit() : "Cái").append("</td>")
                .append("<td>").append(d.getQuantity()).append("</td>")
                .append("<td>").append(d.getUnitPrice()).append(" ").append(quote.getCurrency()).append("</td>")
                .append("<td>").append(d.getDiscountAmount() != null ? d.getDiscountAmount() : 0).append("</td>")
                .append("<td>").append(d.getSubTotal()).append(" ").append(quote.getCurrency()).append("</td></tr>");
        }
        html.append("</tbody></table>");

        html.append("<div class='total-box'><p>Tiền hàng: <strong>").append(quote.getSubTotal()).append(" ").append(quote.getCurrency()).append("</strong></p>");
        html.append("<p>Giảm giá: <strong>-").append(quote.getDiscountAmount() != null ? quote.getDiscountAmount() : 0).append(" ").append(quote.getCurrency()).append("</strong></p>");
        html.append("<p>Phí vận chuyển: <strong>+").append(quote.getShippingFee() != null ? quote.getShippingFee() : 0).append(" ").append(quote.getCurrency()).append("</strong></p>");
        html.append("<p>Thuế VAT: <strong>+").append(quote.getTaxAmount() != null ? quote.getTaxAmount() : 0).append(" ").append(quote.getCurrency()).append("</strong></p>");
        html.append("<hr/><p style='font-size:16px;'>TỔNG THANH TOÁN: <strong style='color:#059669;'>").append(quote.getTotalAmount()).append(" ").append(quote.getCurrency()).append("</strong></p></div>");

        if (quote.getPaymentTerms() != null || quote.getDeliveryTerms() != null) {
            html.append("<div style='margin-top:120px;clear:both;'><h3>ĐIỀU KHOẢN BÁO GIÁ</h3>");
            if (quote.getPaymentTerms() != null) html.append("<p><strong>Thanh toán:</strong> ").append(quote.getPaymentTerms()).append("</p>");
            if (quote.getDeliveryTerms() != null) html.append("<p><strong>Giao hàng:</strong> ").append(quote.getDeliveryTerms()).append("</p>");
            if (quote.getWarrantyTerms() != null) html.append("<p><strong>Bảo hành:</strong> ").append(quote.getWarrantyTerms()).append("</p>");
            html.append("</div>");
        }

        html.append("</body></html>");
        return html.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void deleteQuote(Long id) {
        Quote quote = quoteRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", "id", id));

        String username = getCurrentUsername();
        quote.setIsDeleted(true);
        quote.setDeletedBy(username);
        quote.setDeletedAt(LocalDateTime.now());
        quoteRepository.save(quote);

        List<QuoteDetail> details = quoteDetailRepository.findByQuoteIdAndIsDeletedFalse(id);
        for (QuoteDetail detail : details) {
            detail.setIsDeleted(true);
            detail.setDeletedBy(username);
            detail.setDeletedAt(LocalDateTime.now());
        }
        quoteDetailRepository.saveAll(details);
    }

    @Override
    @Transactional(readOnly = true)
    public QuoteResponse getQuoteById(Long id) {
        Quote quote = quoteRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", "id", id));

        List<QuoteDetail> details = quoteDetailRepository.findByQuoteIdAndIsDeletedFalse(id);
        return mapToResponse(quote, details);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuoteResponse> getAllQuotes(String search, String status, Long branchId, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sorting);
        Page<Quote> pageResult = quoteRepository.findAllQuotes(search, status, branchId, includeDeleted, pageable);
        return pageResult.getContent().stream()
                .map(q -> {
                    List<QuoteDetail> details = quoteDetailRepository.findByQuoteIdAndIsDeletedFalse(q.getId());
                    return mapToResponse(q, details);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuoteResponse> getQuotesPaginated(String search, String status, Long branchId, int page, int size, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<Quote> pageResult = quoteRepository.findAllQuotes(search, status, branchId, includeDeleted, pageable);

        List<QuoteResponse> content = pageResult.getContent().stream()
                .map(q -> {
                    List<QuoteDetail> details = quoteDetailRepository.findByQuoteIdAndIsDeletedFalse(q.getId());
                    return mapToResponse(q, details);
                })
                .collect(Collectors.toList());

        return PageResponse.<QuoteResponse>builder()
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
        return "admin"; // Default fallback if context authentication is anonymous
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

    private QuoteResponse mapToResponse(Quote q, List<QuoteDetail> details) {
        List<QuoteDetailResponse> detailsResponse = details.stream()
                .map(d -> QuoteDetailResponse.builder()
                        .id(d.getId())
                        .productVariantId(d.getProductVariant() != null ? d.getProductVariant().getId() : null)
                        .productId(d.getProduct() != null ? d.getProduct().getId() : null)
                        .productCode(d.getProduct() != null ? d.getProduct().getProductCode() : "")
                        .productName(d.getProduct() != null ? d.getProduct().getName() : (d.getProductVariant() != null ? d.getProductVariant().getProduct().getName() : "Sản phẩm"))
                        .sku(d.getSku())
                        .barcode(d.getBarcode())
                        .description(d.getDescription())
                        .unit(d.getUnit())
                        .quantity(d.getQuantity())
                        .unitPrice(d.getUnitPrice())
                        .discountType(d.getDiscountType())
                        .discountValue(d.getDiscountValue())
                        .discount(d.getDiscountAmount())
                        .discountAmount(d.getDiscountAmount())
                        .taxRate(d.getTaxRate())
                        .taxAmount(d.getTaxAmount())
                        .subTotal(d.getSubTotal())
                        .totalAmount(d.getTotalAmount())
                        .build())
                .collect(Collectors.toList());

        return QuoteResponse.builder()
                .id(q.getId())
                .quoteCode(q.getQuoteCode())
                .quoteDate(q.getQuoteDate())
                .validUntil(q.getValidUntil())
                .revision(q.getRevision() != null ? q.getRevision() : 1)
                .currency(q.getCurrency() != null ? q.getCurrency() : "VND")
                .paymentTerms(q.getPaymentTerms())
                .deliveryTerms(q.getDeliveryTerms())
                .warrantyTerms(q.getWarrantyTerms())
                .validityTerms(q.getValidityTerms())
                .shippingAddress(q.getShippingAddress())
                .subTotal(q.getSubTotal())
                .discountType(q.getDiscountType())
                .discountValue(q.getDiscountValue())
                .discountAmount(q.getDiscountAmount())
                .shippingFee(q.getShippingFee())
                .taxRate(q.getTaxRate())
                .taxAmount(q.getTaxAmount())
                .totalAmount(q.getTotalAmount())
                .status(q.getStatus())
                .customerId(q.getCustomer() != null ? q.getCustomer().getId() : null)
                .customerName(q.getCustomer() != null ? q.getCustomer().getName() : "")
                .branchId(q.getBranch() != null ? q.getBranch().getId() : null)
                .branchName(q.getBranch() != null ? q.getBranch().getBranchName() : "")
                .warehouseId(q.getWarehouseId())
                .warehouseName(q.getWarehouseName())
                .salesPersonId(q.getSalesPersonId())
                .salesPersonName(q.getSalesPersonName())
                .note(q.getNote())
                .attachments(q.getAttachments())
                .pdfUrl(q.getPdfUrl())
                .createdAt(q.getCreatedAt())
                .createdBy(q.getCreatedBy())
                .details(detailsResponse)
                .build();
    }

    private String generateUniqueQuoteCode(String requestedCode) {
        if (requestedCode != null && !requestedCode.trim().isEmpty()) {
            String cleanCode = requestedCode.trim();
            if (!quoteRepository.existsByQuoteCode(cleanCode)) {
                return cleanCode;
            }
        }
        String prefix = "QT-" + java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd").format(java.time.LocalDateTime.now()) + "-";
        for (int i = 1; i <= 9999; i++) {
            String candidate = prefix + String.format("%04d", i);
            if (!quoteRepository.existsByQuoteCode(candidate)) {
                return candidate;
            }
        }
        return prefix + System.currentTimeMillis();
    }
}
