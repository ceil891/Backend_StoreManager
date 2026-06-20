package org.example.storemanager.repository.system;

import org.example.storemanager.entity.system.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByIdAndIsDeletedFalse(Long id);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIsDeletedFalse(String username);

    // Bỏ qua check ID hiện tại khi update (nếu cần đổi username sau này)
    boolean existsByUsernameAndIdNotAndIsDeletedFalse(String username, Long id);

    @Query("SELECT u FROM User u WHERE " +
            "(:includeDeleted = true OR u.isDeleted = false) AND " +
            "(:status IS NULL OR u.status = :status) AND " +
            "(:roleId IS NULL OR u.role.id = :roleId) AND " +
            // Nếu User Entity của bạn có quan hệ với Branch, hãy mở comment dòng dưới:
            // "(:branchId IS NULL OR u.branch.id = :branchId) AND " +
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