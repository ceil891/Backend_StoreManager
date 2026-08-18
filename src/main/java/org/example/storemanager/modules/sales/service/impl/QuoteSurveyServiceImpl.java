package org.example.storemanager.modules.sales.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.partnerarea.entity.Customer;
import org.example.storemanager.modules.partnerarea.repository.CustomerRepository;
import org.example.storemanager.modules.sales.dto.request.CreateQuoteRequest;
import org.example.storemanager.modules.sales.dto.request.CreateQuoteSurveyRequest;
import org.example.storemanager.modules.sales.dto.request.QuoteDetailRequest;
import org.example.storemanager.modules.sales.dto.request.UpdateQuoteSurveyRequest;
import org.example.storemanager.modules.sales.dto.response.QuoteResponse;
import org.example.storemanager.modules.sales.dto.response.QuoteSurveyResponse;
import org.example.storemanager.modules.sales.entity.QuoteSurvey;
import org.example.storemanager.modules.sales.repository.QuoteSurveyRepository;
import org.example.storemanager.modules.sales.service.QuoteService;
import org.example.storemanager.modules.sales.service.QuoteSurveyService;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.system.repository.BranchRepository;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuoteSurveyServiceImpl implements QuoteSurveyService {

    private final QuoteSurveyRepository surveyRepository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final QuoteService quoteService;

    @Override
    public QuoteSurveyResponse createSurvey(CreateQuoteSurveyRequest request) {
        Customer customer = customerRepository.findByIdAndIsDeletedFalse(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        String username = getCurrentUsername();
        String uniqueCode = generateUniqueSurveyCode(request.getSurveyCode());

        QuoteSurvey survey = QuoteSurvey.builder()
                .surveyCode(uniqueCode)
                .customer(customer)
                .branch(branch)
                .contactPerson(request.getContactPerson())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .salespersonId(request.getSalespersonId())
                .salespersonName(request.getSalespersonName() != null ? request.getSalespersonName() : username)
                .surveyDate(request.getSurveyDate() != null ? request.getSurveyDate() : LocalDateTime.now())
                .responseDeadline(request.getResponseDeadline())
                .requestedProducts(request.getRequestedProducts())
                .expectedQuantity(request.getExpectedQuantity())
                .expectedBudget(request.getExpectedBudget())
                .technicalRequirements(request.getTechnicalRequirements())
                .deliveryRequirements(request.getDeliveryRequirements())
                .paymentRequirements(request.getPaymentRequirements())
                .potentialLevel(request.getPotentialLevel() != null ? request.getPotentialLevel() : "TRUNG_BINH")
                .note(request.getNote())
                .attachments(request.getAttachments())
                .status(request.getStatus() != null ? request.getStatus() : "NEW")
                .build();

        survey.setIsDeleted(false);
        survey.setCreatedBy(username);

        QuoteSurvey saved = surveyRepository.save(survey);
        return mapToResponse(saved);
    }

    @Override
    public QuoteSurveyResponse updateSurvey(Long id, UpdateQuoteSurveyRequest request) {
        QuoteSurvey survey = surveyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuoteSurvey", "id", id));

        Customer customer = customerRepository.findByIdAndIsDeletedFalse(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        survey.setCustomer(customer);
        survey.setBranch(branch);
        survey.setContactPerson(request.getContactPerson());
        survey.setContactPhone(request.getContactPhone());
        survey.setContactEmail(request.getContactEmail());
        survey.setSalespersonId(request.getSalespersonId());
        if (request.getSalespersonName() != null) survey.setSalespersonName(request.getSalespersonName());
        survey.setSurveyDate(request.getSurveyDate());
        survey.setResponseDeadline(request.getResponseDeadline());
        survey.setRequestedProducts(request.getRequestedProducts());
        survey.setExpectedQuantity(request.getExpectedQuantity());
        survey.setExpectedBudget(request.getExpectedBudget());
        survey.setTechnicalRequirements(request.getTechnicalRequirements());
        survey.setDeliveryRequirements(request.getDeliveryRequirements());
        survey.setPaymentRequirements(request.getPaymentRequirements());
        survey.setPotentialLevel(request.getPotentialLevel());
        survey.setNote(request.getNote());
        survey.setAttachments(request.getAttachments());
        survey.setStatus(request.getStatus());
        survey.setUpdatedBy(getCurrentUsername());

        QuoteSurvey updated = surveyRepository.save(survey);
        return mapToResponse(updated);
    }

    @Override
    public QuoteSurveyResponse updateStatus(Long id, String status) {
        QuoteSurvey survey = surveyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuoteSurvey", "id", id));
        survey.setStatus(status);
        survey.setUpdatedBy(getCurrentUsername());
        return mapToResponse(surveyRepository.save(survey));
    }

    @Override
    public void deleteSurvey(Long id) {
        QuoteSurvey survey = surveyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuoteSurvey", "id", id));
        survey.setIsDeleted(true);
        survey.setDeletedBy(getCurrentUsername());
        survey.setDeletedAt(LocalDateTime.now());
        surveyRepository.save(survey);
    }

    @Override
    @Transactional(readOnly = true)
    public QuoteSurveyResponse getSurveyById(Long id) {
        QuoteSurvey survey = surveyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuoteSurvey", "id", id));
        return mapToResponse(survey);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuoteSurveyResponse> getAllSurveys(String search, String status, Long branchId, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sorting);
        Page<QuoteSurvey> pageResult = surveyRepository.findAllSurveys(search, status, branchId, includeDeleted, pageable);
        return pageResult.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuoteSurveyResponse> getSurveysPaginated(String search, String status, Long branchId, int page, int size, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<QuoteSurvey> pageResult = surveyRepository.findAllSurveys(search, status, branchId, includeDeleted, pageable);

        List<QuoteSurveyResponse> content = pageResult.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
        return PageResponse.<QuoteSurveyResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    @Override
    public QuoteResponse convertToQuote(Long surveyId) {
        QuoteSurvey survey = surveyRepository.findByIdAndIsDeletedFalse(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("QuoteSurvey", "id", surveyId));

        CreateQuoteRequest quoteReq = new CreateQuoteRequest();
        quoteReq.setQuoteCode("QT-" + survey.getSurveyCode().replace("KS-", ""));
        quoteReq.setQuoteDate(LocalDateTime.now());
        quoteReq.setValidUntil(LocalDateTime.now().plusDays(30));
        quoteReq.setCustomerId(survey.getCustomer().getId());
        quoteReq.setBranchId(survey.getBranch().getId());
        quoteReq.setSalesPersonId(survey.getSalespersonId());
        quoteReq.setSalesPersonName(survey.getSalespersonName());
        quoteReq.setPaymentTerms(survey.getPaymentRequirements());
        quoteReq.setDeliveryTerms(survey.getDeliveryRequirements());
        quoteReq.setStatus("DRAFT");
        quoteReq.setNote("Tạo tự động từ Khảo sát báo giá mã: " + survey.getSurveyCode() +
                (survey.getNote() != null ? "\nGhi chú khảo sát: " + survey.getNote() : ""));

        // Default item for quote created from survey
        QuoteDetailRequest detail = new QuoteDetailRequest();
        detail.setProductId(1L);
        detail.setDescription(survey.getRequestedProducts() != null ? survey.getRequestedProducts() : "Sản phẩm / Dịch vụ khảo sát");
        detail.setUnit("Gói");
        detail.setQuantity(BigDecimal.ONE);
        detail.setUnitPrice(survey.getExpectedBudget() != null ? survey.getExpectedBudget() : BigDecimal.ZERO);
        detail.setDiscountType("AMOUNT");
        detail.setDiscountValue(BigDecimal.ZERO);
        detail.setTaxRate(BigDecimal.valueOf(10));

        List<QuoteDetailRequest> details = new ArrayList<>();
        details.add(detail);
        quoteReq.setDetails(details);

        QuoteResponse quoteResponse = quoteService.createQuote(quoteReq);

        survey.setStatus("QUOTED");
        survey.setQuoteId(quoteResponse.getId());
        survey.setUpdatedBy(getCurrentUsername());
        surveyRepository.save(survey);

        return quoteResponse;
    }

    private String generateUniqueSurveyCode(String requestedCode) {
        if (requestedCode != null && !requestedCode.trim().isEmpty()) {
            String cleanCode = requestedCode.trim();
            if (!surveyRepository.existsBySurveyCode(cleanCode)) {
                return cleanCode;
            }
        }
        String prefix = "KS-" + DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now()) + "-";
        for (int i = 1; i <= 9999; i++) {
            String candidate = prefix + String.format("%04d", i);
            if (!surveyRepository.existsBySurveyCode(candidate)) {
                return candidate;
            }
        }
        return prefix + System.currentTimeMillis();
    }

    private QuoteSurveyResponse mapToResponse(QuoteSurvey s) {
        return QuoteSurveyResponse.builder()
                .id(s.getId())
                .surveyCode(s.getSurveyCode())
                .customerId(s.getCustomer() != null ? s.getCustomer().getId() : null)
                .customerName(s.getCustomer() != null ? s.getCustomer().getName() : "")
                .branchId(s.getBranch() != null ? s.getBranch().getId() : null)
                .branchName(s.getBranch() != null ? s.getBranch().getBranchName() : "")
                .contactPerson(s.getContactPerson())
                .contactPhone(s.getContactPhone())
                .contactEmail(s.getContactEmail())
                .salespersonId(s.getSalespersonId())
                .salespersonName(s.getSalespersonName())
                .surveyDate(s.getSurveyDate())
                .responseDeadline(s.getResponseDeadline())
                .requestedProducts(s.getRequestedProducts())
                .expectedQuantity(s.getExpectedQuantity())
                .expectedBudget(s.getExpectedBudget())
                .technicalRequirements(s.getTechnicalRequirements())
                .deliveryRequirements(s.getDeliveryRequirements())
                .paymentRequirements(s.getPaymentRequirements())
                .potentialLevel(s.getPotentialLevel())
                .note(s.getNote())
                .attachments(s.getAttachments())
                .status(s.getStatus())
                .quoteId(s.getQuoteId())
                .createdAt(s.getCreatedAt())
                .createdBy(s.getCreatedBy())
                .build();
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "id");
        }
        String[] parts = sort.split(",");
        String property = parts[0];
        Sort.Direction direction = (parts.length > 1 && parts[1].equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "system";
    }
}
