package org.example.storemanager.modules.inventory.repository;

import org.example.storemanager.modules.inventory.entity.StockTransferDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockTransferDetailRepository extends JpaRepository<StockTransferDetail, Long> {
    List<StockTransferDetail> findByTransferIdAndIsDeletedFalse(Long transferId);
}
