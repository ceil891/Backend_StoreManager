package org.example.storemanager.repository.crm;

import org.example.storemanager.entity.crm.LoyaltyTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface LoyaltyTierRepository extends JpaRepository<LoyaltyTier, Long> {
    Optional<LoyaltyTier> findByIdAndIsDeletedFalse(Long id);
    List<LoyaltyTier> findByIsDeletedFalse();
}
