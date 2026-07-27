package org.example.storemanager.modules.hrm.repository;

import org.example.storemanager.modules.hrm.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    Optional<LeaveRequest> findByIdAndIsDeletedFalse(Long id);
    List<LeaveRequest> findByIsDeletedFalse();
}
