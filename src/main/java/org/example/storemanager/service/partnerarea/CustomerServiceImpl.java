package org.example.storemanager.service.partnerarea;

import org.example.storemanager.config.LogActivity;
import org.example.storemanager.dto.request.partnerarea.customerdto.CreateCustomerRequest;
import org.example.storemanager.dto.request.partnerarea.customerdto.UpdateCustomerRequest;
import org.example.storemanager.dto.response.partnerarea.customer.*;
import org.example.storemanager.entity.partnerarea.Customer;
import org.example.storemanager.repository.partnerarea.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    @LogActivity(actionType = "CREATE", entityName = "Customer", entityClass = Customer.class)
    public CreateCustomerResponse createCustomer(CreateCustomerRequest req) {
        Customer c = new Customer();
        BeanUtils.copyProperties(req, c);
        c.setCustomerCode("CUS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        c.setIsActive(true);
        customerRepository.save(c);

        return CreateCustomerResponse.builder()
                .id(c.getId())
                .customerCode(c.getCustomerCode())
                .message("Tạo khách hàng thành công")
                .build();
    }

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "Customer", entityClass = Customer.class)
    public UpdateCustomerResponse updateCustomer(Long id, UpdateCustomerRequest req) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + id));

        BeanUtils.copyProperties(req, c);
        customerRepository.save(c);

        return UpdateCustomerResponse.builder()
                .id(c.getId())
                .message("Cập nhật thành công")
                .updatedAt(LocalDateTime.now().toString())
                .build();
    }

    @Override
    @LogActivity(actionType = "DELETE", entityName = "Customer", entityClass = Customer.class)
    public void deleteCustomer(Long id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        c.setIsActive(false); // Soft Delete
        customerRepository.save(c);
    }

    @Override
    public CustomerDetailResponse getCustomerById(Long id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        return CustomerDetailResponse.builder()
                .id(c.getId())
                .customerCode(c.getCustomerCode())
                .name(c.getName())
                .phone(c.getPhone())
                .email(c.getEmail())
                .address(c.getAddress())
                .taxCode(c.getTaxCode())
                .groupName(c.getGroup() != null ? c.getGroup().getGroupName() : null)
                .areaName(c.getArea() != null ? c.getArea().getAreaName() : null)
                .build();
    }

    @Override
    public Page<CustomerListResponse> getAllCustomers(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<Customer> customers;
        if (keyword != null && !keyword.isEmpty()) {
            customers = customerRepository.searchCustomers(keyword, pageable);
        } else {
            customers = customerRepository.findByIsActiveTrue(pageable);
        }

        return customers.map(c -> CustomerListResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .phone(c.getPhone())
                .isActive(c.getIsActive())
                .build());
    }

    // --- CÁC PHƯƠNG THỨC MỞ RỘNG (TẠM THỜI TRẢ VỀ DỮ LIỆU RỖNG ĐỂ CHẠY ĐƯỢC CODE) ---

    @Override
    public List<SalesHistoryResponse> getSalesHistory(Long id) {
        // TODO: Viết logic lấy dữ liệu từ bảng Order sau
        return new ArrayList<>();
    }

    @Override
    public List<DebtResponse> getCustomerDebts(Long id) {
        // TODO: Viết logic lấy dữ liệu từ bảng Debt sau
        return new ArrayList<>();
    }

    @Override
    public String importCustomers(MultipartFile file) {
        // TODO: Viết logic xử lý file Excel sau
        return "Chức năng import đang được cập nhật";
    }

    @Override
    public byte[] exportCustomers() {
        // TODO: Viết logic xuất file Excel sau
        return new byte[0];
    }
}