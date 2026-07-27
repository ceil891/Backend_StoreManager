package org.example.storemanager.modules.purchase.service;

import org.example.storemanager.modules.purchase.dto.request.CreateSupplierContractRequest;
import org.example.storemanager.modules.purchase.dto.request.UpdateSupplierContractRequest;
import org.example.storemanager.modules.purchase.dto.response.SupplierContractResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.catalog.entity.Product;

import java.time.LocalDate;
import java.util.List;

public interface SupplierContractService {
    SupplierContractResponse createContract(CreateSupplierContractRequest request);
    SupplierContractResponse updateContract(Long id, UpdateSupplierContractRequest request);
    SupplierContractResponse updateStatus(Long id, String status);
    void deleteContract(Long id);
    SupplierContractResponse getContractById(Long id);
    List<SupplierContractResponse> getAllContracts(String search, String status, Long supplierId, String sort, boolean includeDeleted);
    PageResponse<SupplierContractResponse> getContractsPaginated(String search, String status, Long supplierId, int page, int size, String sort, boolean includeDeleted);

    SupplierContractResponse submitContract(Long id);
    SupplierContractResponse approveContract(Long id);
    SupplierContractResponse activateContract(Long id);
    SupplierContractResponse terminateContract(Long id);
    SupplierContractResponse renewContract(Long id, LocalDate newEndDate);

    List<SupplierContractResponse> getActiveContracts();
    List<SupplierContractResponse> getExpiringContracts(int days);

    List<Product> getContractProducts(Long id);
    void addContractProduct(Long id, Long productId);
    void removeContractProduct(Long id, Long productId);
}
