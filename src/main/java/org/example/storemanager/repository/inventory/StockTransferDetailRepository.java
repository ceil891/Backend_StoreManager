package org.example.storemanager.repository.inventory;

import org.example.storemanager.entity.inventory.StockTransferDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockTransferDetailRepository extends JpaRepository<StockTransferDetail, Long> {
    List<StockTransferDetail> findByTransferIdAndIsDeletedFalse(Long transferId);
}
