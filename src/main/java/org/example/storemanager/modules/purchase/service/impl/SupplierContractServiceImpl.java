package org.example.storemanager.modules.purchase.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.purchase.dto.request.CreateSupplierContractRequest;
import org.example.storemanager.modules.purchase.dto.request.UpdateSupplierContractRequest;
import org.example.storemanager.modules.purchase.dto.response.SupplierContractResponse;
import org.example.storemanager.modules.common.dto.response.PageResponse;
import org.example.storemanager.modules.warranty.entity.SupplierContract;
import org.example.storemanager.modules.partnerarea.entity.Supplier;
import org.example.storemanager.modules.partnerarea.entity.SupplierProduct;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.warranty.repository.SupplierContractRepository;
import org.example.storemanager.modules.partnerarea.repository.SupplierRepository;
import org.example.storemanager.modules.partnerarea.repository.SupplierProductRepository;
import org.example.storemanager.modules.catalog.repository.ProductRepository;
import org.example.storemanager.modules.purchase.service.SupplierContractService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SupplierContractServiceImpl implements SupplierContractService {

    private final SupplierContractRepository supplierContractRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final ProductRepository productRepository;

    @Override
    public SupplierContractResponse createContract(CreateSupplierContractRequest request) {
        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));

        String username = getCurrentUsername();

        SupplierContract contract = SupplierContract.builder()
                .contractCode(request.getContractCode())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .maxDebtLimit(request.getMaxDebtLimit())
                .status(request.getStatus())
                .supplier(supplier)
                .contractName(request.getContractName())
                .contractType(request.getContractType())
                .signedDate(request.getSignedDate())
                .signedBy(request.getSignedBy())
                .paymentTerm(request.getPaymentTerm())
                .deliveryTerm(request.getDeliveryTerm())
                .attachment(request.getAttachment())
                .build();

        contract.setIsDeleted(false);
        contract.setCreatedBy(username);
        contract.setNote(request.getNote());

        SupplierContract savedContract = supplierContractRepository.save(contract);
        return mapToResponse(savedContract);
    }

    @Override
    public SupplierContractResponse updateContract(Long id, UpdateSupplierContractRequest request) {
        SupplierContract contract = supplierContractRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierContract", "id", id));

        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));

        String username = getCurrentUsername();

        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setMaxDebtLimit(request.getMaxDebtLimit());
        contract.setStatus(request.getStatus());
        contract.setSupplier(supplier);
        contract.setContractName(request.getContractName());
        contract.setContractType(request.getContractType());
        contract.setSignedDate(request.getSignedDate());
        contract.setSignedBy(request.getSignedBy());
        contract.setPaymentTerm(request.getPaymentTerm());
        contract.setDeliveryTerm(request.getDeliveryTerm());
        contract.setAttachment(request.getAttachment());
        contract.setNote(request.getNote());
        contract.setUpdatedBy(username);

        SupplierContract savedContract = supplierContractRepository.save(contract);
        return mapToResponse(savedContract);
    }

    @Override
    public SupplierContractResponse updateStatus(Long id, String status) {
        SupplierContract contract = supplierContractRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierContract", "id", id));

        contract.setStatus(status);
        contract.setUpdatedBy(getCurrentUsername());

        SupplierContract savedContract = supplierContractRepository.save(contract);
        return mapToResponse(savedContract);
    }

    @Override
    public void deleteContract(Long id) {
        SupplierContract contract = supplierContractRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierContract", "id", id));

        String username = getCurrentUsername();
        contract.setIsDeleted(true);
        contract.setDeletedBy(username);
        contract.setDeletedAt(LocalDateTime.now());
        supplierContractRepository.save(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierContractResponse getContractById(Long id) {
        SupplierContract contract = supplierContractRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierContract", "id", id));
        return mapToResponse(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierContractResponse> getAllContracts(String search, String status, Long supplierId, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sorting);
        Page<SupplierContract> pageResult = supplierContractRepository.findAllContracts(search, status, supplierId, includeDeleted, pageable);
        return pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupplierContractResponse> getContractsPaginated(String search, String status, Long supplierId, int page, int size, String sort, boolean includeDeleted) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<SupplierContract> pageResult = supplierContractRepository.findAllContracts(search, status, supplierId, includeDeleted, pageable);

        List<SupplierContractResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<SupplierContractResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    @Override
    public SupplierContractResponse submitContract(Long id) {
        return updateStatus(id, "PENDING_APPROVAL");
    }

    @Override
    public SupplierContractResponse approveContract(Long id) {
        return updateStatus(id, "ACTIVE");
    }

    @Override
    public SupplierContractResponse activateContract(Long id) {
        return updateStatus(id, "ACTIVE");
    }

    @Override
    public SupplierContractResponse terminateContract(Long id) {
        return updateStatus(id, "TERMINATED");
    }

    @Override
    public SupplierContractResponse renewContract(Long id, LocalDate newEndDate) {
        SupplierContract contract = supplierContractRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierContract", "id", id));

        contract.setEndDate(newEndDate);
        contract.setRenewalDate(LocalDate.now());
        contract.setStatus("RENEWED");
        contract.setUpdatedBy(getCurrentUsername());

        SupplierContract savedContract = supplierContractRepository.save(contract);
        return mapToResponse(savedContract);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierContractResponse> getActiveContracts() {
        List<SupplierContract> list = supplierContractRepository.findActiveContracts();
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierContractResponse> getExpiringContracts(int days) {
        LocalDate limitDate = LocalDate.now().plusDays(days);
        List<SupplierContract> list = supplierContractRepository.findExpiringContracts(limitDate);
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getContractProducts(Long id) {
        SupplierContract contract = supplierContractRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierContract", "id", id));

        List<SupplierProduct> supplierProducts = supplierProductRepository.findBySupplier_IdAndIsDeletedFalse(contract.getSupplier().getId());
        return supplierProducts.stream()
                .map(SupplierProduct::getProduct)
                .collect(Collectors.toList());
    }

    @Override
    public void addContractProduct(Long id, Long productId) {
        SupplierContract contract = supplierContractRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierContract", "id", id));

        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Supplier supplier = contract.getSupplier();

        Optional<SupplierProduct> existingOpt = supplierProductRepository.findBySupplier_IdAndProduct_IdAndIsDeletedFalse(supplier.getId(), productId);
        if (existingOpt.isEmpty()) {
            SupplierProduct sp = SupplierProduct.builder()
                    .supplier(supplier)
                    .product(product)
                    .isActive(true)
                    .isPreferred(false)
                    .build();
            sp.setIsDeleted(false);
            sp.setCreatedBy(getCurrentUsername());
            supplierProductRepository.save(sp);
        }
    }

    @Override
    public void removeContractProduct(Long id, Long productId) {
        SupplierContract contract = supplierContractRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierContract", "id", id));

        Optional<SupplierProduct> existingOpt = supplierProductRepository.findBySupplier_IdAndProduct_IdAndIsDeletedFalse(contract.getSupplier().getId(), productId);
        if (existingOpt.isPresent()) {
            SupplierProduct sp = existingOpt.get();
            sp.setIsDeleted(true);
            sp.setDeletedBy(getCurrentUsername());
            sp.setDeletedAt(LocalDateTime.now());
            supplierProductRepository.save(sp);
        }
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Người dùng chưa đăng nhập hoặc token không hợp lệ");
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isEmpty()) {
            return Sort.by("id").descending();
        }
        String[] parts = sortParam.split(",");
        String property = parts[0];
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    private SupplierContractResponse mapToResponse(SupplierContract sc) {
        return SupplierContractResponse.builder()
                .id(sc.getId())
                .contractCode(sc.getContractCode())
                .startDate(sc.getStartDate())
                .endDate(sc.getEndDate())
                .maxDebtLimit(sc.getMaxDebtLimit())
                .status(sc.getStatus())
                .supplierId(sc.getSupplier().getId())
                .supplierName(sc.getSupplier().getName())
                .contractName(sc.getContractName())
                .contractType(sc.getContractType())
                .signedDate(sc.getSignedDate())
                .signedBy(sc.getSignedBy())
                .paymentTerm(sc.getPaymentTerm())
                .deliveryTerm(sc.getDeliveryTerm())
                .attachment(sc.getAttachment())
                .renewalDate(sc.getRenewalDate())
                .note(sc.getNote())
                .createdAt(sc.getCreatedAt())
                .createdBy(sc.getCreatedBy())
                .build();
    }
}
