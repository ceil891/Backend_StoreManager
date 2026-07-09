package org.example.storemanager.service.hrm;

import org.example.storemanager.dto.request.hrm.contract.*;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.hrm.contract.*;

import java.util.List;

public interface EmployeeContractService {

    CreateEmployeeContractResponse create(CreateEmployeeContractRequest request);

    UpdateEmployeeContractResponse update(Long id, UpdateEmployeeContractRequest request);

    DeleteEmployeeContractResponse delete(Long id);

    UpdateEmployeeContractResponse updateStatus(Long id, Boolean isActive);

    EmployeeContractResponse getById(Long id);

    List<EmployeeContractResponse> getAll(String search, Boolean isActive, Long userId, String status, String sort, boolean includeDeleted);

    PageResponse<EmployeeContractResponse> getPaginated(String search, Boolean isActive, Long userId, String status, int page, int size, String sort, boolean includeDeleted);

    // New methods for contract management
    List<EmployeeContractResponse> getByUserId(Long userId);

    EmployeeContractResponse getCurrentContract(Long userId);

    List<ContractHistoryResponse> getContractHistory(Long userId);

    RenewContractResponse renewContract(Long id, RenewContractRequest request);

    TerminateContractResponse terminateContract(Long id, TerminateContractRequest request);

    EmployeeContractResponse uploadContractFile(Long id, UploadContractFileRequest request);

    List<ExpiringContractResponse> getExpiringContracts(int daysThreshold);
}
