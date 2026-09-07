package org.example.storemanager.modules.hrm.repository;

import org.example.storemanager.modules.hrm.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.user LEFT JOIN FETCH l.approvedByUser WHERE l.id = :id AND l.isDeleted = false")
    Optional<LeaveRequest> findByIdAndIsDeletedFalse(@Param("id") Long id);

    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.user LEFT JOIN FETCH l.approvedByUser WHERE l.isDeleted = false ORDER BY l.id DESC")
    List<LeaveRequest> findByIsDeletedFalse();
}
