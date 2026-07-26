package org.example.storemanager.repository.crm;

import org.example.storemanager.entity.crm.MarketOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface MarketOrderDetailRepository extends JpaRepository<MarketOrderDetail, Long> {
    Optional<MarketOrderDetail> findByIdAndIsDeletedFalse(Long id);
    List<MarketOrderDetail> findByIsDeletedFalse();
}
