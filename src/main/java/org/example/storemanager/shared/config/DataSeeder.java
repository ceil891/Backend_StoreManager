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
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "app.seeder.enabled", havingValue = "true", matchIfMissing = true)
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
    private final org.example.storemanager.modules.hrm.repository.DepartmentRepository hrmDepartmentRepository;
    private final org.example.storemanager.modules.hrm.repository.PositionRepository hrmPositionRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private static final Pattern PERMISSION_PATTERN = Pattern.compile("hasPermission\\s*\\(\\s*'([^']+)'\\s*\\)");

    @Override
    public void run(String... args) {
        try {
            fixNullIsDeleted();
            seedInitialData();
        } catch (Exception e) {
            log.warn("[DataSeeder] Quá trình seed dữ liệu khởi tạo gặp sự cố (ứng dụng vẫn tiếp tục khởi động): {}", e.getMessage());
        }
    }

    private void fixNullIsDeleted() {
        try {
            jdbcTemplate.execute("UPDATE users SET is_deleted = false WHERE is_deleted IS NULL");
            jdbcTemplate.execute("UPDATE roles SET is_deleted = false WHERE is_deleted IS NULL");
            jdbcTemplate.execute("UPDATE departments SET is_deleted = false WHERE is_deleted IS NULL");
            jdbcTemplate.execute("UPDATE positions SET is_deleted = false WHERE is_deleted IS NULL");
        } catch (Exception ignored) {}
    }

    private void seedInitialData() {
        seedPermissions();
        Role superAdminRole = seedSuperAdminRole();
        if (superAdminRole != null) {
            seedSuperAdminPermissions(superAdminRole);
            seedStandardRolesAndPermissions(superAdminRole);
            seedSuperAdminUser(superAdminRole);
            seedSampleUsers(superAdminRole);
        }
        seedDepartmentsAndPositions();
        fixNullIsDeleted();
        log.info("[DataSeeder] Hoàn tất kiểm tra phân quyền và dữ liệu khởi tạo trong Database.");
    }

    private static class RoleSeedDefinition {
        final String roleCode;
        final String description;
        final List<String> permissionCodes;

        RoleSeedDefinition(String roleCode, String description, List<String> permissionCodes) {
            this.roleCode = roleCode;
            this.description = description;
            this.permissionCodes = permissionCodes;
        }
    }

    private void seedStandardRolesAndPermissions(Role superAdminRole) {
        try {
            List<RoleSeedDefinition> standardRoles = List.of(
                new RoleSeedDefinition("STORE_MANAGER", "Cửa hàng trưởng (Store Manager)", List.of(
                    "pos:terminal:access", "pos:session:view", "pos:session:open", "pos:session:close",
                    "pos:payment:process", "pos:order:discount", "pos:order:cancel", "pos:price:override",
                    "pos:branch:change", "sales:order:view", "sales:invoice:view", "catalog:product:view",
                    "inventory:stock-keeping:view", "inventory:dashboard:view", "crm:customer:view",
                    "hr:employee:view", "reports:sales:view", "reports:inventory:view"
                )),
                new RoleSeedDefinition("CASHIER", "Thu ngân / Nhân viên POS", List.of(
                    "pos:terminal:access", "pos:session:view", "pos:session:open", "pos:session:close",
                    "pos:payment:process", "sales:invoice:view", "catalog:product:view", "crm:customer:view"
                )),
                new RoleSeedDefinition("INVENTORY_STAFF", "Nhân viên Thủ kho / Kiểm kê", List.of(
                    "catalog:product:view", "inventory:product-detail:view", "inventory:variant:view",
                    "inventory:batch:view", "inventory:serial:view", "inventory:ledger:view", "inventory:check:view",
                    "inventory:import:view", "inventory:stock-out:view", "inventory:transfer:view",
                    "inventory:transfer-list:view", "inventory:return-supplier:view", "purchase:order:view"
                )),
                new RoleSeedDefinition("ACCOUNTANT", "Kế toán & Tài chính", List.of(
                    "finance:receipt:view", "finance:payment:view", "finance:debt:view", "finance:fund-balance:view",
                    "finance:bank:view", "finance:order-payment:view", "reports:finance:view", "sales:invoice:view",
                    "purchase:invoice:view", "purchase:payment:view"
                )),
                new RoleSeedDefinition("SALES_STAFF", "Nhân viên Kinh doanh / Bán hàng", List.of(
                    "sales:order:view", "sales:quote:view", "sales:offer:view", "sales:invoice:view",
                    "crm:customer:view", "catalog:product:view", "crm:voucher:view"
                )),
                new RoleSeedDefinition("CUSTOMER", "Khách hàng mua sắm Web Online", List.of())
            );

            for (RoleSeedDefinition def : standardRoles) {
                Role role = roleRepository.findByRoleName(def.roleCode).orElse(null);
                if (role == null) {
                    role = Role.builder()
                            .roleName(def.roleCode)
                            .description(def.description)
                            .isActive(true)
                            .build();
                    role.setIsDeleted(false);
                    role = roleRepository.saveAndFlush(role);
                    log.info("[DataSeeder] Đã thêm mới vai trò [{}] vào Database.", def.roleCode);
                }

                // Gán quyền tương ứng
                List<RolePermission> existingPerms = rolePermissionRepository.findByRoleId(role.getId());
                if (existingPerms.isEmpty()) {
                    List<Permission> perms = permissionRepository.findByPermissionCodeIn(def.permissionCodes);
                    List<RolePermission> rps = new ArrayList<>();
                    for (Permission p : perms) {
                        RolePermission rp = RolePermission.builder()
                                .role(role)
                                .permission(p)
                                .build();
                        rp.setIsDeleted(false);
                        rps.add(rp);
                    }
                    if (!rps.isEmpty()) {
                        rolePermissionRepository.saveAllAndFlush(rps);
                        log.info("[DataSeeder] Đã lưu {} quyền vào Database cho vai trò [{}].", rps.size(), def.roleCode);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[DataSeeder] Lỗi khi khởi tạo roles vào Database (non-fatal): {}", e.getMessage());
        }
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

    private void seedSuperAdminUser(Role superAdminRole) {
        try {
            String email = "luuhung261125@storemanager.com";
            String username = "luuhung261125";
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                user = userRepository.findByUsername(username).orElse(null);
            }

            Branch defaultBranch = null;
            try {
                defaultBranch = branchRepository.findByIsDeletedFalse().stream().findFirst().orElse(null);
            } catch (Exception ignored) {}

            if (user == null) {
                user = User.builder()
                        .username(username)
                        .email(email)
                        .password(passwordEncoder.encode("123456"))
                        .fullName("Nguyễn Lưu Hưng (Super Admin)")
                        .phone("0943021105")
                        .status("ACTIVE")
                        .role(superAdminRole)
                        .branch(defaultBranch)
                        .build();
                user.setIsDeleted(false);
                userRepository.saveAndFlush(user);
                log.info("Đã tạo mới tài khoản SuperAdmin [{}] mật khẩu [123456]", email);
            } else {
                user.setRole(superAdminRole);
                user.setPassword(passwordEncoder.encode("123456"));
                user.setStatus("ACTIVE");
                user.setIsDeleted(false);
                if (user.getBranch() == null && defaultBranch != null) {
                    user.setBranch(defaultBranch);
                }
                userRepository.saveAndFlush(user);
                log.info("Đã cập nhật tài khoản [{}] với vai trò SUPER_ADMIN và mật khẩu [123456]", email);
            }
        } catch (Exception e) {
            log.warn("[DataSeeder] Không thể seed tài khoản SuperAdmin (non-fatal): {}", e.getMessage());
        }
    }

    private void seedSampleUsers(Role superAdminRole) {
        try {
            Role storeManagerRole = roleRepository.findByRoleName("STORE_MANAGER").orElse(superAdminRole);
            Role cashierRole = roleRepository.findByRoleName("CASHIER").orElse(superAdminRole);
            Role inventoryRole = roleRepository.findByRoleName("INVENTORY_STAFF").orElse(superAdminRole);
            Role accountantRole = roleRepository.findByRoleName("ACCOUNTANT").orElse(superAdminRole);
            Role salesRole = roleRepository.findByRoleName("SALES_STAFF").orElse(superAdminRole);

            List<Branch> branches = branchRepository.findByIsDeletedFalse();
            Branch b1 = !branches.isEmpty() ? branches.get(0) : null;
            Branch b2 = branches.size() > 1 ? branches.get(1) : b1;
            Branch b3 = branches.size() > 2 ? branches.get(2) : b1;

            var sampleUsers = List.of(
                new Object[]{"nguyenminhquan", "quan.nguyen@storemanager.com", "Nguyễn Minh Quân", "0901234567", storeManagerRole, b1},
                new Object[]{"tranthilan", "lan.tran@storemanager.com", "Trần Thị Lan", "0912345678", storeManagerRole, b2},
                new Object[]{"lehoangnam", "nam.le@storemanager.com", "Lê Hoàng Nam", "0923456789", cashierRole, b1},
                new Object[]{"phamvanhung", "hung.pham@storemanager.com", "Phạm Văn Hùng", "0934567890", inventoryRole, b3},
                new Object[]{"hoangthimai", "mai.hoang@storemanager.com", "Hoàng Thị Mai", "0945678901", accountantRole, b1},
                new Object[]{"dangthithu", "thu.dang@storemanager.com", "Đặng Thị Thu", "0956789012", salesRole, b2}
            );

            for (Object[] row : sampleUsers) {
                String username = (String) row[0];
                String email = (String) row[1];
                String fullName = (String) row[2];
                String phone = (String) row[3];
                Role role = (Role) row[4];
                Branch branch = (Branch) row[5];

                if (userRepository.findByEmail(email).isEmpty() && userRepository.findByUsername(username).isEmpty()) {
                    User u = User.builder()
                            .username(username)
                            .email(email)
                            .password(passwordEncoder.encode("123456"))
                            .fullName(fullName)
                            .phone(phone)
                            .status("ACTIVE")
                            .role(role)
                            .branch(branch)
                            .build();
                    u.setIsDeleted(false);
                    userRepository.save(u);
                    log.info("[DataSeeder] Đã tạo người dùng mẫu [{}] - [{}]", fullName, email);
                }
            }
        } catch (Exception e) {
            log.warn("[DataSeeder] Không thể khởi tạo người dùng mẫu (non-fatal): {}", e.getMessage());
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

    private static class DeptSeed {
        final String code;
        final String name;
        final String desc;
        final List<PosSeed> positions;
        DeptSeed(String code, String name, String desc, List<PosSeed> positions) {
            this.code = code;
            this.name = name;
            this.desc = desc;
            this.positions = positions;
        }
    }

    private static class PosSeed {
        final String code;
        final String name;
        final double salary;
        PosSeed(String code, String name, double salary) {
            this.code = code;
            this.name = name;
            this.salary = salary;
        }
    }

    private void seedDepartmentsAndPositions() {
        try {
            if (hrmDepartmentRepository.count() == 0) {
                var depts = List.of(
                    new DeptSeed("BGD", "Ban Giám Đốc", "Cấp điều hành cao nhất toàn hệ thống", List.of(
                        new PosSeed("CEO", "Tổng Giám Đốc Điều Hành (CEO)", 50000000),
                        new PosSeed("COO", "Giám Đốc Vận Hành (COO)", 40000000),
                        new PosSeed("CFO", "Giám Đốc Tài Chính (CFO)", 40000000)
                    )),
                    new DeptSeed("KD", "Phòng Kinh Doanh & Bán Hàng", "Bộ phận kinh doanh, phát triển thị trường và điểm bán POS", List.of(
                        new PosSeed("SALES_MGR", "Trưởng Phòng Kinh Doanh (Sales Manager)", 25000000),
                        new PosSeed("STORE_SUP", "Trưởng Ca Bán Hàng (Store Supervisor)", 15000000),
                        new PosSeed("CASHIER_POS", "Nhân Viên Thu Ngân & Bán Hàng POS", 9000000),
                        new PosSeed("SALES_CONS", "Chuyên Viên Tư Vấn Bán Hàng", 10000000)
                    )),
                    new DeptSeed("TCKT", "Phòng Tài Chính - Kế Toán", "Quản lý ngân sách, dòng tiền và hạch toán kế toán", List.of(
                        new PosSeed("CHIEF_ACC", "Kế Toán Trưởng (Chief Accountant)", 25000000),
                        new PosSeed("GEN_ACC", "Kế Toán Tổng Hợp & Công Nợ", 14000000),
                        new PosSeed("TREASURER", "Nhân Viên Thủ Quỹ", 10000000)
                    )),
                    new DeptSeed("KHO", "Phòng Kho Vận & Chuỗi Cung Ứng", "Quản lý kho bãi, luân chuyển và giao nhận hàng hóa", List.of(
                        new PosSeed("WMS_MGR", "Trưởng Phòng Kho Vận (Warehouse Manager)", 22000000),
                        new PosSeed("INV_STAFF", "Nhân Viên Thủ Kho & Kiểm Kê", 11000000),
                        new PosSeed("LOG_PACKER", "Nhân Viên Giao Vận & Đóng Gói", 9500000)
                    )),
                    new DeptSeed("IT", "Phòng Công Nghệ Thông Tin", "Quản trị hạ tầng mạng, an ninh và phần mềm RetailHub", List.of(
                        new PosSeed("IT_MGR", "Trưởng Phòng CNTT (IT Manager)", 30000000),
                        new PosSeed("SYS_ADMIN", "Quản Trị Hệ Thống & Mạng (SysAdmin)", 18000000),
                        new PosSeed("SWE", "Kỹ Sư Phần Mềm & RetailHub (SWE)", 20000000)
                    )),
                    new DeptSeed("HR", "Phòng Hành Chính - Nhân Sự", "Quản lý hồ sơ nhân sự, tuyển dụng, C&B và chế độ", List.of(
                        new PosSeed("HR_MGR", "Trưởng Phòng Nhân Sự (HR Manager)", 22000000),
                        new PosSeed("HR_RECRUIT", "Chuyên Viên Tuyển Dụng & C&B", 13000000),
                        new PosSeed("HR_ADMIN", "Nhân Viên Hành Chính Lễ Tân", 9000000)
                    )),
                    new DeptSeed("CRM", "Phòng Chăm Sóc Khách Hàng", "Chăm sóc khách hàng VIP, xử lý khiếu nại và bảo hành", List.of(
                        new PosSeed("CRM_LEAD", "Trưởng Nhóm CSKH (Support Lead)", 16000000),
                        new PosSeed("CRM_AGENT", "Chuyên Viên CSKH & Khiếu Nại", 10000000)
                    ))
                );

                for (var dSeed : depts) {
                    org.example.storemanager.modules.hrm.entity.Department dept = org.example.storemanager.modules.hrm.entity.Department.builder()
                            .deptCode(dSeed.code)
                            .deptName(dSeed.name)
                            .description(dSeed.desc)
                            .build();
                    dept.setIsDeleted(false);
                    var savedDept = hrmDepartmentRepository.save(dept);

                    for (var pSeed : dSeed.positions) {
                        org.example.storemanager.modules.hrm.entity.Position pos = org.example.storemanager.modules.hrm.entity.Position.builder()
                                .positionCode(pSeed.code)
                                .positionName(pSeed.name)
                                .baseSalary(java.math.BigDecimal.valueOf(pSeed.salary))
                                .department(savedDept)
                                .build();
                        pos.setIsDeleted(false);
                        hrmPositionRepository.save(pos);
                    }
                }
                log.info("[DataSeeder] Đã khởi tạo thành công 7 Phòng ban và 20 Chức danh chuẩn vào Database.");
            }
        } catch (Exception e) {
            log.warn("[DataSeeder] Lỗi khi seed phòng ban/chức danh (non-fatal): {}", e.getMessage());
        }
    }
}
