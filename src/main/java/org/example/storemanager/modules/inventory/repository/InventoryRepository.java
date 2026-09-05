package org.example.storemanager.modules.inventory.repository;

import org.example.storemanager.modules.inventory.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByBranchIdAndProductIdAndSizeIdAndColorId(Long branchId, Long productId, Long sizeId, Long colorId);

    List<Inventory> findByProductId(Long productId);

    @Query("SELECT i FROM Inventory i " +
           "LEFT JOIN i.size s " +
           "LEFT JOIN i.color c " +
           "WHERE " +
           "(cast(:productId as long) IS NULL OR i.product.id = :productId) AND " +
           "(cast(:branchId as long) IS NULL OR i.branch.id = :branchId) AND " +
           "(cast(:departmentId as long) IS NULL OR i.product.category.department.id = :departmentId) AND " +
           "(cast(:size as string) IS NULL OR cast(:size as string) = '' OR LOWER(s.sizeCode) = LOWER(cast(:size as string)) OR LOWER(s.sizeName) LIKE LOWER(CONCAT('%', cast(:size as string), '%'))) AND " +
           "(cast(:color as string) IS NULL OR cast(:color as string) = '' OR LOWER(c.colorCode) = LOWER(cast(:color as string)) OR LOWER(c.colorName) LIKE LOWER(CONCAT('%', cast(:color as string), '%'))) AND " +
           "(cast(:search as string) IS NULL OR cast(:search as string) = '' OR " +
           "LOWER(i.product.name) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(i.product.productCode) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(i.branch.branchName) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    Page<Inventory> searchInventory(
            @Param("productId") Long productId,
            @Param("branchId") Long branchId,
            @Param("departmentId") Long departmentId,
            @Param("size") String size,
            @Param("color") String color,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE i.product.minStock IS NOT NULL AND i.quantity < i.product.minStock")
    List<Inventory> findLowStock();
}
