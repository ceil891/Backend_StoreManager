package org.example.storemanager.service.partnerarea.customer;

import org.example.storemanager.config.LogActivity;
import org.example.storemanager.dto.request.partnerarea.customerdto.CreateCustomerRequest;
import org.example.storemanager.dto.request.partnerarea.customerdto.UpdateCustomerRequest;
import org.example.storemanager.dto.response.partnerarea.customer.*;
import org.example.storemanager.entity.partnerarea.Customer;
import org.example.storemanager.exception.DuplicateResourceException;
import org.example.storemanager.repository.partnerarea.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.example.storemanager.service.common.CloudinaryService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CloudinaryService cloudinaryService;

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal()))
                ? auth.getName() : "System";
    }

    @Override
    @LogActivity(actionType = "CREATE", entityName = "Customer", entityClass = Customer.class)
    public CreateCustomerResponse createCustomer(CreateCustomerRequest req) {
        if (customerRepository.existsByPhone(req.getPhone())) {
            throw new DuplicateResourceException("Customer", "số điện thoại", req.getPhone());
        }
        if (customerRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateResourceException("Customer", "email", req.getEmail());
        }

        Customer c = new Customer();
        c.setName(req.getName());
        c.setPhone(req.getPhone());
        c.setEmail(req.getEmail());
        c.setAddress(req.getAddress());
        c.setTaxCode(req.getTaxCode());

        // Xử lý Cloudinary
        if (req.getAvatar() != null && !req.getAvatar().isEmpty()) {
            c.setAvatarUrl(cloudinaryService.uploadImage(req.getAvatar()));
        }

        c.setCustomerCode("CUS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        c.setCreatedBy(getCurrentUser());
        c.setIsActive(true);
        c.setPoints(0.0);
        c.setTotalSpend(0.0);
        c.setMembershipRank("Đồng");

        customerRepository.save(c);

        return CreateCustomerResponse.builder()
                .id(c.getId())
                .customerCode(c.getCustomerCode())
                .name(c.getName())
                .phone(c.getPhone())
                .email(c.getEmail())
                .address(c.getAddress())
                .taxCode(c.getTaxCode())
                .avatarUrl(c.getAvatarUrl())
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

        // Copy properties nhưng bỏ qua trường avatar
        BeanUtils.copyProperties(req, c, "avatar");

        // Cập nhật ảnh mới nếu có
        if (req.getAvatar() != null && !req.getAvatar().isEmpty()) {
            c.setAvatarUrl(cloudinaryService.uploadImage(req.getAvatar()));
        }

        c.setUpdatedBy(getCurrentUser());
        customerRepository.save(c);

        return UpdateCustomerResponse.builder()
                .id(c.getId())
                .message("Cập nhật thành công")
                .updatedAt(c.getUpdatedAt())
                .updatedBy(c.getUpdatedBy())
                .build();
    }

    @Override
    @LogActivity(actionType = "DELETE", entityName = "Customer", entityClass = Customer.class)
    public DeleteCustomerResponse deleteCustomer(Long id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        c.setIsDeleted(true);
        c.setDeletedAt(LocalDateTime.now());
        c.setDeletedBy(getCurrentUser());
        customerRepository.save(c);

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
                .phone(c.getPhone())
                .email(c.getEmail())
                .address(c.getAddress())
                .taxCode(c.getTaxCode())
                .note(c.getNote())
                .points(c.getPoints())
                .totalSpend(c.getTotalSpend())
                .membershipRank(c.getMembershipRank())
                .status(c.getIsActive())
                .avatarUrl(c.getAvatarUrl())
                .createdAt(c.getCreatedAt())
                .createdBy(c.getCreatedBy())
                .updatedAt(c.getUpdatedAt())
                .updatedBy(c.getUpdatedBy())
                .build();
    }

    @Override
    public Page<CustomerListResponse> getAllCustomers(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<Customer> customers = (keyword != null && !keyword.isEmpty())
                ? customerRepository.searchCustomers(keyword, pageable)
                : customerRepository.findByIsDeletedFalse(pageable);

        return customers.map(c -> CustomerListResponse.builder()
                .id(c.getId())
                .customerCode(c.getCustomerCode())
                .name(c.getName())
                .phone(c.getPhone())
                .email(c.getEmail())
                .address(c.getAddress())
                .taxCode(c.getTaxCode())
                .membershipRank(c.getMembershipRank())
                .status(c.getIsActive())
                .avatarUrl(c.getAvatarUrl())
                .build());
    }

    @Override public List<SalesHistoryResponse> getSalesHistory(Long id) { return Collections.emptyList(); }
    @Override public List<DebtResponse> getCustomerDebts(Long id) { return Collections.emptyList(); }
    @Override public String importCustomers(MultipartFile file) { return "OK"; }
    @Override public byte[] exportCustomers() { return new byte[0]; }
}