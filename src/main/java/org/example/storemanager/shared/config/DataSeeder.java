package org.example.storemanager.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.modules.crm.entity.LoyaltyTier;
import org.example.storemanager.modules.crm.repository.LoyaltyTierRepository;
import org.example.storemanager.modules.system.entity.Permission;
import org.example.storemanager.modules.system.entity.Role;
import org.example.storemanager.modules.system.entity.RolePermission;
import org.example.storemanager.modules.system.entity.User;
import org.example.storemanager.modules.system.repository.PermissionRepository;
import org.example.storemanager.modules.system.repository.RolePermissionRepository;
import org.example.storemanager.modules.system.repository.RoleRepository;
import org.example.storemanager.modules.system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.example.storemanager.modules.sales.repository.SaleOrderRepository;
import org.example.storemanager.modules.sales.repository.SaleOrderDetailRepository;
import org.example.storemanager.modules.sales.entity.SaleOrder;
import org.example.storemanager.modules.sales.entity.SaleOrderDetail;
import org.example.storemanager.modules.finance.entity.BankAccount;
import org.example.storemanager.modules.finance.entity.TransactionReason;
import org.example.storemanager.modules.finance.entity.ReceiptVoucher;
import org.example.storemanager.modules.finance.entity.PaymentVoucher;
import org.example.storemanager.modules.finance.repository.BankAccountRepository;
import org.example.storemanager.modules.finance.repository.TransactionReasonRepository;
import org.example.storemanager.modules.finance.repository.ReceiptVoucherRepository;
import org.example.storemanager.modules.finance.repository.PaymentVoucherRepository;
import org.example.storemanager.modules.marketing.entity.Banner;
import org.example.storemanager.modules.marketing.repository.BannerRepository;
import org.example.storemanager.modules.catalog.entity.ProductCategory;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.catalog.repository.CategoriesRepository;
import org.example.storemanager.modules.catalog.repository.ProductRepository;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.system.repository.BranchRepository;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final LoyaltyTierRepository loyaltyTierRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final SaleOrderDetailRepository saleOrderDetailRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationContext applicationContext;
    private final BranchRepository branchRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionReasonRepository transactionReasonRepository;
    private final ReceiptVoucherRepository receiptVoucherRepository;
    private final PaymentVoucherRepository paymentVoucherRepository;
    private final BannerRepository bannerRepository;
    private final CategoriesRepository categoriesRepository;
    private final ProductRepository productRepository;

    private static final Pattern PERMISSION_PATTERN = Pattern.compile("hasPermission\\s*\\(\\s*'([^']+)'\\s*\\)");

    @Override
    public void run(String... args) {
        try {
            seedInitialData();
        } catch (Exception e) {
            log.error("[DataSeeder] Quá trình seed dữ liệu khởi tạo gặp sự cố (ứng dụng vẫn tiếp tục khởi động bình thường): {}", e.getMessage());
        }
    }

    private void seedInitialData() {
        seedPermissions();
        Role superAdminRole = seedSuperAdminRole();
        if (superAdminRole != null) {
            seedSuperAdminPermissions(superAdminRole);
            seedDefaultStaffRoles(superAdminRole);
        }
        log.info("[DataSeeder] Hoàn tất kiểm tra dữ liệu khởi tạo.");
    }

    private void seedPermissions() {
        try {
            Set<String> scannedPermissionCodes = scanControllerPermissions();

            List<String> fePermissions = List.of(
                "pos:terminal:access", "pos:session:view", "pos:session:open", "pos:session:close",
                "pos:payment:process", "pos:order:discount", "pos:order:cancel", "pos:inventory:negative-sell",
                "pos:price:override", "pos:branch:change", "pos:payment-method:view", "sales:order:view",
                "sales:online-order:view", "sales:quote:view", "sales:offer:view", "sales:invoice-retail:view",
                "sales:market-order:view", "sales:invoice:view", "sales:payment:view", "sales:receivable:view",
                "sales:invoice-list:view", "sales:delivery-list:view", "sales:delivery-note:view",
                "sales:return:view", "sales:return-request:view", "sales:return-history:view",
                "catalog:product:view", "inventory:product-detail:view", "inventory:variant:view",
                "catalog:combo:view", "inventory:batch:view", "inventory:serial:view", "catalog:category:view",
                "catalog:unit:view", "catalog:color:view", "catalog:size:view", "inventory:supplier-product:view",
                "inventory:dashboard:view", "inventory:product-storage:view", "inventory:product-warehouse:view",
                "inventory:ledger:view", "inventory:adjustment:view", "inventory:check:view",
                "inventory:stock-keeping:view", "inventory:mobile:view", "inventory:import:view",
                "inventory:stock-out:view", "inventory:transfer:view", "inventory:transfer-list:view",
                "inventory:transfer-request:view", "inventory:return-supplier:view", "inventory:cancel:view",
                "inventory:storage-area:view", "inventory:warehouse-area:view", "inventory:warehouse-zone:view",
                "inventory:warehouse-bin:view", "inventory:supplier-storage:view", "inventory:supplier-warehouse:view",
                "purchase:supplier:view", "purchase:order:view", "purchase:request:view", "purchase:contract:view",
                "purchase:evaluation:view", "purchase:supplier-request:view", "purchase:delivery:view",
                "purchase:invoice:view", "purchase:payment:view", "purchase:return-list:view",
                "purchase:return-history:view", "finance:receipt:view", "finance:payment:view",
                "finance:debt:view", "finance:fund-balance:view", "finance:bank:view", "finance:order-payment:view",
                "finance:journal:view", "finance:transaction-reason:view", "finance:tax-duty:view",
                "finance:cost:view", "finance:chart-of-accounts:view", "finance:fixed-asset:view",
                "finance:cost-center:view", "finance:depreciation-history:view", "crm:customer:view",
                "crm:tier:view", "crm:voucher:view", "crm:customer-voucher:view", "crm:feedback:view",
                "crm:ticket:view", "crm:ticket-message:view", "crm:warranty:view", "crm:warranty-claim:view",
                "crm:partner-group:view", "crm:area:view", "crm:loyalty-history:view", "crm:campaign:view",
                "logistics:shipper:view", "logistics:trip:view", "logistics:price:view", "logistics:promotion:view",
                "logistics:carrier:view", "logistics:method:view", "logistics:charge:view", "logistics:fee:view",
                "logistics:fee-rate:view", "logistics:fee-group:view", "logistics:shipment:view",
                "logistics:note:view", "logistics:order:view", "logistics:location:view", "logistics:contact:view",
                "logistics:address:view", "logistics:batch:view", "logistics:packing-list:view",
                "logistics:delivery-note:view", "reports:sales:view", "reports:inventory:view",
                "reports:finance:view", "reports:crm:view", "system:user:view", "system:role:view",
                "catalog:department:view", "hr:position:view", "hr:log:view", "hr:contract:view",
                "hrm:attendance:view", "hr:leave-request:view", "hr:shift-swap:view", "hr:kpi:view",
                "hr:payroll:view", "system:branch:view", "system:settings:view", "system:config:view",
                "system:vat:view", "system:template:view", "system:notification:view", "system:error-log:view",
                "system:banner:view", "system:permission:view", "system:device-session:view", "system:password-history:view"
            );
            scannedPermissionCodes.addAll(fePermissions);

            List<Permission> pendingPermissions = new ArrayList<>();
            for (String code : scannedPermissionCodes) {
                if (!permissionRepository.existsByPermissionCode(code)) {
                    String moduleName = determineModuleFromCode(code);
                    String description = "Quyền truy cập chức năng " + code;

                    Permission perm = Permission.builder()
                            .permissionCode(code)
                            .module(moduleName)
                            .description(description)
                            .isActive(true)
                            .build();
                    perm.setIsDeleted(false);
                    pendingPermissions.add(perm);
                }
            }

            if (!pendingPermissions.isEmpty()) {
                permissionRepository.saveAllAndFlush(pendingPermissions);
                log.info("Đã lưu thành công {} quyền mới vào Database.", pendingPermissions.size());
            }
        } catch (Exception e) {
            log.warn("[DataSeeder] Không thể đồng bộ permissions (non-fatal): {}", e.getMessage());
        }
    }

    private Role seedSuperAdminRole() {
        try {
            Role superAdminRole = roleRepository.findByRoleName("SUPER_ADMIN").orElse(null);
            if (superAdminRole == null) {
                superAdminRole = Role.builder()
                        .roleName("SUPER_ADMIN")
                        .description("Vai trò quản trị tối cao của toàn bộ hệ thống")
                        .isActive(true)
                        .build();
                superAdminRole.setIsDeleted(false);
                superAdminRole = roleRepository.saveAndFlush(superAdminRole);
                log.info("Đã tạo mới vai trò SUPER_ADMIN mặc định.");
            }
            return superAdminRole;
        } catch (Exception e) {
            log.warn("[DataSeeder] Không thể khởi tạo vai trò SUPER_ADMIN (non-fatal): {}", e.getMessage());
            return null;
        }
    }

    private void seedSuperAdminPermissions(Role superAdminRole) {
        try {
            List<Permission> allPermissions = permissionRepository.findAll();
            List<RolePermission> currentRolePermissions = rolePermissionRepository.findByRoleId(superAdminRole.getId());
            Set<Long> currentPermissionIds = currentRolePermissions.stream()
                    .map(rp -> rp.getPermission().getId())
                    .collect(Collectors.toSet());

            List<RolePermission> newPermissionsToAssign = new ArrayList<>();
            for (Permission perm : allPermissions) {
                if (!currentPermissionIds.contains(perm.getId())) {
                    RolePermission newRp = RolePermission.builder()
                            .role(superAdminRole)
                            .permission(perm)
                            .build();
                    newRp.setIsDeleted(false);
                    newPermissionsToAssign.add(newRp);
                }
            }

            if (!newPermissionsToAssign.isEmpty()) {
                rolePermissionRepository.saveAllAndFlush(newPermissionsToAssign);
                log.info("Đã gán bổ sung {} quyền mới cho vai trò SUPER_ADMIN.", newPermissionsToAssign.size());
            }
        } catch (Exception e) {
            log.warn("[DataSeeder] Không thể gán quyền cho SUPER_ADMIN (non-fatal): {}", e.getMessage());
        }
    }

    private void seedDefaultStaffRoles(Role superAdminRole) {
        try {
            List<String> defaultStaffPermissionCodes = List.of(
                "pos:terminal:access", "pos:session:view", "pos:session:open", "pos:session:close",
                "pos:payment:process", "pos:order:discount", "pos:order:cancel", "pos:inventory:negative-sell",
                "pos:price:override", "catalog:product:view", "catalog:category:view", "catalog:pricelist:view",
                "sales:invoice:view", "sales:invoice:create", "crm:customer:view"
            );

            final Long targetSuperAdminId = superAdminRole.getId();
            List<Role> nonSuperAdminRoles = roleRepository.findAll().stream()
                    .filter(r -> !r.getId().equals(targetSuperAdminId))
                    .collect(Collectors.toList());

            for (Role roleItem : nonSuperAdminRoles) {
                List<RolePermission> existingPerms = rolePermissionRepository.findByRoleId(roleItem.getId());
                if (existingPerms.isEmpty()) {
                    List<Permission> defaultPerms = permissionRepository.findByPermissionCodeIn(defaultStaffPermissionCodes);
                    List<RolePermission> newRolePerms = defaultPerms.stream()
                            .map(p -> {
                                RolePermission rp = RolePermission.builder()
                                        .role(roleItem)
                                        .permission(p)
                                        .build();
                                rp.setIsDeleted(false);
                                return rp;
                            })
                            .collect(Collectors.toList());
                    if (!newRolePerms.isEmpty()) {
                        rolePermissionRepository.saveAllAndFlush(newRolePerms);
                        log.info("Đã gán tự động {} quyền mặc định cho vai trò [{}].", newRolePerms.size(), roleItem.getRoleName());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[DataSeeder] Không thể cập nhật quyền mặc định cho staff roles (non-fatal): {}", e.getMessage());
        }
    }

    /**
     * Quét tất cả các @RestController bean trong ApplicationContext,
     * tìm tất cả method có @PreAuthorize("hasPermission('...')") và trích xuất mã quyền.
     */
    private Set<String> scanControllerPermissions() {
        Set<String> permissionCodes = new LinkedHashSet<>();
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);

        for (Object bean : controllers.values()) {
            Class<?> targetClass = bean.getClass();
            // Unwrap CGLIB proxy nếu có
            while (targetClass.getSimpleName().contains("$$")) {
                targetClass = targetClass.getSuperclass();
            }

            for (Method method : targetClass.getDeclaredMethods()) {
                PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
                if (preAuthorize != null) {
                    String expression = preAuthorize.value();
                    Matcher matcher = PERMISSION_PATTERN.matcher(expression);
                    while (matcher.find()) {
                        String permCode = matcher.group(1).trim();
                        if (!permCode.isEmpty()) {
                            permissionCodes.add(permCode);
                        }
                    }
                }
            }
        }
        return permissionCodes;
    }

    /**
     * Xác định tên Module từ mã quyền.
     * Ví dụ: "catalog:product:create" -> "Catalog"
     *         "inventory:import:approve" -> "Inventory"
     */
    private String determineModuleFromCode(String code) {
        if (code == null || code.isBlank()) return "General";
        String[] parts = code.split("[:_\\-]");
        if (parts.length == 0) return "General";

        String prefix = parts[0].toLowerCase();
        return switch (prefix) {
            case "catalog"          -> "Catalog - Danh mục sản phẩm";
            case "inventory"        -> "Inventory - Kho hàng";
            case "purchase"         -> "Purchase - Mua hàng";
            case "sales"            -> "Sales - Bán hàng";
            case "pos"              -> "POS - Bán hàng tại quầy";
            case "crm"              -> "CRM - Khách hàng";
            case "hrm"              -> "HRM - Nhân sự";
            case "finance"          -> "Finance - Tài chính";
            case "wms"              -> "WMS - Quản lý kho vật lý";
            case "system"           -> "System - Hệ thống";
            case "partnerarea"      -> "Partner Area - Đối tác";
            case "report"           -> "Report - Báo cáo";
            case "omnichannel"      -> "Omnichannel - Đa kênh";
            default                 -> capitalize(prefix);
        };

    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
