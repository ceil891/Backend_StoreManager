package org.example.storemanager.modules.purchase.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.purchase.dto.response.PurchaseDashboardSummaryResponse;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.partnerarea.entity.Supplier;
import org.example.storemanager.modules.partnerarea.entity.SupplierProduct;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.catalog.entity.ProductVariant;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.system.entity.User;
import org.example.storemanager.modules.sales.entity.PurchaseOrder;
import org.example.storemanager.modules.sales.entity.PurchaseOrderDetail;
import org.example.storemanager.modules.partnerarea.repository.SupplierRepository;
import org.example.storemanager.modules.partnerarea.repository.SupplierProductRepository;
import org.example.storemanager.modules.catalog.repository.ProductRepository;
import org.example.storemanager.modules.catalog.repository.ProductVariantRepository;
import org.example.storemanager.modules.system.repository.BranchRepository;
import org.example.storemanager.modules.system.repository.UserRepository;
import org.example.storemanager.modules.sales.repository.PurchaseOrderRepository;
import org.example.storemanager.modules.sales.repository.PurchaseOrderDetailRepository;
import org.example.storemanager.modules.wms.repository.PurchaseRequestRepository;
import org.example.storemanager.modules.warranty.repository.SupplierContractRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class PurchaseSupportController {

    private final SupplierRepository supplierRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderDetailRepository purchaseOrderDetailRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final SupplierContractRepository supplierContractRepository;

    @GetMapping("/api/v1/purchase/suppliers")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:view')")
    public ResponseEntity<ApiResponse<List<Supplier>>> getAllSuppliers() {
        List<Supplier> suppliers = supplierRepository.findByIsDeletedFalse();
        return ResponseEntity.ok(ApiResponse.ok(suppliers));
    }

    @GetMapping("/api/v1/purchase/suppliers/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:view')")
    public ResponseEntity<ApiResponse<Supplier>> getSupplierById(@PathVariable Long id) {
        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new org.example.storemanager.shared.exception.ResourceNotFoundException("Supplier", "id", id));
        return ResponseEntity.ok(ApiResponse.ok(supplier));
    }

    @GetMapping("/api/v1/purchase/suppliers/{supplierId}/products")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:view')")
    public ResponseEntity<ApiResponse<List<Product>>> getProductsBySupplier(@PathVariable Long supplierId) {
        List<SupplierProduct> list = supplierProductRepository.findBySupplier_IdAndIsDeletedFalse(supplierId);
        List<Product> products = list.stream()
                .map(SupplierProduct::getProduct)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    @GetMapping("/api/v1/purchase/suppliers/{supplierId}/products/{productId}/price-history")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:view')")
    public ResponseEntity<ApiResponse<List<BigDecimal>>> getPriceHistory(
            @PathVariable Long supplierId,
            @PathVariable Long productId) {
        List<BigDecimal> history = purchaseOrderDetailRepository.findPurchasePriceHistory(supplierId, productId);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    // --- Dropdowns ---

    @GetMapping("/api/v1/purchase/dropdowns/suppliers")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSuppliersDropdown(@RequestParam(required = false) String keyword) {
        List<Supplier> suppliers = supplierRepository.findByIsDeletedFalse();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase().trim();
            suppliers = suppliers.stream().filter(s -> s.getName() != null && s.getName().toLowerCase().contains(kw)).collect(Collectors.toList());
        }

        List<Map<String, Object>> result = suppliers.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("name", s.getName());
            map.put("code", s.getSupplierCode());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/api/v1/purchase/dropdowns/products")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getProductsDropdown(@RequestParam(required = false) String keyword) {
        List<Product> products = productRepository.findByIsDeletedFalse();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase().trim();
            products = products.stream().filter(p -> p.getName() != null && p.getName().toLowerCase().contains(kw)).collect(Collectors.toList());
        }

        List<Map<String, Object>> result = products.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("code", p.getProductCode());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/api/v1/purchase/dropdowns/product-variants")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getVariantsDropdown(@RequestParam(required = false) String keyword) {
        List<ProductVariant> variants = productVariantRepository.findByIsDeletedFalse();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase().trim();
            variants = variants.stream().filter(v -> (v.getSku() != null && v.getSku().toLowerCase().contains(kw)) || 
                                                     (v.getProduct() != null && v.getProduct().getName() != null && v.getProduct().getName().toLowerCase().contains(kw))).collect(Collectors.toList());
        }

        List<Map<String, Object>> result = variants.stream().map(v -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", v.getId());
            map.put("sku", v.getSku());
            map.put("barcode", v.getBarcode());
            map.put("name", v.getProduct() != null ? v.getProduct().getName() : "");
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/api/v1/purchase/dropdowns/branches")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBranchesDropdown() {
        List<Branch> branches = branchRepository.findByIsDeletedFalse();

        List<Map<String, Object>> result = branches.stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("name", b.getBranchName());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/api/v1/purchase/dropdowns/employees")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getEmployeesDropdown() {
        List<User> users = userRepository.findAllUsersIncludeDeleted("", "ACTIVE", null, null, false, org.springframework.data.domain.PageRequest.of(0, 200)).getContent();

        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName() : u.getUsername());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // --- Dashboard ---

    @GetMapping("/api/v1/purchase/dashboard/summary")
    @PreAuthorize("@securityEvaluator.hasPermission('purchase:order:view')")
    public ResponseEntity<ApiResponse<PurchaseDashboardSummaryResponse>> getDashboardSummary() {
        List<PurchaseOrder> orders = purchaseOrderRepository.findByIsDeletedFalse();

        BigDecimal totalSpending = orders.stream()
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()) || "RECEIVED".equalsIgnoreCase(o.getStatus()))
                .map(PurchaseOrder::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingRequests = purchaseRequestRepository.findByIsDeletedFalse().stream()
                .filter(r -> "PENDING_APPROVAL".equalsIgnoreCase(r.getStatus()) || "SUBMITTED".equalsIgnoreCase(r.getStatus()))
                .count();

        long activeContracts = supplierContractRepository.findActiveContracts().size();

        // Top Suppliers
        Map<Supplier, BigDecimal> supplierMap = new HashMap<>();
        for (PurchaseOrder po : orders) {
            BigDecimal amt = po.getTotalAmount() != null ? po.getTotalAmount() : BigDecimal.ZERO;
            if (po.getSupplier() != null) {
                supplierMap.put(po.getSupplier(), supplierMap.getOrDefault(po.getSupplier(), BigDecimal.ZERO).add(amt));
            }
        }

        List<PurchaseDashboardSummaryResponse.SupplierSpending> topSuppliers = supplierMap.entrySet().stream()
                .map(e -> PurchaseDashboardSummaryResponse.SupplierSpending.builder()
                        .supplierId(e.getKey().getId())
                        .supplierName(e.getKey().getName())
                        .amount(e.getValue())
                        .build())
                .sorted(Comparator.comparing(PurchaseDashboardSummaryResponse.SupplierSpending::getAmount).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Top Purchased Products
        List<PurchaseOrderDetail> poDetails = purchaseOrderDetailRepository.findByIsDeletedFalse();

        Map<Product, BigDecimal> productMap = new HashMap<>();
        for (PurchaseOrderDetail pod : poDetails) {
            BigDecimal qty = pod.getQuantity() != null ? pod.getQuantity() : BigDecimal.ZERO;
            if (pod.getProduct() != null) {
                productMap.put(pod.getProduct(), productMap.getOrDefault(pod.getProduct(), BigDecimal.ZERO).add(qty));
            }
        }

        List<PurchaseDashboardSummaryResponse.ProductPurchased> topProducts = productMap.entrySet().stream()
                .map(e -> PurchaseDashboardSummaryResponse.ProductPurchased.builder()
                        .productId(e.getKey().getId())
                        .productName(e.getKey().getName())
                        .quantity(e.getValue())
                        .build())
                .sorted(Comparator.comparing(PurchaseDashboardSummaryResponse.ProductPurchased::getQuantity).reversed())
                .limit(5)
                .collect(Collectors.toList());

        PurchaseDashboardSummaryResponse summary = PurchaseDashboardSummaryResponse.builder()
                .totalSpending(totalSpending)
                .totalOrdersCount((long) orders.size())
                .pendingRequestsCount(pendingRequests)
                .activeContractsCount(activeContracts)
                .topSuppliers(topSuppliers)
                .topProducts(topProducts)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(summary));
    }
}
