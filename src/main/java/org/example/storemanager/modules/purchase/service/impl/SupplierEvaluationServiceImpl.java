package org.example.storemanager.modules.purchase.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.purchase.dto.request.CreateSupplierEvaluationRequest;
import org.example.storemanager.modules.purchase.dto.request.UpdateSupplierEvaluationRequest;
import org.example.storemanager.modules.purchase.dto.response.SupplierEvaluationResponse;
import org.example.storemanager.modules.purchase.dto.response.SupplierScoreResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.warranty.entity.SupplierEvaluation;
import org.example.storemanager.modules.partnerarea.entity.Supplier;
import org.example.storemanager.modules.system.entity.User;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.warranty.repository.SupplierEvaluationRepository;
import org.example.storemanager.modules.partnerarea.repository.SupplierRepository;
import org.example.storemanager.modules.system.repository.UserRepository;
import org.example.storemanager.modules.purchase.service.SupplierEvaluationService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SupplierEvaluationServiceImpl implements SupplierEvaluationService {

    private final SupplierEvaluationRepository supplierEvaluationRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;

    @Override
    public SupplierEvaluationResponse createEvaluation(CreateSupplierEvaluationRequest request) {
        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));

        String username = getCurrentUsername();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không tìm thấy thông tin tài khoản người dùng hiện tại"));

        int quality = request.getQualityScore() != null ? request.getQualityScore() : 0;
        int delivery = request.getDeliveryScore() != null ? request.getDeliveryScore() : 0;
        int service = request.getServiceScore() != null ? request.getServiceScore() : 0;
        int price = request.getPriceScore() != null ? request.getPriceScore() : 0;
        int overall = (quality + delivery + service + price) / 4;

        SupplierEvaluation eval = SupplierEvaluation.builder()
                .supplier(supplier)
                .evalDate(request.getEvalDate())
                .score(overall)
                .remarks(request.getRemarks())
                .evaluatedBy(currentUser)
                .evaluationType(request.getEvaluationType())
                .qualityScore(quality)
                .deliveryScore(delivery)
                .serviceScore(service)
                .priceScore(price)
                .overallScore(overall)
                .result(request.getResult())
                .improvement(request.getImprovement())
                .build();

        eval.setIsDeleted(false);
        eval.setCreatedBy(username);
        eval.setNote(request.getNote());

        SupplierEvaluation savedEval = supplierEvaluationRepository.save(eval);
        return mapToResponse(savedEval);
    }

    @Override
    public SupplierEvaluationResponse updateEvaluation(Long id, UpdateSupplierEvaluationRequest request) {
        SupplierEvaluation eval = supplierEvaluationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierEvaluation", "id", id));

        String username = getCurrentUsername();

        int quality = request.getQualityScore() != null ? request.getQualityScore() : 0;
        int delivery = request.getDeliveryScore() != null ? request.getDeliveryScore() : 0;
        int service = request.getServiceScore() != null ? request.getServiceScore() : 0;
        int price = request.getPriceScore() != null ? request.getPriceScore() : 0;
        int overall = (quality + delivery + service + price) / 4;

        eval.setEvalDate(request.getEvalDate());
        eval.setScore(overall);
        eval.setRemarks(request.getRemarks());
        eval.setEvaluationType(request.getEvaluationType());
        eval.setQualityScore(quality);
        eval.setDeliveryScore(delivery);
        eval.setServiceScore(service);
        eval.setPriceScore(price);
        eval.setOverallScore(overall);
        eval.setResult(request.getResult());
        eval.setImprovement(request.getImprovement());
        eval.setNote(request.getNote());
        eval.setUpdatedBy(username);

        SupplierEvaluation savedEval = supplierEvaluationRepository.save(eval);
        return mapToResponse(savedEval);
    }

    @Override
    public SupplierEvaluationResponse updateStatus(Long id, String status) {
        SupplierEvaluation eval = supplierEvaluationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierEvaluation", "id", id));

        // Lưu trạng thái vào trường note hoặc dùng trường result làm trạng thái duyệt
        // Ở đây chúng ta có cột status không? SupplierEvaluation kế thừa BaseEntity, và BaseEntity không có trường status.
        // Tuy nhiên chúng ta có thể lưu trạng thái duyệt vào trường result (DRAFT, SUBMITTED, APPROVED, REJECTED)
        eval.setResult(status);
        eval.setUpdatedBy(getCurrentUsername());

        SupplierEvaluation savedEval = supplierEvaluationRepository.save(eval);
        return mapToResponse(savedEval);
    }

    @Override
    public void deleteEvaluation(Long id) {
        SupplierEvaluation eval = supplierEvaluationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierEvaluation", "id", id));

        String username = getCurrentUsername();
        eval.setIsDeleted(true);
        eval.setDeletedBy(username);
        eval.setDeletedAt(LocalDateTime.now());
        supplierEvaluationRepository.save(eval);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierEvaluationResponse getEvaluationById(Long id) {
        SupplierEvaluation eval = supplierEvaluationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierEvaluation", "id", id));
        return mapToResponse(eval);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierEvaluationResponse> getAllEvaluations(String search, Long supplierId, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sorting);
        Page<SupplierEvaluation> pageResult = supplierEvaluationRepository.findAllEvaluations(search, supplierId, includeDeleted, pageable);
        return pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupplierEvaluationResponse> getEvaluationsPaginated(String search, Long supplierId, int page, int size, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<SupplierEvaluation> pageResult = supplierEvaluationRepository.findAllEvaluations(search, supplierId, includeDeleted, pageable);

        List<SupplierEvaluationResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<SupplierEvaluationResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    @Override
    public SupplierEvaluationResponse submitEvaluation(Long id) {
        return updateStatus(id, "SUBMITTED");
    }

    @Override
    public SupplierEvaluationResponse approveEvaluation(Long id) {
        return updateStatus(id, "APPROVED");
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierEvaluationResponse> getEvaluationsBySupplier(Long supplierId) {
        List<SupplierEvaluation> list = supplierEvaluationRepository.findBySupplierIdAndIsDeletedFalse(supplierId);
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierScoreResponse getSupplierScore(Long supplierId) {
        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", supplierId));

        List<SupplierEvaluation> list = supplierEvaluationRepository.findBySupplierIdAndIsDeletedFalse(supplierId);

        // Chỉ tính điểm dựa trên các đánh giá APPROVED (hoặc đã duyệt)
        List<SupplierEvaluation> approvedList = list.stream()
                .filter(e -> "APPROVED".equalsIgnoreCase(e.getResult()))
                .collect(Collectors.toList());

        if (approvedList.isEmpty()) {
            return SupplierScoreResponse.builder()
                    .supplierId(supplierId)
                    .supplierName(supplier.getName())
                    .qualityScore(0.0)
                    .deliveryScore(0.0)
                    .priceScore(0.0)
                    .serviceScore(0.0)
                    .overallScore(0.0)
                    .build();
        }

        double sumQuality = 0;
        double sumDelivery = 0;
        double sumPrice = 0;
        double sumService = 0;
        double sumOverall = 0;

        for (SupplierEvaluation eval : approvedList) {
            sumQuality += eval.getQualityScore() != null ? eval.getQualityScore() : 0;
            sumDelivery += eval.getDeliveryScore() != null ? eval.getDeliveryScore() : 0;
            sumPrice += eval.getPriceScore() != null ? eval.getPriceScore() : 0;
            sumService += eval.getServiceScore() != null ? eval.getServiceScore() : 0;
            sumOverall += eval.getOverallScore() != null ? eval.getOverallScore() : 0;
        }

        int count = approvedList.size();
        return SupplierScoreResponse.builder()
                .supplierId(supplierId)
                .supplierName(supplier.getName())
                .qualityScore(sumQuality / count)
                .deliveryScore(sumDelivery / count)
                .priceScore(sumPrice / count)
                .serviceScore(sumService / count)
                .overallScore(sumOverall / count)
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

    private SupplierEvaluationResponse mapToResponse(SupplierEvaluation se) {
        return SupplierEvaluationResponse.builder()
                .id(se.getId())
                .supplierId(se.getSupplier().getId())
                .supplierName(se.getSupplier().getName())
                .evalDate(se.getEvalDate())
                .score(se.getScore())
                .remarks(se.getRemarks())
                .evaluatedById(se.getEvaluatedBy() != null ? se.getEvaluatedBy().getId() : null)
                .evaluatedByName(se.getEvaluatedBy() != null ? se.getEvaluatedBy().getUsername() : null)
                .evaluationType(se.getEvaluationType())
                .qualityScore(se.getQualityScore())
                .deliveryScore(se.getDeliveryScore())
                .serviceScore(se.getServiceScore())
                .priceScore(se.getPriceScore())
                .overallScore(se.getOverallScore())
                .result(se.getResult())
                .improvement(se.getImprovement())
                .note(se.getNote())
                .createdAt(se.getCreatedAt())
                .createdBy(se.getCreatedBy())
                .build();
    }
}
