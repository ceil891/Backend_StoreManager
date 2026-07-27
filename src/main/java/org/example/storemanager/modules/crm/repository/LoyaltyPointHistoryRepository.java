package org.example.storemanager.modules.crm.repository;

import org.example.storemanager.modules.crm.entity.LoyaltyPointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface LoyaltyPointHistoryRepository extends JpaRepository<LoyaltyPointHistory, Long> {
    Optional<LoyaltyPointHistory> findByIdAndIsDeletedFalse(Long id);
    List<LoyaltyPointHistory> findByIsDeletedFalse();
}
