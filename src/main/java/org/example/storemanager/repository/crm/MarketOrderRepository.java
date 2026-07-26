package org.example.storemanager.repository.crm;

import org.example.storemanager.entity.crm.MarketOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface MarketOrderRepository extends JpaRepository<MarketOrder, Long> {
    Optional<MarketOrder> findByIdAndIsDeletedFalse(Long id);
    List<MarketOrder> findByIsDeletedFalse();
}
