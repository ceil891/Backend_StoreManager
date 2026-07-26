package org.example.storemanager.repository.inventory;

import org.example.storemanager.entity.inventory.StockLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface StockLedgerRepository extends JpaRepository<StockLedger, Long> {
    @Query("SELECT sl FROM StockLedger sl " +
           "JOIN FETCH sl.product p " +
           "JOIN FETCH sl.branch b " +
           "LEFT JOIN FETCH sl.warehouseZone wz " +
           "ORDER BY sl.id DESC")
    List<StockLedger> findAllWithProductAndBranch();
}
