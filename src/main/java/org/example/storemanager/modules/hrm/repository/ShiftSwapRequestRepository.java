package org.example.storemanager.modules.hrm.repository;

import org.example.storemanager.modules.hrm.entity.ShiftSwapRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftSwapRequestRepository extends JpaRepository<ShiftSwapRequest, Long> {
    List<ShiftSwapRequest> findByIsDeletedFalse();
    Optional<ShiftSwapRequest> findByIdAndIsDeletedFalse(Long id);
}
