package org.example.storemanager.service.partnerarea.customer;

import org.example.storemanager.dto.request.partnerarea.customerdto.CreateCustomerRequest;
import org.example.storemanager.dto.request.partnerarea.customerdto.UpdateCustomerRequest;
import org.example.storemanager.dto.response.partnerarea.customer.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CustomerService {

    // --- Các thao tác CRUD ---
    CreateCustomerResponse createCustomer(CreateCustomerRequest request);
    UpdateCustomerResponse updateCustomer(Long id, UpdateCustomerRequest request);
    UpdateCustomerResponse updateStatus(Long id, Boolean isActive);
    DeleteCustomerResponse deleteCustomer(Long id);

    // --- Các thao tác Truy vấn ---
    CustomerDetailResponse getCustomerById(Long id);
    Page<CustomerListResponse> getAllCustomers(int page, int size, String keyword);

    // --- Các báo cáo & Tiện ích ---
    List<SalesHistoryResponse> getSalesHistory(Long id);
    List<DebtResponse> getCustomerDebts(Long id);

    // --- Import/Export ---
    String importCustomers(MultipartFile file);
    byte[] exportCustomers();
}