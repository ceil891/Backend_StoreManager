package org.example.storemanager.modules.sales.service;

import org.example.storemanager.modules.sales.dto.request.CreateCustomerReturnRequest;
import org.example.storemanager.modules.sales.dto.request.UpdateCustomerReturnRequest;
import org.example.storemanager.modules.sales.dto.response.CustomerReturnResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;

import java.util.List;

public interface CustomerReturnService {
    CustomerReturnResponse createReturn(CreateCustomerReturnRequest request);
    CustomerReturnResponse updateReturn(Long id, UpdateCustomerReturnRequest request);
    CustomerReturnResponse updateStatus(Long id, String status);
    void deleteReturn(Long id);
    CustomerReturnResponse getReturnById(Long id);
    List<CustomerReturnResponse> getAllReturns(String search, String status, Long branchId, String sort, boolean includeDeleted);
    PageResponse<CustomerReturnResponse> getReturnsPaginated(String search, String status, Long branchId, int page, int size, String sort, boolean includeDeleted);
}
