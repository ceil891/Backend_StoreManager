package org.example.storemanager.repository.inventory;

import org.example.storemanager.entity.inventory.StockAdjustmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockAdjustmentDetailRepository extends JpaRepository<StockAdjustmentDetail, Long> {

    List<StockAdjustmentDetail> findByStockAdjustmentIdAndIsDeletedFalse(Long adjustmentId);

    List<StockAdjustmentDetail> findByProductVariantIdAndIsDeletedFalse(Long productVariantId);
}
