package org.example.storemanager.modules.system.repository;

import org.example.storemanager.modules.system.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(String roleName);
    Optional<Role> findByIdAndIsDeletedFalse(Long id);

    boolean existsByRoleName(String roleName);

    boolean existsByRoleNameAndIsDeletedFalse(String roleName);

    // Xử lý check duplicate khi update tên Role
    boolean existsByRoleNameAndIdNotAndIsDeletedFalse(String roleName, Long id);

    Page<Role> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT r FROM Role r WHERE " +
            "(:includeDeleted = true OR r.isDeleted = false OR r.isDeleted IS NULL) AND " +
            "(cast(:isActive as boolean) IS NULL OR r.isActive = :isActive) AND " +
            "(cast(:search as string) IS NULL OR cast(:search as string) = '' OR " +
            "LOWER(r.roleName) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    Page<Role> findAllRolesIncludeDeleted(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}