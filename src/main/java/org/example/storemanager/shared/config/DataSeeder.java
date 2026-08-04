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

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final LoyaltyTierRepository loyaltyTierRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationContext applicationContext;

    private static final Pattern PERMISSION_PATTERN = Pattern.compile("hasPermission\\s*\\(\\s*'([^']+)'\\s*\\)");

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // 1. Quét tất cả các RestControllers trong ứng dụng để thu thập các mã quyền từ @PreAuthorize
        Set<String> scannedPermissionCodes = scanControllerPermissions();

        // 2. Thêm các quyền mới phát hiện vào DB nếu chưa tồn tại
        List<Permission> pendingPermissions = new ArrayList<>();
        for (String code : scannedPermissionCodes) {
            if (!permissionRepository.existsByPermissionCode(code)) {
                // Xác định tên Module và mô tả cơ bản từ mã quyền (ví dụ: "catalog:product:create" -> Module "Catalog")
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
                log.info("Phát hiện quyền mới -> chuẩn bị lưu: [{}] - Module: [{}]", code, moduleName);
            }
        }

        if (!pendingPermissions.isEmpty()) {
            permissionRepository.saveAllAndFlush(pendingPermissions);
            log.info("Đã lưu thành công {} quyền mới vào Database.", pendingPermissions.size());
        }

        // 3. Khởi tạo thực thể SUPER_ADMIN (nếu chưa có)
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

        // 4. CẬP NHẬT TỰ ĐỘNG CÁ C QUYỀN MỚI NHẤT CHO SUPER_ADMIN
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

        // 5. TẠO TÀI KHOẢN ADMIN MẶC ĐỊNH
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Quản trị viên hệ thống")
                    .email("admin@storemanager.com")
                    .phone("0999999999")
                    .status("ACTIVE")
                    .role(superAdminRole)
                    .build();
            userRepository.save(adminUser);
            log.info("Đã tạo tài khoản 'admin' mặc định.");
        }

        // 6. KHỞI TẠO HẠNG THÀNH VIÊN LOYALTY MẶC ĐỊNH (NẾU CHƯA CÓ)
        if (loyaltyTierRepository.count() == 0) {
            List<LoyaltyTier> defaultTiers = List.of(
                LoyaltyTier.builder()
                    .tierCode("NEW")
                    .tierName("Thành viên mới")
                    .minPoints(0)
                    .maxPoints(99)
                    .minSpend(BigDecimal.ZERO)
                    .maxSpend(new BigDecimal("999999"))
                    .discountPercent(BigDecimal.ZERO)
                    .pointMultiplier(BigDecimal.ONE)
                    .description("Thành viên vừa đăng ký tài khoản")
                    .benefits("Tài khoản khách hàng chính thức;Nhận thông báo ưu đãi độc quyền")
                    .isDefault(true)
                    .isActive(true)
                    .displayOrder(1)
                    .build(),
                LoyaltyTier.builder()
                    .tierCode("BRONZE")
                    .tierName("Hạng Đồng")
                    .minPoints(100)
                    .maxPoints(499)
                    .minSpend(new BigDecimal("1000000"))
                    .maxSpend(new BigDecimal("4999999"))
                    .discountPercent(BigDecimal.ZERO)
                    .pointMultiplier(BigDecimal.ONE)
                    .description("Tổng chi tiêu từ 1.000.000 đ")
                    .benefits("Tích điểm 1% trên tổng đơn hàng;Quà tặng khi đạt mốc hạng Đồng")
                    .isDefault(false)
                    .isActive(true)
                    .displayOrder(2)
                    .build(),
                LoyaltyTier.builder()
                    .tierCode("SILVER")
                    .tierName("Hạng Bạc")
                    .minPoints(500)
                    .maxPoints(1499)
                    .minSpend(new BigDecimal("5000000"))
                    .maxSpend(new BigDecimal("14999999"))
                    .discountPercent(BigDecimal.ZERO)
                    .pointMultiplier(new BigDecimal("2.0"))
                    .description("Tổng chi tiêu từ 5.000.000 đ")
                    .benefits("Tích điểm 2% trên tổng đơn hàng;Voucher sinh nhật 100.000 đ;Ưu tiên hỗ trợ")
                    .isDefault(false)
                    .isActive(true)
                    .displayOrder(3)
                    .build(),
                LoyaltyTier.builder()
                    .tierCode("GOLD")
                    .tierName("Hạng Vàng")
                    .minPoints(1500)
                    .maxPoints(2999)
                    .minSpend(new BigDecimal("15000000"))
                    .maxSpend(new BigDecimal("29999999"))
                    .discountPercent(BigDecimal.ZERO)
                    .pointMultiplier(new BigDecimal("3.0"))
                    .description("Tổng chi tiêu từ 15.000.000 đ")
                    .benefits("Tích điểm 3% trên tổng đơn hàng;Miễn phí vận chuyển mọi đơn;Voucher sinh nhật 300.000 đ")
                    .isDefault(false)
                    .isActive(true)
                    .displayOrder(4)
                    .build(),
                LoyaltyTier.builder()
                    .tierCode("PLATINUM")
                    .tierName("Hạng Bạch Kim")
                    .minPoints(3000)
                    .maxPoints(5999)
                    .minSpend(new BigDecimal("30000000"))
                    .maxSpend(new BigDecimal("59999999"))
                    .discountPercent(BigDecimal.ZERO)
                    .pointMultiplier(new BigDecimal("4.0"))
                    .description("Tổng chi tiêu từ 30.000.000 đ")
                    .benefits("Tích điểm 4% trên tổng đơn hàng;Tổng đài chăm sóc ưu tiên 24/7;Voucher sinh nhật 500.000 đ")
                    .isDefault(false)
                    .isActive(true)
                    .displayOrder(5)
                    .build(),
                LoyaltyTier.builder()
                    .tierCode("DIAMOND")
                    .tierName("Hạng Kim Cương")
                    .minPoints(6000)
                    .maxPoints(null)
                    .minSpend(new BigDecimal("60000000"))
                    .maxSpend(null)
                    .discountPercent(BigDecimal.ZERO)
                    .pointMultiplier(new BigDecimal("5.0"))
                    .description("Tổng chi tiêu từ 60.000.000 đ")
                    .benefits("Tích điểm 5% trên tổng đơn hàng;Quà tặng VIP độc quyền hàng năm;Voucher sinh nhật 1.000.000 đ;Trợ lý hỗ trợ riêng")
                    .isDefault(false)
                    .isActive(true)
                    .displayOrder(6)
                    .build()
            );
            defaultTiers.forEach(t -> t.setIsDeleted(false));
            loyaltyTierRepository.saveAll(defaultTiers);
            log.info("Đã khởi tạo 6 hạng thành viên Loyalty mặc định vào Database.");
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
