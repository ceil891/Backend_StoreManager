package org.example.storemanager.modules.inventory.repository;

import org.example.storemanager.modules.inventory.entity.StockOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockOutRepository extends JpaRepository<StockOut, Long> {
    Optional<StockOut> findByStockOutCode(String stockOutCode);
}
