package org.example.storemanager.repository.inventory;

import org.example.storemanager.entity.inventory.SizeInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SizeInventoryRepository extends JpaRepository<SizeInventory, Long> {

    Optional<SizeInventory> findByWarehouseZoneIdAndProductIdAndSizeIdAndColorId(
            Long warehouseZoneId, Long productId, Long sizeId, Long colorId);

    List<SizeInventory> findByProductIdAndIsDeletedFalse(Long productId);

    @Query("SELECT si FROM SizeInventory si " +
           "JOIN si.product p " +
           "LEFT JOIN p.category cat " +
           "LEFT JOIN cat.department dept " +
           "LEFT JOIN si.size s " +
           "LEFT JOIN si.color c " +
           "WHERE si.isDeleted = false AND " +
           "(:productId IS NULL OR si.product.id = :productId) AND " +
           "(:categoryId IS NULL OR cat.id = :categoryId) AND " +
           "(:departmentId IS NULL OR dept.id = :departmentId) AND " +
           "(:branchId IS NULL OR si.warehouseZone.branch.id = :branchId) AND " +
           "(:warehouseZoneId IS NULL OR si.warehouseZone.id = :warehouseZoneId) AND " +
           "(:size IS NULL OR :size = '' OR LOWER(s.sizeCode) = LOWER(:size) OR LOWER(s.sizeName) LIKE LOWER(CONCAT('%', :size, '%'))) AND " +
           "(:color IS NULL OR :color = '' OR LOWER(c.colorCode) = LOWER(:color) OR LOWER(c.colorName) LIKE LOWER(CONCAT('%', :color, '%'))) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(si.product.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(si.product.productCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(si.warehouseZone.zoneName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(si.warehouseZone.branch.branchName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<SizeInventory> searchInventory(
            @Param("productId") Long productId,
            @Param("categoryId") Long categoryId,
            @Param("departmentId") Long departmentId,
            @Param("branchId") Long branchId,
            @Param("warehouseZoneId") Long warehouseZoneId,
            @Param("size") String size,
            @Param("color") String color,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT si FROM SizeInventory si WHERE si.isDeleted = false AND " +
           "si.product.minStock IS NOT NULL AND " +
           "(si.quantityPhysical - si.quantityAllocated) < si.product.minStock")
    List<SizeInventory> findLowStock();
}
