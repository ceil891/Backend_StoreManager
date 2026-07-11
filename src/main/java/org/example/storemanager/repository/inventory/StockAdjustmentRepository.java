package org.example.storemanager.repository.inventory;

import org.example.storemanager.entity.inventory.StockAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, Long> {

    Optional<StockAdjustment> findByIdAndIsDeletedFalse(Long id);

    Optional<StockAdjustment> findByAdjustmentCodeAndIsDeletedFalse(String adjustmentCode);

    boolean existsByAdjustmentCodeAndIsDeletedFalse(String adjustmentCode);

    List<StockAdjustment> findByBranchIdAndIsDeletedFalse(Long branchId);
}
