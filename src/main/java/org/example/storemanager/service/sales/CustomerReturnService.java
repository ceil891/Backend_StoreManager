package org.example.storemanager.service.sales;

import org.example.storemanager.dto.request.sales.CreateCustomerReturnRequest;
import org.example.storemanager.dto.request.sales.UpdateCustomerReturnRequest;
import org.example.storemanager.dto.response.sales.CustomerReturnResponse;
import org.example.storemanager.dto.response.common.PageResponse;

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
