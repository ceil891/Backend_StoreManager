package org.example.storemanager.repository.warranty;

import org.example.storemanager.entity.warranty.WarrantyRepairHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface WarrantyRepairHistoryRepository extends JpaRepository<WarrantyRepairHistory, Long> {
    Optional<WarrantyRepairHistory> findByIdAndIsDeletedFalse(Long id);
    List<WarrantyRepairHistory> findByIsDeletedFalse();
}
