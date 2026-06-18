package org.example.storemanager.service.partnerarea.customer;

import org.example.storemanager.config.LogActivity;
import org.example.storemanager.dto.request.partnerarea.customerdto.CreateCustomerRequest;
import org.example.storemanager.dto.request.partnerarea.customerdto.UpdateCustomerRequest;
import org.example.storemanager.dto.response.partnerarea.customer.*;
import org.example.storemanager.entity.partnerarea.Customer;
import org.example.storemanager.repository.partnerarea.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal()))
                ? auth.getName() : "System";
    }

    @Override
    @LogActivity(actionType = "CREATE", entityName = "Customer", entityClass = Customer.class)
    public CreateCustomerResponse createCustomer(CreateCustomerRequest req) {
        Customer c = new Customer();
        BeanUtils.copyProperties(req, c);

        // Xử lý các logic tự động
        c.setCustomerCode("CUS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        c.setCreatedBy(getCurrentUser()); // Hàm lấy User mình đã viết ở trên

        customerRepository.save(c);

        // Trả về DTO full thông tin
        return CreateCustomerResponse.builder()
                .id(c.getId())
                .customerCode(c.getCustomerCode())
                .name(c.getName())
                .phone(c.getPhone())
                .email(c.getEmail())
                .address(c.getAddress())
                .taxCode(c.getTaxCode())
                .message("Tạo khách hàng thành công")
                .createdAt(c.getCreatedAt())
                .createdBy(c.getCreatedBy())
                .build();
    }

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "Customer", entityClass = Customer.class)
    public UpdateCustomerResponse updateCustomer(Long id, UpdateCustomerRequest req) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng ID: " + id));

        BeanUtils.copyProperties(req, c);
        c.setUpdatedBy(getCurrentUser());
        customerRepository.save(c);

        return UpdateCustomerResponse.builder()
                .id(c.getId())
                .message("Cập nhật thành công")
                .updatedAt(c.getUpdatedAt())                .updatedBy(c.getUpdatedBy())
                .build();
    }

    @Override
    @LogActivity(actionType = "DELETE", entityName = "Customer", entityClass = Customer.class)
    public DeleteCustomerResponse deleteCustomer(Long id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        // Đánh dấu xóa mềm
        c.setIsDeleted(true);
        c.setDeletedAt(LocalDateTime.now());
        c.setDeletedBy(getCurrentUser()); // Hàm lấy user hiện tại bạn đã viết

        customerRepository.save(c);

        // Trả về DTO mới (Gọn gàng giống hệt Update)
        return DeleteCustomerResponse.builder()
                .id(c.getId())
                .message("Xóa thành công")
                .deletedAt(c.getDeletedAt())
                .deletedBy(c.getDeletedBy())
                .build();
    }

    @Override
    public CustomerDetailResponse getCustomerById(Long id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        return CustomerDetailResponse.builder()
                .id(c.getId())
                .customerCode(c.getCustomerCode())
                .name(c.getName())
                .createdAt(c.getCreatedAt())
                .createdBy(c.getCreatedBy())
                .updatedAt(c.getUpdatedAt())
                .updatedBy(c.getUpdatedBy())
                .deletedAt(c.getDeletedAt())
                .deletedBy(c.getDeletedBy())
                .build();
    }

    @Override
    public Page<CustomerListResponse> getAllCustomers(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        // Chỉ lấy khách hàng chưa bị xóa mềm
        Page<Customer> customers = (keyword != null && !keyword.isEmpty())
                ? customerRepository.searchCustomers(keyword, pageable)
                : customerRepository.findByIsDeletedFalse(pageable);

        return customers.map(c -> CustomerListResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .phone(c.getPhone())
                .build());
    }

    @Override
    public List<SalesHistoryResponse> getSalesHistory(Long id) {
        // Logic thực tế: Gọi OrderRepository để lấy lịch sử đơn hàng theo ID
        return Collections.emptyList();
    }

    @Override
    public List<DebtResponse> getCustomerDebts(Long id) {
        // Logic thực tế: Gọi DebtRepository để lấy công nợ
        return Collections.emptyList();
    }

    @Override
    public String importCustomers(MultipartFile file) {
        return "Tính năng Import đã sẵn sàng để tích hợp logic đọc file";
    }

    @Override
    public byte[] exportCustomers() {
        return new byte[0];
    }
}