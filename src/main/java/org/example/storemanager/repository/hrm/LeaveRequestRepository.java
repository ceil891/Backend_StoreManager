package org.example.storemanager.repository.hrm;

import org.example.storemanager.entity.hrm.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    Optional<LeaveRequest> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT l FROM LeaveRequest l WHERE " +
           "(:includeDeleted = true OR l.isDeleted = false) AND " +
           "(:isActive IS NULL OR (:isActive = true AND (l.isLocked IS NULL OR l.isLocked = false)) OR (:isActive = false AND l.isLocked = true)) AND " +
           "(:userId IS NULL OR l.user.id = :userId) AND " +
           "(:status IS NULL OR :status = '' OR l.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(l.leaveType) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.reason) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.status) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<LeaveRequest> findAllFiltered(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);

    @Query("SELECT l FROM LeaveRequest l WHERE l.user.id = :userId AND l.isDeleted = false ORDER BY l.startDate DESC")
    List<LeaveRequest> findUserLeaveHistory(@Param("userId") Long userId);

    @Query("SELECT l FROM LeaveRequest l WHERE l.status = 'PENDING' AND l.isDeleted = false ORDER BY l.startDate ASC")
    List<LeaveRequest> findPendingLeaves();

    @Query("SELECT l FROM LeaveRequest l WHERE l.user.id = :userId AND l.status IN ('APPROVED', 'PENDING') AND l.isDeleted = false")
    List<LeaveRequest> findActiveLeavesByUserId(@Param("userId") Long userId);
}
