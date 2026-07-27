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

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndIsDeletedFalse(Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByUsernameAndIsDeletedFalse(String username);

    boolean existsByUsernameAndIdNotAndIsDeletedFalse(String username, Long id);

    @Query("SELECT u FROM User u WHERE " +
            "(:includeDeleted = true OR u.isDeleted = false) AND " +
            "(:status IS NULL OR u.status = :status) AND " +
            "(:roleId IS NULL OR u.role.id = :roleId) AND " +
            "(:branchId IS NULL OR u.branch.id = :branchId) AND " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findAllUsersIncludeDeleted(
            @Param("search") String search,
            @Param("status") String status,
            @Param("roleId") Long roleId,
            @Param("branchId") Long branchId,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}