package org.example.storemanager.service.sales.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.sales.CreateQuoteRequest;
import org.example.storemanager.dto.request.sales.QuoteDetailRequest;
import org.example.storemanager.dto.request.sales.UpdateQuoteRequest;
import org.example.storemanager.dto.response.sales.QuoteDetailResponse;
import org.example.storemanager.dto.response.sales.QuoteResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.entity.sales.Quote;
import org.example.storemanager.entity.sales.QuoteDetail;
import org.example.storemanager.entity.partnerarea.Customer;
import org.example.storemanager.entity.system.Branch;
import org.example.storemanager.entity.catalog.Product;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.sales.QuoteRepository;
import org.example.storemanager.repository.sales.QuoteDetailRepository;
import org.example.storemanager.repository.partnerarea.CustomerRepository;
import org.example.storemanager.repository.system.BranchRepository;
import org.example.storemanager.repository.catalog.ProductRepository;
import org.example.storemanager.service.sales.QuoteService;
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
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;
    private final QuoteDetailRepository quoteDetailRepository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    @Override
    public QuoteResponse createQuote(CreateQuoteRequest request) {
        Customer customer = customerRepository.findByIdAndIsDeletedFalse(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        String username = getCurrentUsername();

        Quote quote = Quote.builder()
                .quoteCode(request.getQuoteCode())
                .quoteDate(request.getQuoteDate())
                .validUntil(request.getValidUntil())
                .status(request.getStatus())
                .customer(customer)
                .branch(branch)
                .build();

        quote.setIsDeleted(false);
        quote.setCreatedBy(username);
        quote.setNote(request.getNote());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<QuoteDetail> details = new ArrayList<>();

        for (QuoteDetailRequest detailReq : request.getDetails()) {
            Product product = productRepository.findByIdAndIsDeletedFalse(detailReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", detailReq.getProductId()));

            BigDecimal discount = detailReq.getDiscount() != null ? detailReq.getDiscount() : BigDecimal.ZERO;
            BigDecimal subTotal = detailReq.getQuantity().multiply(detailReq.getUnitPrice().subtract(discount));
            totalAmount = totalAmount.add(subTotal);

            QuoteDetail detail = QuoteDetail.builder()
                    .quote(quote)
                    .product(product)
                    .quantity(detailReq.getQuantity())
                    .unitPrice(detailReq.getUnitPrice())
                    .discount(discount)
                    .subTotal(subTotal)
                    .build();

            detail.setIsDeleted(false);
            detail.setCreatedBy(username);
            details.add(detail);
        }

        quote.setTotalAmount(totalAmount);
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

        quote.setQuoteDate(request.getQuoteDate());
        quote.setValidUntil(request.getValidUntil());
        quote.setStatus(request.getStatus());
        quote.setCustomer(customer);
        quote.setBranch(branch);
        quote.setNote(request.getNote());
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
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<QuoteDetail> newDetails = new ArrayList<>();

        for (QuoteDetailRequest detailReq : request.getDetails()) {
            Product product = productRepository.findByIdAndIsDeletedFalse(detailReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", detailReq.getProductId()));

            BigDecimal discount = detailReq.getDiscount() != null ? detailReq.getDiscount() : BigDecimal.ZERO;
            BigDecimal subTotal = detailReq.getQuantity().multiply(detailReq.getUnitPrice().subtract(discount));
            totalAmount = totalAmount.add(subTotal);

            QuoteDetail detail = QuoteDetail.builder()
                    .quote(quote)
                    .product(product)
                    .quantity(detailReq.getQuantity())
                    .unitPrice(detailReq.getUnitPrice())
                    .discount(discount)
                    .subTotal(subTotal)
                    .build();

            detail.setIsDeleted(false);
            detail.setCreatedBy(username);
            newDetails.add(detail);
        }

        quote.setTotalAmount(totalAmount);
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

    private QuoteResponse mapToResponse(Quote q, List<QuoteDetail> details) {
        List<QuoteDetailResponse> detailsResponse = details.stream()
                .map(d -> QuoteDetailResponse.builder()
                        .id(d.getId())
                        .productId(d.getProduct().getId())
                        .productCode(d.getProduct().getProductCode())
                        .productName(d.getProduct().getName())
                        .quantity(d.getQuantity())
                        .unitPrice(d.getUnitPrice())
                        .discount(d.getDiscount())
                        .subTotal(d.getSubTotal())
                        .build())
                .collect(Collectors.toList());

        return QuoteResponse.builder()
                .id(q.getId())
                .quoteCode(q.getQuoteCode())
                .quoteDate(q.getQuoteDate())
                .validUntil(q.getValidUntil())
                .totalAmount(q.getTotalAmount())
                .status(q.getStatus())
                .customerId(q.getCustomer().getId())
                .customerName(q.getCustomer().getName())
                .branchId(q.getBranch().getId())
                .branchName(q.getBranch().getBranchName())
                .note(q.getNote())
                .createdAt(q.getCreatedAt())
                .createdBy(q.getCreatedBy())
                .details(detailsResponse)
                .build();
    }
}
