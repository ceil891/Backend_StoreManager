package org.example.storemanager.repository.inventory;

import org.example.storemanager.entity.inventory.StockLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockLedgerRepository extends JpaRepository<StockLedger, Long> {
}
