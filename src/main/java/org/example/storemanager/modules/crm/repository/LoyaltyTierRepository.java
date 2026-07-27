package org.example.storemanager.modules.crm.repository;

import org.example.storemanager.modules.crm.entity.LoyaltyTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface LoyaltyTierRepository extends JpaRepository<LoyaltyTier, Long> {
    Optional<LoyaltyTier> findByIdAndIsDeletedFalse(Long id);
    List<LoyaltyTier> findByIsDeletedFalse();
}
