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

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(b.availableQuantity), 0) FROM InventoryBalance b WHERE b.productVariant.product.id = :productId AND b.isDeleted = false")
    java.math.BigDecimal sumAvailableQuantityByProductId(@org.springframework.data.repository.query.Param("productId") Long productId);

    @org.springframework.data.jpa.repository.Query("SELECT b.productVariant.product.id, COALESCE(SUM(b.availableQuantity), 0) FROM InventoryBalance b WHERE b.productVariant.product.id IN :productIds AND b.isDeleted = false GROUP BY b.productVariant.product.id")
    List<Object[]> sumAvailableQuantityByProductIds(@org.springframework.data.repository.query.Param("productIds") List<Long> productIds);

    @org.springframework.data.jpa.repository.Query("SELECT b FROM InventoryBalance b WHERE b.productVariant.product.id = :productId AND b.isDeleted = false")
    List<InventoryBalance> findByProductIdAndIsDeletedFalse(@org.springframework.data.repository.query.Param("productId") Long productId);
}
