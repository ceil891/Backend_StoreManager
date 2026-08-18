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
    @Transactional
    public void run(String... args) throws Exception {

        // 1. Quét tất cả các RestControllers trong ứng dụng để thu thập các mã quyền từ @PreAuthorize
        Set<String> scannedPermissionCodes = scanControllerPermissions();

        // Bổ sung danh sách các quyền FE & POS đầy đủ vào hệ thống DB backend
        List<String> fePermissions = List.of(
            "pos:terminal:access",
            "pos:session:view",
            "pos:session:open",
            "pos:session:close",
            "pos:payment:process",
            "pos:order:discount",
            "pos:order:cancel",
            "pos:inventory:negative-sell",
            "pos:price:override",
            "pos:branch:change",
            "pos:payment-method:view",
            "sales:order:view",
            "sales:online-order:view",
            "sales:quote:view",
            "sales:offer:view",
            "sales:invoice-retail:view",
            "sales:market-order:view",
            "sales:invoice:view",
            "sales:payment:view",
            "sales:receivable:view",
            "sales:invoice-list:view",
            "sales:delivery-list:view",
            "sales:delivery-note:view",
            "sales:return:view",
            "sales:return-request:view",
            "sales:return-history:view",
            "catalog:product:view",
            "inventory:product-detail:view",
            "inventory:variant:view",
            "catalog:combo:view",
            "inventory:batch:view",
            "inventory:serial:view",
            "catalog:category:view",
            "catalog:unit:view",
            "catalog:color:view",
            "catalog:size:view",
            "inventory:supplier-product:view",
            "inventory:dashboard:view",
            "inventory:product-storage:view",
            "inventory:product-warehouse:view",
            "inventory:ledger:view",
            "inventory:adjustment:view",
            "inventory:check:view",
            "inventory:stock-keeping:view",
            "inventory:mobile:view",
            "inventory:import:view",
            "inventory:stock-out:view",
            "inventory:transfer:view",
            "inventory:transfer-list:view",
            "inventory:transfer-request:view",
            "inventory:return-supplier:view",
            "inventory:cancel:view",
            "inventory:storage-area:view",
            "inventory:warehouse-area:view",
            "inventory:warehouse-zone:view",
            "inventory:warehouse-bin:view",
            "inventory:supplier-storage:view",
            "inventory:supplier-warehouse:view",
            "purchase:supplier:view",
            "purchase:order:view",
            "purchase:request:view",
            "purchase:contract:view",
            "purchase:evaluation:view",
            "purchase:supplier-request:view",
            "purchase:delivery:view",
            "purchase:invoice:view",
            "purchase:payment:view",
            "purchase:return-list:view",
            "purchase:return-history:view",
            "finance:receipt:view",
            "finance:payment:view",
            "finance:debt:view",
            "finance:fund-balance:view",
            "finance:bank:view",
            "finance:order-payment:view",
            "finance:journal:view",
            "finance:transaction-reason:view",
            "finance:tax-duty:view",
            "finance:cost:view",
            "finance:chart-of-accounts:view",
            "finance:fixed-asset:view",
            "finance:cost-center:view",
            "finance:depreciation-history:view",
            "crm:customer:view",
            "crm:tier:view",
            "crm:voucher:view",
            "crm:customer-voucher:view",
            "crm:feedback:view",
            "crm:ticket:view",
            "crm:ticket-message:view",
            "crm:warranty:view",
            "crm:warranty-claim:view",
            "crm:partner-group:view",
            "crm:area:view",
            "crm:loyalty-history:view",
            "crm:campaign:view",
            "logistics:shipper:view",
            "logistics:trip:view",
            "logistics:price:view",
            "logistics:promotion:view",
            "logistics:carrier:view",
            "logistics:method:view",
            "logistics:charge:view",
            "logistics:fee:view",
            "logistics:fee-rate:view",
            "logistics:fee-group:view",
            "logistics:shipment:view",
            "logistics:note:view",
            "logistics:order:view",
            "logistics:location:view",
            "logistics:contact:view",
            "logistics:address:view",
            "logistics:batch:view",
            "logistics:packing-list:view",
            "logistics:delivery-note:view",
            "reports:sales:view",
            "reports:inventory:view",
            "reports:finance:view",
            "reports:crm:view",
            "system:user:view",
            "system:role:view",
            "catalog:department:view",
            "hr:position:view",
            "hr:log:view",
            "hr:contract:view",
            "hrm:attendance:view",
            "hr:leave-request:view",
            "hr:shift-swap:view",
            "hr:kpi:view",
            "hr:payroll:view",
            "system:branch:view",
            "system:settings:view",
            "system:config:view",
            "system:vat:view",
            "system:template:view",
            "system:notification:view",
            "system:error-log:view",
            "system:banner:view",
            "system:permission:view",
            "system:device-session:view",
            "system:password-history:view"
        );
        scannedPermissionCodes.addAll(fePermissions);

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

        // 4b. ĐẢM BẢO CÁC VAI TRÒ KHÔNG PHẢI SUPER_ADMIN (ví dụ: "Nhân viên") CÓ QUYỀN MẶC ĐỊNH NẾU ĐANG RỖNG
        List<String> defaultStaffPermissionCodes = List.of(
            "pos:terminal:access",
            "pos:session:view",
            "pos:session:open",
            "pos:session:close",
            "pos:payment:process",
            "pos:order:discount",
            "pos:order:cancel",
            "pos:inventory:negative-sell",
            "pos:price:override",
            "catalog:product:view",
            "catalog:category:view",
            "catalog:pricelist:view",
            "sales:invoice:view",
            "sales:invoice:create",
            "crm:customer:view"
        );

        final Long targetSuperAdminId = superAdminRole.getId();
        List<Role> nonSuperAdminRoles = roleRepository.findAll().stream()
                .filter(r -> !r.getId().equals(targetSuperAdminId))
                .collect(Collectors.toList());

        for (Role roleItem : nonSuperAdminRoles) {
            List<RolePermission> existingPerms = rolePermissionRepository.findByRoleId(roleItem.getId());
            if (existingPerms.isEmpty()) {
                // Nếu vai trò chưa có quyền nào, tự động gán bộ quyền mặc định
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

        // TẠO / CẬP NHẬT TÀI KHOẢN SUPERADMIN luuhung261125@storemanager.com
        String superAdminEmail = "luuhung261125@storemanager.com";
        String superAdminUsername = "luuhung261125";
        User superUser = userRepository.findByEmail(superAdminEmail)
                .or(() -> userRepository.findByUsername(superAdminUsername))
                .orElse(null);

        if (superUser == null) {
            superUser = User.builder()
                    .username(superAdminUsername)
                    .password(passwordEncoder.encode("123456"))
                    .fullName("Lưu Hưng")
                    .email(superAdminEmail)
                    .phone("0988888888")
                    .status("ACTIVE")
                    .role(superAdminRole)
                    .build();
            userRepository.save(superUser);
            log.info("Đã tạo tài khoản Super Admin [{}] thành công với quyền SUPER_ADMIN.", superAdminEmail);
        } else {
            superUser.setPassword(passwordEncoder.encode("123456"));
            superUser.setRole(superAdminRole);
            superUser.setStatus("ACTIVE");
            userRepository.save(superUser);
            log.info("Đã cập nhật tài khoản Super Admin [{}] với quyền SUPER_ADMIN và mật khẩu mới.", superAdminEmail);
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

        // 7. KHỞI TẠO CHI NHÁNH MẶC ĐỊNH
        Branch branchHN = branchRepository.findByIdAndIsDeletedFalse(1L).orElse(null);
        if (branchHN == null && branchRepository.count() == 0) {
            branchHN = Branch.builder()
                    .branchCode("CN-HN-01")
                    .branchName("Chi nhánh Kho chính Hà Nội")
                    .address("128 Cầu Giấy, Quận Cầu Giấy, Hà Nội")
                    .phone("02438889999")
                    .isActive(true)
                    .build();
            branchHN.setIsDeleted(false);
            branchHN = branchRepository.save(branchHN);

            Branch branchHCM = Branch.builder()
                    .branchCode("CN-HCM-01")
                    .branchName("Chi nhánh Quận 1 TP.HCM")
                    .address("45 Lê Lợi, Phường Bến Nghé, Quận 1, TP.HCM")
                    .phone("02839998888")
                    .isActive(true)
                    .build();
            branchHCM.setIsDeleted(false);
            branchRepository.save(branchHCM);

            Branch branchDN = Branch.builder()
                    .branchCode("CN-DN-01")
                    .branchName("Chi nhánh Hải Châu Đà Nẵng")
                    .address("72 Nguyễn Văn Linh, Quận Hải Châu, Đà Nẵng")
                    .phone("02363778899")
                    .isActive(true)
                    .build();
            branchDN.setIsDeleted(false);
            branchRepository.save(branchDN);
            log.info("Đã khởi tạo 3 chi nhánh mặc định vào Database.");
        }

        // 8. KHỞI TẠO TÀI KHOẢN NGÂN HÀNG & QUỸ TIỀN MẶT
        if (bankAccountRepository.count() == 0) {
            List<BankAccount> defaultBankAccounts = List.of(
                BankAccount.builder()
                    .bankName("Techcombank")
                    .accountNumber("1902838392")
                    .accountHolder("Công ty CP Bán Lẻ StoreManager")
                    .branchName("Hội Sở Ba Đình Hà Nội")
                    .isActive(true)
                    .branch(branchHN)
                    .build(),
                BankAccount.builder()
                    .bankName("Vietcombank")
                    .accountNumber("0918273645")
                    .accountHolder("Công ty CP Bán Lẻ StoreManager")
                    .branchName("Hội Sở Quận 1 TP.HCM")
                    .isActive(true)
                    .branch(branchHN)
                    .build(),
                BankAccount.builder()
                    .bankName("MBBank (Quân Đội)")
                    .accountNumber("888899992026")
                    .accountHolder("StoreManager POS Retail")
                    .branchName("Chi nhánh Đà Nẵng")
                    .isActive(true)
                    .branch(branchHN)
                    .build(),
                BankAccount.builder()
                    .bankName("Quỹ tiền mặt")
                    .accountNumber("CASH-HN")
                    .accountHolder("Quỹ tiền mặt Kho chính Hà Nội")
                    .branchName("Kho chính Hà Nội")
                    .isActive(true)
                    .branch(branchHN)
                    .build(),
                BankAccount.builder()
                    .bankName("Quỹ tiền mặt")
                    .accountNumber("CASH-HCM")
                    .accountHolder("Quỹ tiền mặt Chi nhánh TP.HCM")
                    .branchName("Chi nhánh Quận 1 TP.HCM")
                    .isActive(true)
                    .branch(branchHN)
                    .build()
            );
            defaultBankAccounts.forEach(b -> b.setIsDeleted(false));
            bankAccountRepository.saveAll(defaultBankAccounts);
            log.info("Đã khởi tạo 5 tài khoản ngân hàng & quỹ tiền mặt vào Database.");
        }

        // 9. KHỞI TẠO LÝ DO THU / CHI (TRANSACTION REASONS)
        if (transactionReasonRepository.count() == 0) {
            List<TransactionReason> defaultReasons = List.of(
                TransactionReason.builder().reasonCode("SALES_REVENUE").reasonName("Thu tiền bán hàng (Sales Revenue)").type("RECEIPT").build(),
                TransactionReason.builder().reasonCode("DEBT_COLLECTION").reasonName("Thu hồi công nợ khách hàng").type("RECEIPT").build(),
                TransactionReason.builder().reasonCode("INVESTMENT").reasonName("Thu vốn góp / Đầu tư kinh doanh").type("RECEIPT").build(),
                TransactionReason.builder().reasonCode("FUND_SURPLUS").reasonName("Thu chênh lệch thừa quỹ kiểm kê").type("RECEIPT").build(),
                TransactionReason.builder().reasonCode("SUPPLIER_PAYMENT").reasonName("Thanh toán tiền hàng nhà cung cấp").type("PAYMENT").build(),
                TransactionReason.builder().reasonCode("UTILITIES").reasonName("Chi phí điện nước & mặt bằng kinh doanh").type("PAYMENT").build(),
                TransactionReason.builder().reasonCode("PAYROLL").reasonName("Chi trả lương & phụ cấp nhân viên").type("PAYMENT").build(),
                TransactionReason.builder().reasonCode("TAXES").reasonName("Nộp thuế VAT, TNDN & lệ phí nhà nước").type("PAYMENT").build(),
                TransactionReason.builder().reasonCode("LOGISTICS").reasonName("Chi phí vận chuyển & bốc dỡ kho bãi").type("PAYMENT").build()
            );
            defaultReasons.forEach(r -> r.setIsDeleted(false));
            transactionReasonRepository.saveAll(defaultReasons);
            log.info("Đã khởi tạo các lý do thu/chi tài chính vào Database.");
        }

        // 10. PHIẾU THU & PHIẾU CHI: Để trống hoàn toàn, người dùng tự tạo dữ liệu thực tế
        log.info("Hệ thống quản trị tài chính sẵn sàng tiếp nhận phiếu thu / phiếu chi mới từ người dùng.");

        // 12. KHỞI TẠO BANNER QUẢNG CÁO MẶC ĐỊNH (ACTIVE)
        if (bannerRepository.count() == 0) {
            List<Banner> defaultBanners = List.of(
                Banner.builder()
                    .title("Lễ hội Công nghệ AuraMart 2026 - Giảm tới 50%")
                    .imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=1600&auto=format&fit=crop&q=80")
                    .linkUrl("/listing")
                    .sortOrder(1)
                    .isActive(true)
                    .validFrom(LocalDateTime.now().minusDays(1))
                    .validUntil(LocalDateTime.now().plusMonths(6))
                    .build(),
                Banner.builder()
                    .title("Bộ Sưu Tập Giày Sneaker & Phụ Kiện Thể Thao Chính Hãng")
                    .imageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=1600&auto=format&fit=crop&q=80")
                    .linkUrl("/listing")
                    .sortOrder(2)
                    .isActive(true)
                    .validFrom(LocalDateTime.now().minusDays(1))
                    .validUntil(LocalDateTime.now().plusMonths(6))
                    .build(),
                Banner.builder()
                    .title("Đồng Hồ & Thiết Bị Đeo Thông Minh Thế Hệ Mới")
                    .imageUrl("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=1600&auto=format&fit=crop&q=80")
                    .linkUrl("/listing")
                    .sortOrder(3)
                    .isActive(true)
                    .validFrom(LocalDateTime.now().minusDays(1))
                    .validUntil(LocalDateTime.now().plusMonths(6))
                    .build()
            );
            bannerRepository.saveAll(defaultBanners);
            log.info("Đã khởi tạo {} banner quảng cáo active mặc định vào Database.", defaultBanners.size());
        }

        // 13. KHỞI TẠO DANH MỤC & SẢN PHẨM MẶC ĐỊNH CHO FE_WEBONLINE & RETAILHUB
        if (categoriesRepository.count() == 0) {
            ProductCategory catBev = ProductCategory.builder()
                    .categoryCode("CAT-BEV")
                    .categoryName("Đồ Uống & Giải Khát")
                    .description("Nước ngọt, trà, sữa, nước ép, cà phê...")
                    .imageUrl("https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=800&auto=format&fit=crop&q=80")
                    .isActive(true)
                    .build();
            catBev.setIsDeleted(false);
            catBev = categoriesRepository.save(catBev);

            ProductCategory catFood = ProductCategory.builder()
                    .categoryCode("CAT-FOOD")
                    .categoryName("Bánh Kẹo & Thực Phẩm")
                    .description("Bánh quy, đồ ăn vặt, gia vị, thực phẩm đóng hộp...")
                    .imageUrl("https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=800&auto=format&fit=crop&q=80")
                    .isActive(true)
                    .build();
            catFood.setIsDeleted(false);
            catFood = categoriesRepository.save(catFood);

            ProductCategory catFashion = ProductCategory.builder()
                    .categoryCode("CAT-FASHION")
                    .categoryName("Thời Trang & Phụ Kiện")
                    .description("Quần áo thời trang, giày sneaker, phụ kiện...")
                    .imageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&auto=format&fit=crop&q=80")
                    .isActive(true)
                    .build();
            catFashion.setIsDeleted(false);
            catFashion = categoriesRepository.save(catFashion);

            ProductCategory catElec = ProductCategory.builder()
                    .categoryCode("CAT-ELEC")
                    .categoryName("Thiết Bị Điện Tử & Âm Thanh")
                    .description("Tai nghe, loa bluetooth, phụ kiện công nghệ...")
                    .imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80")
                    .isActive(true)
                    .build();
            catElec.setIsDeleted(false);
            catElec = categoriesRepository.save(catElec);

            log.info("Đã khởi tạo 4 danh mục sản phẩm vào Database.");

            if (productRepository.count() == 0) {
                List<Product> defaultProducts = List.of(
                    Product.builder()
                        .productCode("SP-COCA-330")
                        .name("Nước ngọt Coca-Cola vị nguyên bản lon 330ml")
                        .description("Nước giải khát có gas Coca-Cola lon 330ml vị nguyên bản sảng khoái, nhập chính hãng.")
                        .basePrice(new BigDecimal("10000"))
                        .costPrice(new BigDecimal("7500"))
                        .brand("Coca-Cola")
                        .mainImageUrl("https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=800&auto=format&fit=crop&q=80")
                        .barcode("8934560111111")
                        .category(catBev)
                        .isActive(true)
                        .reorderPoint(new BigDecimal("20"))
                        .minStock(new BigDecimal("10"))
                        .maxStock(new BigDecimal("500"))
                        .build(),
                    Product.builder()
                        .productCode("SP-PEPSI-330")
                        .name("Nước ngọt Pepsi chanh không calo lon 330ml")
                        .description("Nước giải khát có gas Pepsi Zero Calories hương vị chanh thanh mát bùng nổ năng lượng.")
                        .basePrice(new BigDecimal("10000"))
                        .costPrice(new BigDecimal("7500"))
                        .brand("Suntory PepsiCo")
                        .mainImageUrl("https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=800&auto=format&fit=crop&q=80")
                        .barcode("8934560222222")
                        .category(catBev)
                        .isActive(true)
                        .reorderPoint(new BigDecimal("20"))
                        .minStock(new BigDecimal("10"))
                        .maxStock(new BigDecimal("500"))
                        .build(),
                    Product.builder()
                        .productCode("SP-VINA-1L")
                        .name("Sữa tươi tiệt trùng Vinamilk 100% nguyên chất 1L")
                        .description("Sữa tươi tiệt trùng Vinamilk 100% sữa tươi từ trang trại chuẩn quốc tế, giàu canxi & vitamin D3.")
                        .basePrice(new BigDecimal("36000"))
                        .costPrice(new BigDecimal("29000"))
                        .brand("Vinamilk")
                        .mainImageUrl("https://images.unsplash.com/photo-1550583724-b2692b85b150?w=800&auto=format&fit=crop&q=80")
                        .barcode("8934560333333")
                        .category(catBev)
                        .isActive(true)
                        .reorderPoint(new BigDecimal("15"))
                        .minStock(new BigDecimal("5"))
                        .maxStock(new BigDecimal("200"))
                        .build(),
                    Product.builder()
                        .productCode("SP-OREO-133")
                        .name("Bánh quy kẹp kem Vani Oreo cây 133g")
                        .description("Bánh quy sôcôla kẹp kem vani thơm ngon giòn rụm, phong cách thưởng thức Xoay - Liếm - Nhúng sữa.")
                        .basePrice(new BigDecimal("18000"))
                        .costPrice(new BigDecimal("13500"))
                        .brand("Mondelez")
                        .mainImageUrl("https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=800&auto=format&fit=crop&q=80")
                        .barcode("8934560444444")
                        .category(catFood)
                        .isActive(true)
                        .reorderPoint(new BigDecimal("25"))
                        .minStock(new BigDecimal("10"))
                        .maxStock(new BigDecimal("300"))
                        .build(),
                    Product.builder()
                        .productCode("SP-CHINSU-250")
                        .name("Tương ớt Chinsu đượm vị cay bùng nổ chai 250g")
                        .description("Tương ớt Chinsu thơm cay hảo hạng, món gia vị không thể thiếu cho các bữa ăn Việt.")
                        .basePrice(new BigDecimal("15000"))
                        .costPrice(new BigDecimal("11000"))
                        .brand("Masan")
                        .mainImageUrl("https://images.unsplash.com/photo-1588644525273-f37b60d78512?w=800&auto=format&fit=crop&q=80")
                        .barcode("8934560555555")
                        .category(catFood)
                        .isActive(true)
                        .reorderPoint(new BigDecimal("30"))
                        .minStock(new BigDecimal("10"))
                        .maxStock(new BigDecimal("400"))
                        .build(),
                    Product.builder()
                        .productCode("SP-AOTHUN-COTTON")
                        .name("Áo thun nam Cotton Compact cao cấp Co- giãn thoáng khí")
                        .description("Chất liệu 100% sợi Cotton Compact 2 chiều chải kỹ, không bai xù, thấm hút mồ hôi tối đa.")
                        .basePrice(new BigDecimal("189000"))
                        .costPrice(new BigDecimal("110000"))
                        .brand("Coolmate")
                        .mainImageUrl("https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=800&auto=format&fit=crop&q=80")
                        .barcode("8934560666666")
                        .category(catFashion)
                        .isActive(true)
                        .reorderPoint(new BigDecimal("10"))
                        .minStock(new BigDecimal("5"))
                        .maxStock(new BigDecimal("150"))
                        .build(),
                    Product.builder()
                        .productCode("SP-GIAY-SNEAKER")
                        .name("Giày thể thao nam nữ Sneaker Dynamic thế hệ mới")
                        .description("Đế đệm khí êm ái đàn hồi cao, thiết kế thể thao trẻ trung năng động phù hợp chạy bộ và đi học, đi làm.")
                        .basePrice(new BigDecimal("450000"))
                        .costPrice(new BigDecimal("280000"))
                        .brand("Biti's Hunter")
                        .mainImageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&auto=format&fit=crop&q=80")
                        .barcode("8934560777777")
                        .category(catFashion)
                        .isActive(true)
                        .reorderPoint(new BigDecimal("8"))
                        .minStock(new BigDecimal("3"))
                        .maxStock(new BigDecimal("100"))
                        .build(),
                    Product.builder()
                        .productCode("SP-TAINGHE-BLUETOOTH")
                        .name("Tai nghe không dây Bluetooth True Wireless khử tiếng ồn")
                        .description("Âm thanh vòm sống động, thời lượng pin 24h, công nghệ chống ồn chủ động ANC tiên tiến.")
                        .basePrice(new BigDecimal("320000"))
                        .costPrice(new BigDecimal("210000"))
                        .brand("SoundCore")
                        .mainImageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80")
                        .barcode("8934560888888")
                        .category(catElec)
                        .isActive(true)
                        .reorderPoint(new BigDecimal("10"))
                        .minStock(new BigDecimal("5"))
                        .maxStock(new BigDecimal("100"))
                        .build()
                );
                defaultProducts.forEach(p -> p.setIsDeleted(false));
                productRepository.saveAll(defaultProducts);
                log.info("Đã khởi tạo 8 sản phẩm chuẩn vào Database.");
            }
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
