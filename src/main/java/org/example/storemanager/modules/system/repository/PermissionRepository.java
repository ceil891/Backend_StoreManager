package org.example.storemanager.modules.system.repository;

import org.example.storemanager.modules.system.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    // Hàm kiểm tra xem mã quyền đã tồn tại chưa (dùng cho DataSeeder)
    boolean existsByPermissionCode(String permissionCode);

    // BỔ SUNG HÀM NÀY ĐỂ SỬA LỖI findAllByIsDeletedFalse
    List<Permission> findAllByIsDeletedFalse();

    // Bổ sung hàm tìm kiếm, lọc và phân trang (dành cho getAllPermissions và getPermissionsPaginated)
    @Query("SELECT p FROM Permission p WHERE " +
            "(:search IS NULL OR LOWER(p.permissionCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.module) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:isActive IS NULL OR p.isActive = :isActive) " +
            "AND (:includeDeleted = TRUE OR p.isDeleted = FALSE)")
    Page<Permission> findAllPermissionsIncludeDeleted(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}