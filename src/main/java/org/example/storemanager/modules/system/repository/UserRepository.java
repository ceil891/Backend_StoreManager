package org.example.storemanager.modules.system.repository;

import org.example.storemanager.modules.system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findByIdAndIsDeletedFalse(Long id);

    Optional<User> findByUsernameAndIsDeletedFalse(String username);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByPhoneAndIsDeletedFalse(String phone);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByUsernameAndIsDeletedFalse(String username);

    boolean existsByEmailAndIsDeletedFalse(String email);

    boolean existsByUsernameAndIdNotAndIsDeletedFalse(String username, Long id);

    Page<User> findByIsDeletedFalse(Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"role", "branch"})
    java.util.List<User> findByIsDeletedFalse();

    @Query("SELECT u FROM User u WHERE " +
            "(:includeDeleted = true OR u.isDeleted = false OR u.isDeleted IS NULL) AND " +
            "(cast(:status as string) IS NULL OR u.status = :status) AND " +
            "(cast(:roleId as long) IS NULL OR u.role.id = :roleId) AND " +
            "(:roleId IS NOT NULL OR u.role IS NULL OR UPPER(TRIM(u.role.roleName)) NOT IN ('CUSTOMER', 'KHÁCH HÀNG', 'KHACH HANG', 'USER', 'NGƯỜI DÙNG', 'NGUOI DUNG')) AND " +
            "(cast(:branchId as long) IS NULL OR u.branch.id = :branchId) AND " +
            "(cast(:search as string) IS NULL OR cast(:search as string) = '' OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
            "LOWER(u.phone) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    Page<User> findAllUsersIncludeDeleted(
            @Param("search") String search,
            @Param("status") String status,
            @Param("roleId") Long roleId,
            @Param("branchId") Long branchId,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}