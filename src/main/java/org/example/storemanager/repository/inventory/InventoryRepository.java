package org.example.storemanager.repository.inventory;

import org.example.storemanager.entity.inventory.Inventory;
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
           "(:productId IS NULL OR i.product.id = :productId) AND " +
           "(:branchId IS NULL OR i.branch.id = :branchId) AND " +
           "(:departmentId IS NULL OR i.product.category.department.id = :departmentId) AND " +
           "(:size IS NULL OR :size = '' OR LOWER(s.sizeCode) = LOWER(:size) OR LOWER(s.sizeName) LIKE LOWER(CONCAT('%', :size, '%'))) AND " +
           "(:color IS NULL OR :color = '' OR LOWER(c.colorCode) = LOWER(:color) OR LOWER(c.colorName) LIKE LOWER(CONCAT('%', :color, '%'))) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(i.product.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.product.productCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.branch.branchName) LIKE LOWER(CONCAT('%', :search, '%')))")
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
