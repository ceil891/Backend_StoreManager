package org.example.storemanager.modules.crm.repository;

import org.example.storemanager.modules.crm.entity.MarketOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface MarketOrderDetailRepository extends JpaRepository<MarketOrderDetail, Long> {
    Optional<MarketOrderDetail> findByIdAndIsDeletedFalse(Long id);
    List<MarketOrderDetail> findByIsDeletedFalse();
}
