package org.example.storemanager.repository.hrm;

import org.example.storemanager.entity.hrm.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    Optional<LeaveRequest> findByIdAndIsDeletedFalse(Long id);
    List<LeaveRequest> findByIsDeletedFalse();
}
