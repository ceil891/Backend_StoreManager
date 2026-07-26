package org.example.storemanager.repository.advancedaccounting;

import org.example.storemanager.entity.advancedaccounting.DepreciationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface DepreciationHistoryRepository extends JpaRepository<DepreciationHistory, Long> {
    Optional<DepreciationHistory> findByIdAndIsDeletedFalse(Long id);
    List<DepreciationHistory> findByIsDeletedFalse();
}
