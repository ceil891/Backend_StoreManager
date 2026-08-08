package org.example.storemanager.modules.inventory.repository;

import org.example.storemanager.modules.inventory.entity.StockOutDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockOutDetailRepository extends JpaRepository<StockOutDetail, Long> {
    List<StockOutDetail> findByStockOutId(Long stockOutId);
}
