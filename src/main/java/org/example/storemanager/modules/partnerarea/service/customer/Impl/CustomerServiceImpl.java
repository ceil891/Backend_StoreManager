package org.example.storemanager.modules.partnerarea.service.customer.Impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.partnerarea.dto.request.customerdto.CreateCustomerRequest;
import org.example.storemanager.modules.partnerarea.dto.request.customerdto.UpdateCustomerRequest;
import org.example.storemanager.modules.partnerarea.dto.response.customer.*;
import org.example.storemanager.modules.partnerarea.entity.Customer;
import org.example.storemanager.shared.exception.DuplicateResourceException;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.partnerarea.repository.CustomerRepository;
import org.example.storemanager.modules.common.service.CloudinaryService;
import org.example.storemanager.modules.partnerarea.service.customer.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.example.storemanager.modules.partnerarea.entity.PartnerGroup;
import org.example.storemanager.modules.partnerarea.entity.Area;
import org.example.storemanager.modules.partnerarea.repository.PartnerGroupRepository;
import org.example.storemanager.modules.partnerarea.repository.AreaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.example.storemanager.modules.system.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PartnerGroupRepository partnerGroupRepository;
    private final AreaRepository areaRepository;

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) ? auth.getName() : "SYSTEM";
    }

    @Override
    public CreateCustomerResponse createCustomer(CreateCustomerRequest req) {
        if (customerRepository.existsByPhone(req.getPhone()))
            throw new DuplicateResourceException("Customer", "số điện thoại", req.getPhone());

        if (req.getEmail() != null && customerRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateResourceException("Customer", "email", req.getEmail());
        }

        Customer c = new Customer();
        c.setCustomerCode("CUST-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
        c.setName(req.getName());
        c.setPhone(req.getPhone());
        c.setEmail(req.getEmail());
        c.setAddress(req.getAddress());
        c.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
        c.setPoints(0.0);
        c.setTotalSpend(0.0);
        c.setMembershipRank(req.getMembershipRank() != null ? req.getMembershipRank() : "Đồng");
        c.setCreatedBy(getCurrentUsername());
        
        c.setDob(req.getDob());
        c.setTaxCode(req.getTaxCode());
        c.setGender(req.getGender());
        c.setNote(req.getNote());
        
        if (req.getGroupId() != null) {
            PartnerGroup group = partnerGroupRepository.findById(req.getGroupId()).orElse(null);
            c.setGroup(group);
        }
        if (req.getAreaId() != null) {
            Area area = areaRepository.findById(req.getAreaId()).orElse(null);
            c.setArea(area);
        }

        if (req.getAvatar() != null && !req.getAvatar().isEmpty()) {
            try {
                c.setAvatarUrl(cloudinaryService.uploadFile(req.getAvatar(), "customers").getImageUrl());
            } catch (java.io.IOException e) {
                throw new RuntimeException("Lỗi tải ảnh lên Cloudinary", e);
            }
        } else if (req.getAvatarUrl() != null && !req.getAvatarUrl().isBlank()) {
            c.setAvatarUrl(req.getAvatarUrl().trim());
        }

        Customer saved = customerRepository.save(c);
        // Lấy lại từ DB để chắc chắn có dữ liệu mới nhất
        Customer refreshed = customerRepository.findById(saved.getId()).orElse(saved);

        return CreateCustomerResponse.builder()
                .id(refreshed.getId())
                .customerCode(refreshed.getCustomerCode())
                .name(refreshed.getName())
                .phone(refreshed.getPhone())
                .email(refreshed.getEmail())
                .address(refreshed.getAddress())
                .avatarUrl(refreshed.getAvatarUrl())
                .isActive(refreshed.getIsActive())
                .membershipRank(refreshed.getMembershipRank())
                .points(refreshed.getPoints())
                .totalSpend(refreshed.getTotalSpend())
                .createdAt(refreshed.getCreatedAt())
                .createdBy(refreshed.getCreatedBy())
                .dob(refreshed.getDob())
                .taxCode(refreshed.getTaxCode())
                .gender(refreshed.getGender())
                .note(refreshed.getNote())
                .groupId(refreshed.getGroup() != null ? refreshed.getGroup().getId() : null)
                .areaId(refreshed.getArea() != null ? refreshed.getArea().getId() : null)
                .message("Tạo thành công").build();
    }

    private Customer getOrCreateCustomerForUser(Long id) {
        Customer c = customerRepository.findByIdAndIsDeletedFalse(id).orElse(null);
        if (c == null) {
            org.example.storemanager.modules.system.entity.User user = userRepository.findById(id).orElse(null);
            if (user != null) {
                c = customerRepository.findAll().stream()
                        .filter(cust -> !Boolean.TRUE.equals(cust.getIsDeleted()))
                        .filter(cust -> (user.getPhone() != null && user.getPhone().equals(cust.getPhone())) || 
                                        (user.getEmail() != null && user.getEmail().equalsIgnoreCase(cust.getEmail())))
                        .findFirst().orElse(null);
                
                if (c == null) {
                    c = new Customer();
                    c.setCustomerCode("CUST-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
                    c.setName(user.getFullName() != null ? user.getFullName() : user.getUsername());
                    c.setPhone(user.getPhone());
                    c.setEmail(user.getEmail());
                    c.setIsActive(true);
                    c.setPoints(0.0);
                    c.setTotalSpend(0.0);
                    c.setMembershipRank("Đồng");
                    c.setCreatedBy("SYSTEM");
                    c = customerRepository.save(c);
                }
            }
        }
        if (c == null) {
            throw new ResourceNotFoundException("Customer", "id", id);
        }
        return c;
    }

    @Override
    public UpdateCustomerResponse updateCustomer(Long id, UpdateCustomerRequest req) {
        Customer c = getOrCreateCustomerForUser(id);

        c.setName(req.getName());
        c.setPhone(req.getPhone());
        c.setEmail(req.getEmail());
        c.setAddress(req.getAddress());
        if (req.getIsActive() != null) c.setIsActive(req.getIsActive());

        c.setUpdatedBy(getCurrentUsername());
        c.setUpdatedAt(LocalDateTime.now());
        if (req.getMembershipRank() != null) {
            c.setMembershipRank(req.getMembershipRank());
        }
        if (req.getPoints() != null) {
            c.setPoints(req.getPoints());
        }
        if (req.getTotalSpend() != null) {
            c.setTotalSpend(req.getTotalSpend());
        }

        c.setDob(req.getDob());
        c.setTaxCode(req.getTaxCode());
        c.setGender(req.getGender());
        c.setNote(req.getNote());

        if (req.getGroupId() != null) {
            PartnerGroup group = partnerGroupRepository.findById(req.getGroupId()).orElse(null);
            c.setGroup(group);
        }
        if (req.getAreaId() != null) {
            Area area = areaRepository.findById(req.getAreaId()).orElse(null);
            c.setArea(area);
        }

        if (req.getAvatar() != null && !req.getAvatar().isEmpty()) {
            try {
                c.setAvatarUrl(cloudinaryService.uploadFile(req.getAvatar(), "customers").getImageUrl());
            } catch (java.io.IOException e) {
                throw new RuntimeException("Lỗi tải ảnh lên Cloudinary", e);
            }
        } else if (req.getAvatarUrl() != null && !req.getAvatarUrl().isBlank()) {
            c.setAvatarUrl(req.getAvatarUrl().trim());
        }
        Customer saved = customerRepository.save(c);

        return UpdateCustomerResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .phone(saved.getPhone())
                .email(saved.getEmail())
                .address(saved.getAddress())
                .avatarUrl(saved.getAvatarUrl())
                .membershipRank(saved.getMembershipRank())
                .points(saved.getPoints())
                .totalSpend(saved.getTotalSpend())
                .updatedAt(saved.getUpdatedAt())
                .updatedBy(saved.getUpdatedBy())
                .isActive(saved.getIsActive())
                .dob(saved.getDob())
                .taxCode(saved.getTaxCode())
                .gender(saved.getGender())
                .note(saved.getNote())
                .groupId(saved.getGroup() != null ? saved.getGroup().getId() : null)
                .areaId(saved.getArea() != null ? saved.getArea().getId() : null)
                .message("Cập nhật thành công")
                .build();
    }

    @Override
    public UpdateCustomerResponse updateStatus(Long id, Boolean isActive) {
        Customer c = getOrCreateCustomerForUser(id);
        c.setIsActive(isActive);
        c.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(c);
        return UpdateCustomerResponse.builder()
                .id(c.getId())
                .isActive(c.getIsActive())
                .message("Cập nhật trạng thái thành công").build();
    }

    @Override
    public DeleteCustomerResponse deleteCustomer(Long id) {
        Customer c = getOrCreateCustomerForUser(id);

        if (Boolean.TRUE.equals(c.getIsDeleted())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Khách hàng này đã bị xóa rồi!");
        }

        c.setIsDeleted(true);
        c.setIsActive(false);
        c.setDeletedAt(LocalDateTime.now());
        c.setDeletedBy(getCurrentUsername());

        customerRepository.save(c);

        return DeleteCustomerResponse.builder()
                .id(c.getId())
                .message("Tài khoản khách hàng đã được chuyển sang trạng thái không hoạt động")
                .deletedAt(c.getDeletedAt())
                .deletedBy(c.getDeletedBy())
                .build();
    }

    @Override
    public CustomerDetailResponse getCustomerById(Long id) {
        Customer c = getOrCreateCustomerForUser(id);
        return CustomerDetailResponse.builder()
                .id(c.getId())
                .customerCode(c.getCustomerCode())
                .name(c.getName())
                .phone(c.getPhone())
                .email(c.getEmail())
                .address(c.getAddress())
                .avatarUrl(c.getAvatarUrl())
                .isActive(c.getIsActive())
                .membershipRank(c.getMembershipRank())
                .points(c.getPoints())
                .totalSpend(c.getTotalSpend())
                .createdBy(c.getCreatedBy())
                .createdAt(c.getCreatedAt())
                .deletedAt(c.getDeletedAt())
                .deletedBy(c.getDeletedBy())
                .dob(c.getDob())
                .taxCode(c.getTaxCode())
                .gender(c.getGender())
                .note(c.getNote())
                .groupId(c.getGroup() != null ? c.getGroup().getId() : null)
                .areaId(c.getArea() != null ? c.getArea().getId() : null)
                .build();
    }

    @Override
    public Page<CustomerListResponse> getAllCustomers(int page, int size, Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size);

        // 1. Nếu không lọc (null): Lấy tất cả trừ những cái đã xóa hẳn (is_deleted = true)
        if (isActive == null) {
            return customerRepository.findByIsDeletedFalse(pageable)
                    .map(this::mapToListResponse);
        }
        // 2. Nếu có lọc: Chỉ cần lọc theo isActive là được
        else {
            return customerRepository.findByIsActive(isActive, pageable)
                    .map(this::mapToListResponse);
        }
    }

    private CustomerListResponse mapToListResponse(Customer c) {
        return CustomerListResponse.builder()
                .id(c.getId())
                .customerCode(c.getCustomerCode())
                .name(c.getName())
                .phone(c.getPhone())
                .email(c.getEmail())
                .address(c.getAddress())
                .membershipRank(c.getMembershipRank())
                .points(c.getPoints() != null ? c.getPoints() : 0.0)
                .totalSpend(c.getTotalSpend() != null ? c.getTotalSpend() : 0.0)
                .isActive(c.getIsActive())
                .avatarUrl(c.getAvatarUrl())
                .build();
    }

    @Override public List<SalesHistoryResponse> getSalesHistory(Long id) { return Collections.emptyList(); }
    @Override public List<DebtResponse> getCustomerDebts(Long id) { return Collections.emptyList(); }
    @Override public String importCustomers(MultipartFile file) { return "OK"; }
    @Override public byte[] exportCustomers() { return new byte[0]; }

    @Override
    public void resetCustomerPassword(Long id, String newPassword) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        String pwd = (newPassword != null && !newPassword.trim().isEmpty()) ? newPassword.trim() : "RetailHub@123";
        c.setPassword(passwordEncoder.encode(pwd));
        c.setMustChangePassword(true);
        customerRepository.save(c);
    }

    @Override
    public void changeCustomerPassword(Long id, String oldPassword, String newPassword) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        if (c.getPassword() != null && !passwordEncoder.matches(oldPassword, c.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu cũ không chính xác!");
        }
        c.setPassword(passwordEncoder.encode(newPassword));
        c.setMustChangePassword(false);
        customerRepository.save(c);
    }
}