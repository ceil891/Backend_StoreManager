package org.example.storemanager.service.purchase;

import org.example.storemanager.dto.request.purchase.CreateSupplierContractRequest;
import org.example.storemanager.dto.request.purchase.UpdateSupplierContractRequest;
import org.example.storemanager.dto.response.purchase.SupplierContractResponse;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.entity.catalog.Product;

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
