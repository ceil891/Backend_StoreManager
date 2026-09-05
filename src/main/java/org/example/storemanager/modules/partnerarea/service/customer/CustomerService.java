package org.example.storemanager.modules.partnerarea.service.customer;

import org.example.storemanager.modules.partnerarea.dto.request.customerdto.CreateCustomerRequest;
import org.example.storemanager.modules.partnerarea.dto.request.customerdto.UpdateCustomerRequest;
import org.example.storemanager.modules.partnerarea.dto.response.customer.*;
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

    Page<CustomerListResponse> getAllCustomers(int page, int size, Boolean isActive);
    Page<CustomerListResponse> getAllCustomers(int page, int size, Boolean isActive, String search);

    // --- Các báo cáo & Tiện ích ---
    List<SalesHistoryResponse> getSalesHistory(Long id);
    List<DebtResponse> getCustomerDebts(Long id);

    // --- Import/Export ---
    String importCustomers(MultipartFile file);
    byte[] exportCustomers();

    // --- Quản lý Mật khẩu & Cấp lại ---
    void resetCustomerPassword(Long id, String newPassword);
    void changeCustomerPassword(Long id, String oldPassword, String newPassword);

    // --- Quản lý Công nợ ---
    CustomerDetailResponse toggleCreditBlock(Long id, Boolean blocked);
}