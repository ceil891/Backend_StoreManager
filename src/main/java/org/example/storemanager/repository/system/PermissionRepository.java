package org.example.storemanager.repository.system;

import org.example.storemanager.entity.system.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT p FROM Permission p WHERE " +
            "(:includeDeleted = true OR p.isDeleted = false) AND " +
            "(:isActive IS NULL OR p.isActive = :isActive) AND " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(p.permissionCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.module) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Permission> findAllPermissionsIncludeDeleted(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}