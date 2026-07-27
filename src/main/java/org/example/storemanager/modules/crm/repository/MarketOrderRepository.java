package org.example.storemanager.modules.crm.repository;

import org.example.storemanager.modules.crm.entity.MarketOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface MarketOrderRepository extends JpaRepository<MarketOrder, Long> {
    Optional<MarketOrder> findByIdAndIsDeletedFalse(Long id);
    List<MarketOrder> findByIsDeletedFalse();
}
