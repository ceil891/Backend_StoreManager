package org.example.storemanager.modules.inventory.repository;

import org.example.storemanager.modules.inventory.entity.InventoryBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryBalanceRepository extends JpaRepository<InventoryBalance, Long> {

    Optional<InventoryBalance> findByProductVariantIdAndBranchId(Long productVariantId, Long branchId);

    List<InventoryBalance> findByProductVariantIdAndIsDeletedFalse(Long productVariantId);

    List<InventoryBalance> findByProductVariantIdInAndIsDeletedFalse(List<Long> productVariantIds);

    List<InventoryBalance> findByIsDeletedFalse();
    List<InventoryBalance> findByBranchIdAndIsDeletedFalse(Long branchId);

}
