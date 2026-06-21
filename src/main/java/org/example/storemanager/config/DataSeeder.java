package org.example.storemanager.config;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.entity.system.Permission;
import org.example.storemanager.entity.system.Role;
import org.example.storemanager.entity.system.RolePermission;
import org.example.storemanager.entity.system.User;
import org.example.storemanager.repository.system.PermissionRepository;
import org.example.storemanager.repository.system.RolePermissionRepository;
import org.example.storemanager.repository.system.RoleRepository;
import org.example.storemanager.repository.system.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Tự động sinh danh sách Core Permissions nếu chưa có trong DB
        List<Permission> pendingPermissions = new ArrayList<>();

        // ========== MODULE USER ==========
        addIfAbsent("system:user:view", "System", "Xem danh sách và chi tiết người dùng", pendingPermissions);
        addIfAbsent("system:user:create", "System", "Tạo mới người dùng", pendingPermissions);
        addIfAbsent("system:user:update", "System", "Cập nhật thông tin người dùng", pendingPermissions);
        addIfAbsent("system:user:update-status", "System", "Cập nhật trạng thái người dùng", pendingPermissions);
        addIfAbsent("system:user:reset-password", "System", "Khôi phục mật khẩu người dùng", pendingPermissions);
        addIfAbsent("system:user:delete", "System", "Xóa người dùng", pendingPermissions);
        addIfAbsent("system:user:restore", "System", "Khôi phục tài khoản người dùng đã xóa mềm", pendingPermissions);

        // ========== MODULE ROLE ==========
        addIfAbsent("system:role:view", "System", "Xem danh sách và chi tiết vai trò", pendingPermissions);
        addIfAbsent("system:role:create", "System", "Tạo mới vai trò", pendingPermissions);
        addIfAbsent("system:role:update", "System", "Cập nhật thông tin vai trò", pendingPermissions);
        addIfAbsent("system:role:update-status", "System", "Cập nhật trạng thái vai trò", pendingPermissions);
        addIfAbsent("system:role:delete", "System", "Xóa vai trò", pendingPermissions);
        addIfAbsent("system:role:assign-permissions", "System", "Gán quyền cho vai trò", pendingPermissions);
        addIfAbsent("system:role:restore", "System", "Khôi phục vai trò đã xóa mềm", pendingPermissions);

        // ========== QUYỀN HẠN MODULE PERMISSION ==========
        addIfAbsent("system:permission:view", "System", "Xem danh sách quyền hạn", pendingPermissions);

        if (!pendingPermissions.isEmpty()) {
            // FIX LỖI: Dùng saveAllAndFlush để ép JPA đẩy ngay xuống Database
            permissionRepository.saveAllAndFlush(pendingPermissions);
        }

        // 2. Khởi tạo thực thể SUPER_ADMIN (nếu chưa có)
        Role superAdminRole = roleRepository.findByRoleName("SUPER_ADMIN").orElse(null);

        if (superAdminRole == null) {
            superAdminRole = Role.builder()
                    .roleName("SUPER_ADMIN")
                    .description("Vai trò quản trị tối cao của toàn bộ hệ thống")
                    .isActive(true)
                    .build();
            superAdminRole.setIsDeleted(false);
            superAdminRole = roleRepository.saveAndFlush(superAdminRole);
        }

        // --- CẬP NHẬT TỰ ĐỘNG CÁC QUYỀN MỚI NHẤT CHO SUPER_ADMIN ---

        // Bước A: Lấy tất cả quyền hiện có trong Database (Cũ + Mới)
        List<Permission> allPermissions = permissionRepository.findAll();

        // Bước B: Lấy danh sách ID các quyền mà SUPER_ADMIN ĐANG CÓ sẵn
        List<RolePermission> currentRolePermissions = rolePermissionRepository.findByRoleId(superAdminRole.getId());
        Set<Long> currentPermissionIds = currentRolePermissions.stream()
                .map(rp -> rp.getPermission().getId())
                .collect(Collectors.toSet());

        // Bước C: Lọc ra những quyền MỚI CHƯA CÓ để chuẩn bị gán thêm
        List<RolePermission> newPermissionsToAssign = new ArrayList<>();
        for (Permission perm : allPermissions) {
            if (!currentPermissionIds.contains(perm.getId())) {
                RolePermission newRp = RolePermission.builder()
                        .role(superAdminRole)
                        .permission(perm)
                        .build();
                // FIX LỖI: Set mặc định thuộc tính isDeleted cho RolePermission
                newRp.setIsDeleted(false);
                newPermissionsToAssign.add(newRp);
            }
        }

        // Bước D: Lưu các quyền bổ sung xuống Database
        if (!newPermissionsToAssign.isEmpty()) {
            // FIX LỖI: Ép đẩy dữ liệu tức thì
            rolePermissionRepository.saveAllAndFlush(newPermissionsToAssign);
        }

        // 3. TẠO TÀI KHOẢN ADMIN MẶC ĐỊNH
        if (superAdminRole != null && !userRepository.existsByUsername("admin")) {
            User adminUser = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Quản trị viên hệ thống")
                    .email("admin@storemanager.com")
                    .phone("0999999999")
                    .status("ACTIVE")
                    .role(superAdminRole)
                    .build();
            adminUser.setIsDeleted(false);

            userRepository.save(adminUser);
        }
    }

    private void addIfAbsent(String code, String module, String desc, List<Permission> list) {
        if (!permissionRepository.existsByPermissionCode(code)) {
            Permission perm = Permission.builder()
                    .permissionCode(code)
                    .module(module)
                    .description(desc)
                    // FIX LỖI: Bắt buộc phải là isActive = true
                    .isActive(true)
                    .build();
            perm.setIsDeleted(false);
            list.add(perm);
        }
    }
}