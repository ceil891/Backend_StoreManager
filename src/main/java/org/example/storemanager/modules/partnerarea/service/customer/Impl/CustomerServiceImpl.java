package org.example.storemanager.modules.partnerarea.service.customer.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
    private final org.example.storemanager.modules.sales.repository.SaleOrderRepository saleOrderRepository;
    private final org.example.storemanager.modules.finance.repository.DebtLedgerRepository debtLedgerRepository;
    private final org.example.storemanager.shared.service.EmailService emailService;
    private final org.example.storemanager.modules.system.repository.RefreshTokenRepository refreshTokenRepository;

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
        if (req.getCustomerCode() != null && !req.getCustomerCode().trim().isEmpty()) {
            c.setCustomerCode(req.getCustomerCode().trim());
        } else {
            c.setCustomerCode("CUST-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
        }
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
        c.setDebtLimit(req.getDebtLimit() != null ? req.getDebtLimit() : 0.0);
        
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
                .debtLimit(refreshed.getDebtLimit())
                .groupId(refreshed.getGroup() != null ? refreshed.getGroup().getId() : null)
                .groupName(refreshed.getGroup() != null ? refreshed.getGroup().getGroupName() : null)
                .areaId(refreshed.getArea() != null ? refreshed.getArea().getId() : null)
                .areaName(refreshed.getArea() != null ? refreshed.getArea().getAreaName() : null)
                .message("Tạo thành công").build();
    }

    private Customer getOrCreateCustomerForUser(Long id) {
        Customer c = customerRepository.findByIdAndIsDeletedFalse(id).orElse(null);
        if (c == null) {
            org.example.storemanager.modules.system.entity.User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                String currentUsername = getCurrentUsername();
                if (currentUsername != null && !currentUsername.isBlank() && !"anonymousUser".equalsIgnoreCase(currentUsername)) {
                    user = userRepository.findByUsername(currentUsername)
                            .or(() -> userRepository.findByEmail(currentUsername))
                            .orElse(null);
                }
            }
            if (user != null) {
                if (user.getEmail() != null && !user.getEmail().isBlank()) {
                    c = customerRepository.findByEmailAndIsDeletedFalse(user.getEmail().trim()).orElse(null);
                }
                if (c == null && user.getPhone() != null && !user.getPhone().isBlank()) {
                    c = customerRepository.findByPhoneAndIsDeletedFalse(user.getPhone().replace(" ", "").trim()).orElse(null);
                }
                if (c == null && user.getFullName() != null && !user.getFullName().isBlank()) {
                    c = customerRepository.findByNameIgnoreCaseAndIsDeletedFalse(user.getFullName().trim()).orElse(null);
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

        String oldEmail = c.getEmail();
        String oldPhone = c.getPhone();

        if (req.getCustomerCode() != null && !req.getCustomerCode().trim().isEmpty()) {
            c.setCustomerCode(req.getCustomerCode().trim());
        }
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
        if (req.getDebtLimit() != null) {
            c.setDebtLimit(req.getDebtLimit());
        }

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

        // Đồng bộ dữ liệu với tài khoản User nếu có
        try {
            org.example.storemanager.modules.system.entity.User user = null;
            if (oldEmail != null && !oldEmail.isBlank()) {
                user = userRepository.findByEmail(oldEmail).orElse(null);
            }
            if (user == null && oldPhone != null && !oldPhone.isBlank()) {
                user = userRepository.findByPhone(oldPhone).orElse(null);
            }
            if (user == null && id != null) {
                user = userRepository.findById(id).orElse(null);
            }
            if (user == null) {
                String currentUsername = getCurrentUsername();
                if (currentUsername != null && !currentUsername.isBlank() && !"anonymousUser".equalsIgnoreCase(currentUsername)) {
                    user = userRepository.findByUsername(currentUsername)
                            .or(() -> userRepository.findByEmail(currentUsername))
                            .orElse(null);
                }
            }

            if (user != null) {
                if (saved.getName() != null && !saved.getName().isBlank()) user.setFullName(saved.getName());
                if (saved.getPhone() != null && !saved.getPhone().isBlank()) user.setPhone(saved.getPhone());
                if (saved.getEmail() != null && !saved.getEmail().isBlank()) user.setEmail(saved.getEmail());
                if (saved.getAvatarUrl() != null && !saved.getAvatarUrl().isBlank()) user.setAvatar(saved.getAvatarUrl());
                if (saved.getIsActive() != null) user.setStatus(Boolean.TRUE.equals(saved.getIsActive()) ? "ACTIVE" : "LOCKED");
                userRepository.save(user);
            }
        } catch (Exception e) {
            log.warn("[CustomerService] Failed to sync user account on customer update: {}", e.getMessage());
        }

        return UpdateCustomerResponse.builder()
                .id(saved.getId())
                .customerCode(saved.getCustomerCode())
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
                .debtLimit(saved.getDebtLimit())
                .groupId(saved.getGroup() != null ? saved.getGroup().getId() : null)
                .groupName(saved.getGroup() != null ? saved.getGroup().getGroupName() : null)
                .areaId(saved.getArea() != null ? saved.getArea().getId() : null)
                .areaName(saved.getArea() != null ? saved.getArea().getAreaName() : null)
                .message("Cập nhật thành công")
                .build();
    }

    @Override
    public UpdateCustomerResponse updateStatus(Long id, Boolean isActive) {
        Customer c = getOrCreateCustomerForUser(id);
        c.setIsActive(isActive);
        c.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(c);

        // Đồng bộ trạng thái khóa tài khoản User tương ứng
        try {
            org.example.storemanager.modules.system.entity.User u = null;
            if (id != null) {
                u = userRepository.findByIdAndIsDeletedFalse(id).orElse(null);
            }
            if (u == null && c.getEmail() != null && !c.getEmail().isBlank()) {
                u = userRepository.findByEmailAndIsDeletedFalse(c.getEmail().trim()).orElse(null);
            }
            if (u == null && c.getPhone() != null && !c.getPhone().isBlank()) {
                u = userRepository.findByPhone(c.getPhone().replace(" ", "").trim()).orElse(null);
            }
            if (u != null) {
                u.setStatus(Boolean.TRUE.equals(isActive) ? "ACTIVE" : "LOCKED");
                userRepository.save(u);
                org.example.storemanager.shared.security.SecurityEvaluator.evictUserCache(u.getUsername());
                org.example.storemanager.shared.security.SecurityEvaluator.evictUserCache(u.getEmail());
            }
        } catch (Exception ignored) {}

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
                .debtLimit(c.getDebtLimit())
                .groupId(c.getGroup() != null ? c.getGroup().getId() : null)
                .groupName(c.getGroup() != null ? c.getGroup().getGroupName() : null)
                .areaId(c.getArea() != null ? c.getArea().getId() : null)
                .areaName(c.getArea() != null ? c.getArea().getAreaName() : null)
                .isCreditBlocked(c.getIsCreditBlocked() != null ? c.getIsCreditBlocked() : false)
                .build();
    }

    @Override
    public Page<CustomerListResponse> getAllCustomers(int page, int size, Boolean isActive) {
        return getAllCustomers(page, size, isActive, null);
    }

    @Override
    public Page<CustomerListResponse> getAllCustomers(int page, int size, Boolean isActive, String search) {
        Pageable pageable = PageRequest.of(page, size);

        if (search != null && !search.trim().isEmpty()) {
            return customerRepository.searchAllCustomers(search.trim(), isActive, pageable)
                    .map(this::mapToListResponse);
        }

        // 1. Nếu không lọc (null): Lấy tất cả trừ những cái đã xóa hẳn (is_deleted = true)
        if (isActive == null) {
            return customerRepository.findByIsDeletedFalse(pageable)
                    .map(this::mapToListResponse);
        }
        // 2. Nếu có lọc: Chỉ cần lọc theo isActive là được
        else {
            return customerRepository.findByIsActiveAndIsDeletedFalse(isActive, pageable)
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
                .taxCode(c.getTaxCode())
                .gender(c.getGender())
                .dob(c.getDob())
                .debtLimit(c.getDebtLimit())
                .groupId(c.getGroup() != null ? c.getGroup().getId() : null)
                .groupName(c.getGroup() != null ? c.getGroup().getGroupName() : null)
                .areaId(c.getArea() != null ? c.getArea().getId() : null)
                .areaName(c.getArea() != null ? c.getArea().getAreaName() : null)
                .note(c.getNote())
                .isCreditBlocked(c.getIsCreditBlocked() != null ? c.getIsCreditBlocked() : false)
                .build();
    }

    @Override
    public List<SalesHistoryResponse> getSalesHistory(Long id) {
        Customer c = customerRepository.findByIdAndIsDeletedFalse(id).orElse(null);
        if (c == null) return Collections.emptyList();

        List<org.example.storemanager.modules.sales.entity.SaleOrder> orders =
                saleOrderRepository.findByCustomerIdAndIsDeletedFalseOrderByOrderDateDesc(id);

        return orders.stream().map(so -> SalesHistoryResponse.builder()
                .id(so.getId())
                .invoiceCode(so.getOrderCode())
                .orderDate(so.getOrderDate())
                .totalAmount(so.getFinalAmount() != null ? so.getFinalAmount() : so.getTotalAmount())
                .build()
        ).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<DebtResponse> getCustomerDebts(Long id) {
        Customer c = customerRepository.findByIdAndIsDeletedFalse(id).orElse(null);
        if (c == null) return Collections.emptyList();

        // 1. Check direct debt ledgers
        List<org.example.storemanager.modules.finance.entity.DebtLedger> ledgers =
                debtLedgerRepository.findByPartnerIdAndEntityTypeAndIsDeletedFalseOrderByTransactionDateDesc(id, "CUSTOMER");

        if (!ledgers.isEmpty()) {
            return ledgers.stream().map(dl -> DebtResponse.builder()
                    .id(dl.getId())
                    .amount(dl.getBalance() != null ? dl.getBalance() : dl.getIncrease())
                    .transactionDate(dl.getTransactionDate())
                    .description("Chứng từ: " + (dl.getRefCode() != null ? dl.getRefCode() : "") +
                            (dl.getStatus() != null ? " - " + dl.getStatus() : ""))
                    .build()
            ).collect(java.util.stream.Collectors.toList());
        }

        // 2. If no ledgers, check unpaid or partial sale orders
        List<org.example.storemanager.modules.sales.entity.SaleOrder> unpaidOrders =
                saleOrderRepository.findByCustomerIdAndPaymentStatusNotAndIsDeletedFalseOrderByOrderDateDesc(id, "PAID");

        return unpaidOrders.stream().map(so -> DebtResponse.builder()
                .id(so.getId())
                .amount(so.getFinalAmount() != null ? so.getFinalAmount() : so.getTotalAmount())
                .transactionDate(so.getOrderDate())
                .description("Đơn hàng: " + so.getOrderCode() + " (" + so.getPaymentStatus() + ")")
                .build()
        ).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public CustomerDetailResponse toggleCreditBlock(Long id, Boolean blocked) {
        Customer c = getOrCreateCustomerForUser(id);
        if (blocked != null) {
            c.setIsCreditBlocked(blocked);
        } else {
            c.setIsCreditBlocked(!Boolean.TRUE.equals(c.getIsCreditBlocked()));
        }
        c.setUpdatedBy(getCurrentUsername());
        c.setUpdatedAt(LocalDateTime.now());
        Customer saved = customerRepository.save(c);
        return getCustomerById(saved.getId());
    }

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

        if (c.getEmail() != null && !c.getEmail().isBlank()) {
            String email = c.getEmail().trim();
            userRepository.findByEmailIgnoreCase(email).ifPresent(u -> {
                u.setPassword(passwordEncoder.encode(pwd));
                userRepository.save(u);
                refreshTokenRepository.revokeAllByUser(u);
                org.example.storemanager.shared.security.SecurityEvaluator.evictUserCache(u.getUsername());
                org.example.storemanager.shared.security.SecurityEvaluator.evictUserCache(u.getEmail());
            });

            emailService.sendPasswordResetNotificationEmail(
                    email,
                    c.getName(),
                    c.getCustomerCode() != null ? c.getCustomerCode() : c.getName(),
                    pwd
            );
        }
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