package org.example.storemanager.service.purchase;

import org.example.storemanager.dto.request.purchase.CreateSupplierEvaluationRequest;
import org.example.storemanager.dto.request.purchase.UpdateSupplierEvaluationRequest;
import org.example.storemanager.dto.response.purchase.SupplierEvaluationResponse;
import org.example.storemanager.dto.response.purchase.SupplierScoreResponse;
import org.example.storemanager.dto.response.common.PageResponse;

import java.util.List;

public interface SupplierEvaluationService {
    SupplierEvaluationResponse createEvaluation(CreateSupplierEvaluationRequest request);
    SupplierEvaluationResponse updateEvaluation(Long id, UpdateSupplierEvaluationRequest request);
    SupplierEvaluationResponse updateStatus(Long id, String status);
    void deleteEvaluation(Long id);
    SupplierEvaluationResponse getEvaluationById(Long id);
    List<SupplierEvaluationResponse> getAllEvaluations(String search, Long supplierId, String sort, boolean includeDeleted);
    PageResponse<SupplierEvaluationResponse> getEvaluationsPaginated(String search, Long supplierId, int page, int size, String sort, boolean includeDeleted);

    SupplierEvaluationResponse submitEvaluation(Long id);
    SupplierEvaluationResponse approveEvaluation(Long id);

    List<SupplierEvaluationResponse> getEvaluationsBySupplier(Long supplierId);
    SupplierScoreResponse getSupplierScore(Long supplierId);
}
