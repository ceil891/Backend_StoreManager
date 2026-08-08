package org.example.storemanager.modules.partnerarea.service.supplier.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.partnerarea.dto.request.supplier.CreateSupplierRequest;
import org.example.storemanager.modules.partnerarea.dto.response.supplier.*;
import org.example.storemanager.modules.partnerarea.entity.Supplier;
import org.example.storemanager.shared.exception.DuplicateResourceException;
import org.example.storemanager.modules.partnerarea.repository.SupplierRepository;
import org.example.storemanager.modules.partnerarea.service.supplier.SupplierService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository repository;

    @Override
    public Page<SupplierListResponse> getAll(Boolean isActive, Pageable pageable) {
        Page<Supplier> suppliers = (isActive == null) ? repository.findAll(pageable) : repository.findByIsActive(isActive, pageable);
        return suppliers.map(s -> SupplierListResponse.builder()
                .id(s.getId())
                .supplierCode(s.getSupplierCode())
                .name(s.getName())
                .category(s.getCategory())
                .contactPerson(s.getContactPerson())
                .phone(s.getPhone())
                .email(s.getEmail())
                .address(s.getAddress())
                .taxCode(s.getTaxCode())
                .groupId(s.getGroup() != null ? s.getGroup().getId() : null)
                .areaId(s.getArea() != null ? s.getArea().getId() : null)
                .creditLimit(s.getCreditLimit())
                .isActive(s.getIsActive())
                .createdBy(s.getCreatedBy())
                .createdAt(s.getCreatedAt())
                .build());
    }

    @Override
    public SupplierDetailResponse getById(Long id) {
        Supplier s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp với ID: " + id));

        return SupplierDetailResponse.builder()
                .id(s.getId())
                .supplierCode(s.getSupplierCode())
                .name(s.getName())
                .category(s.getCategory())
                .contactPerson(s.getContactPerson())
                .phone(s.getPhone())
                .email(s.getEmail())
                .address(s.getAddress())
                .taxCode(s.getTaxCode())
                .paymentTerm(s.getPaymentTerm())
                .creditLimit(s.getCreditLimit())
                .bankName(s.getBankName())
                .bankAccount(s.getBankAccount())
                .accountHolder(s.getAccountHolder())
                .description(s.getDescription())
                .isActive(s.getIsActive())
                .createdBy(s.getCreatedBy())
                .createdAt(s.getCreatedAt())
                .updatedBy(s.getUpdatedBy())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) ? auth.getName() : "SYSTEM";
    }

    @Override
    public CreateSupplierResponse create(CreateSupplierRequest req) {
        String email = req.getEmail() != null ? req.getEmail().trim() : "";
        String phone = req.getPhone() != null ? req.getPhone().trim() : "";

        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("Email không đúng định dạng");
        }
        if (!phone.isEmpty() && !phone.matches("^[0-9]{10,11}$")) {
            throw new RuntimeException("Số điện thoại không hợp lệ (phải từ 10-11 chữ số)");
        }

        // Check trùng lặp chỉ khi có dữ liệu
        if (!phone.isEmpty() && repository.existsByPhone(phone)) {
            throw new DuplicateResourceException("Số điện thoại đã tồn tại", "ERR_PHONE", phone);
        }
        if (!email.isEmpty() && repository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email đã tồn tại", "ERR_EMAIL", email);
        }

        if (req.getBankName() != null && req.getBankAccount() != null 
                && !req.getBankName().trim().isEmpty() && !req.getBankAccount().trim().isEmpty()) {
            if (repository.existsByBankNameAndBankAccount(req.getBankName(), req.getBankAccount())) {
                throw new DuplicateResourceException("Ngân hàng này đã đăng ký số tài khoản này", "ERR_BANK", req.getBankAccount());
            }
        }

        // Tạo code nếu chưa có
        String supplierCode = req.getSupplierCode();
        if (supplierCode == null || supplierCode.trim().isEmpty()) {
            String year = String.valueOf(java.time.LocalDate.now().getYear());
            long count = repository.count() + 1;
            supplierCode = "SUP-" + year + "-" + String.format("%03d", count);
        }

        // Build Entity
        Supplier s = Supplier.builder()
                .supplierCode(supplierCode)
                .name(req.getName())
                .category(req.getCategory() != null ? req.getCategory() : "GENERAL")
                .contactPerson(req.getContactPerson())
                .phone(phone)
                .email(email)
                .address(req.getAddress())
                .taxCode(req.getTaxCode())
                .paymentTerm(req.getPaymentTerm())
                .creditLimit(req.getCreditLimit())
                .bankName(req.getBankName())
                .bankAccount(req.getBankAccount())
                .accountHolder(req.getAccountHolder())
                .description(req.getDescription())
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .build();

        s.setCreatedBy(getCurrentUsername());
        Supplier saved = repository.save(s);

        return CreateSupplierResponse.builder()
                .id(saved.getId())
                .supplierCode(saved.getSupplierCode())
                .name(saved.getName())
                .category(saved.getCategory())
                .contactPerson(saved.getContactPerson())
                .phone(saved.getPhone())
                .email(saved.getEmail())
                .address(saved.getAddress())
                .taxCode(saved.getTaxCode())
                .paymentTerm(saved.getPaymentTerm())
                .creditLimit(saved.getCreditLimit())
                .isActive(saved.getIsActive())
                .createdAt(saved.getCreatedAt())
                .createdBy(saved.getCreatedBy())
                .description(saved.getDescription())
                .build();
    }

    @Override
    public UpdateSupplierResponse update(Long id, CreateSupplierRequest req) {
        Supplier s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp id=" + id));

        String email = req.getEmail() != null ? req.getEmail().trim() : "";
        String phone = req.getPhone() != null ? req.getPhone().trim() : "";

        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("Email không đúng định dạng");
        }
        if (!phone.isEmpty() && !phone.matches("^[0-9]{10,11}$")) {
            throw new RuntimeException("Số điện thoại không hợp lệ");
        }

        // Check trùng lặp nếu phone/email thay đổi sang của người khác
        if (!phone.isEmpty() && !phone.equals(s.getPhone()) && repository.existsByPhone(phone)) {
            throw new DuplicateResourceException("Số điện thoại đã tồn tại", "ERR_PHONE", phone);
        }
        if (!email.isEmpty() && !email.equals(s.getEmail()) && repository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email đã tồn tại", "ERR_EMAIL", email);
        }

        s.setIsActive(req.getIsActive() != null ? req.getIsActive() : s.getIsActive());
        s.setName(req.getName());
        s.setCategory(req.getCategory());
        s.setContactPerson(req.getContactPerson());
        s.setPhone(phone);
        s.setEmail(email);
        s.setAddress(req.getAddress());
        s.setTaxCode(req.getTaxCode());
        s.setPaymentTerm(req.getPaymentTerm());
        s.setCreditLimit(req.getCreditLimit());
        s.setBankName(req.getBankName());
        s.setBankAccount(req.getBankAccount());
        s.setAccountHolder(req.getAccountHolder());
        s.setDescription(req.getDescription());

        // 3. Audit
        s.setUpdatedBy(getCurrentUsername());
        s.setUpdatedAt(LocalDateTime.now());

        Supplier updated = repository.save(s);

        // 4. Trả về đầy đủ các trường
        return UpdateSupplierResponse.builder()
                .id(updated.getId())
                .supplierCode(updated.getSupplierCode())
                .name(updated.getName())
                .category(updated.getCategory())
                .contactPerson(updated.getContactPerson())
                .phone(updated.getPhone())
                .email(updated.getEmail())
                .address(updated.getAddress())
                .taxCode(updated.getTaxCode())
                .paymentTerm(updated.getPaymentTerm())
                .creditLimit(updated.getCreditLimit())
                .bankName(updated.getBankName())
                .bankAccount(updated.getBankAccount())
                .accountHolder(updated.getAccountHolder())
                .description(updated.getDescription())
                .updatedBy(updated.getUpdatedBy())
                .updatedAt(updated.getUpdatedAt())
                .message("Cập nhật thành công")
                .isActive(updated.getIsActive())

                .build();
    }

    @Override
    public void delete(Long id) {
        // 1. Tìm nhà cung cấp
        Supplier s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp với ID: " + id));

        // 2. Kiểm tra: Nếu vẫn đang hoạt động thì không cho xóa
        if (Boolean.TRUE.equals(s.getIsActive())) {
            throw new RuntimeException("Không thể xóa: Nhà cung cấp đang ở trạng thái hoạt động (isActive = true). Vui lòng tắt hoạt động trước.");
        }

        // 3. Tiến hành xóa mềm (Soft Delete)
        s.setIsDeleted(true);
        s.setDeletedBy(getCurrentUsername());
        s.setDeletedAt(LocalDateTime.now());

        // 4. Lưu lại thay đổi
        repository.save(s);
    }

    @Override
    public void updateStatus(Long id) {
        Supplier s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));

        // Logic tự động đảo trạng thái: Nếu đang true -> false, false -> true
        s.setIsActive(!s.getIsActive());

        s.setUpdatedBy(getCurrentUsername());
        s.setUpdatedAt(LocalDateTime.now());

        repository.save(s);
    }
}