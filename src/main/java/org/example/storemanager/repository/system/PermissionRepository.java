package org.example.storemanager.repository.system;

import org.example.storemanager.entity.system.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByIdAndIsDeletedFalse(Long id);

    boolean existsByPermissionCode(String permissionCode);

    boolean existsByPermissionCodeAndIsDeletedFalse(String permissionCode);

    // Lấy danh sách permission chưa bị xóa để nhóm theo module
    List<Permission> findByIsDeletedFalse();
}