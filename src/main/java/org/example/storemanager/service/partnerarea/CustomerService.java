package org.example.storemanager.service.partnerarea;
import org.example.storemanager.dto.request.partnerarea.customerdto.CreateCustomerRequest;
import org.example.storemanager.dto.request.partnerarea.customerdto.UpdateCustomerRequest;
import org.example.storemanager.dto.response.partnerarea.customer.CreateCustomerResponse;
import org.example.storemanager.dto.response.partnerarea.customer.UpdateCustomerResponse;
import org.example.storemanager.dto.response.partnerarea.customer.CustomerDetailResponse;
import org.example.storemanager.dto.response.partnerarea.customer.CustomerListResponse;
import org.example.storemanager.dto.response.partnerarea.customer.DebtResponse;
import org.example.storemanager.dto.response.partnerarea.customer.SalesHistoryResponse;


import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CustomerService {
    CreateCustomerResponse createCustomer(CreateCustomerRequest request);
    UpdateCustomerResponse updateCustomer(Long id, UpdateCustomerRequest request);
    void deleteCustomer(Long id);
    CustomerDetailResponse getCustomerById(Long id);
    Page<CustomerListResponse> getAllCustomers(int page, int size, String keyword);
    List<SalesHistoryResponse> getSalesHistory(Long id);
    List<DebtResponse> getCustomerDebts(Long id);

    // Với Import/Export, thường trả về String hoặc byte[]
    String importCustomers(MultipartFile file);
    byte[] exportCustomers();
}


