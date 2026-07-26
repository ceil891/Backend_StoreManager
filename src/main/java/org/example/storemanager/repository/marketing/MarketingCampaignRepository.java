package org.example.storemanager.repository.marketing;

import org.example.storemanager.entity.marketing.MarketingCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface MarketingCampaignRepository extends JpaRepository<MarketingCampaign, Long> {
    Optional<MarketingCampaign> findByIdAndIsDeletedFalse(Long id);
    List<MarketingCampaign> findByIsDeletedFalse();
}
