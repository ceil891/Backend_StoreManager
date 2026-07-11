package org.example.storemanager.repository.inventory;

import org.example.storemanager.entity.inventory.InventoryBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryBalanceRepository extends JpaRepository<InventoryBalance, Long> {

    Optional<InventoryBalance> findByProductVariantIdAndBranchId(Long productVariantId, Long branchId);

    List<InventoryBalance> findByProductVariantIdAndIsDeletedFalse(Long productVariantId);

    List<InventoryBalance> findByBranchIdAndIsDeletedFalse(Long branchId);
}
