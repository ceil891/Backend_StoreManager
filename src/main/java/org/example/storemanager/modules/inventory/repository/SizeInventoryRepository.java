package org.example.storemanager.modules.inventory.repository;

import org.example.storemanager.modules.inventory.entity.SizeInventory;
import org.example.storemanager.modules.catalog.dto.response.inventory.InventorySummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface SizeInventoryRepository extends JpaRepository<SizeInventory, Long> {

    Optional<SizeInventory> findByWarehouseZoneIdAndProductIdAndSizeIdAndColorId(
            Long warehouseZoneId, Long productId, Long sizeId, Long colorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT si FROM SizeInventory si WHERE si.warehouseZone.id = :zoneId AND si.product.id = :productId AND " +
           "((:sizeId IS NULL AND si.size IS NULL) OR (si.size.id = :sizeId)) AND " +
           "((:colorId IS NULL AND si.color IS NULL) OR (si.color.id = :colorId))")
    Optional<SizeInventory> findAndLockBySkuAttributes(
            @Param("zoneId") Long zoneId, 
            @Param("productId") Long productId, 
            @Param("sizeId") Long sizeId, 
            @Param("colorId") Long colorId);

    List<SizeInventory> findByProductIdAndIsDeletedFalse(Long productId);

    @Query(value = "SELECT si FROM SizeInventory si " +
           "JOIN FETCH si.product p " +
           "JOIN FETCH si.warehouseZone wz " +
           "JOIN FETCH wz.branch b " +
           "LEFT JOIN FETCH si.size s " +
           "LEFT JOIN FETCH si.color c " +
           "LEFT JOIN p.category cat " +
           "LEFT JOIN cat.department dept " +
           "WHERE si.isDeleted = false AND " +
           "(:productId IS NULL OR si.product.id = :productId) AND " +
           "(:categoryId IS NULL OR cat.id = :categoryId) AND " +
           "(:departmentId IS NULL OR dept.id = :departmentId) AND " +
           "(:branchId IS NULL OR b.id = :branchId) AND " +
           "(:warehouseZoneId IS NULL OR wz.id = :warehouseZoneId) AND " +
           "(:size IS NULL OR :size = '' OR LOWER(s.sizeCode) = LOWER(:size) OR LOWER(s.sizeName) LIKE LOWER(CONCAT('%', :size, '%'))) AND " +
           "(:color IS NULL OR :color = '' OR LOWER(c.colorCode) = LOWER(:color) OR LOWER(c.colorName) LIKE LOWER(CONCAT('%', :color, '%'))) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.productCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(wz.zoneName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.branchName) LIKE LOWER(CONCAT('%', :search, '%')))",
           countQuery = "SELECT COUNT(si) FROM SizeInventory si " +
           "JOIN si.product p " +
           "JOIN si.warehouseZone wz " +
           "JOIN wz.branch b " +
           "LEFT JOIN si.size s " +
           "LEFT JOIN si.color c " +
           "LEFT JOIN p.category cat " +
           "LEFT JOIN cat.department dept " +
           "WHERE si.isDeleted = false AND " +
           "(:productId IS NULL OR si.product.id = :productId) AND " +
           "(:categoryId IS NULL OR cat.id = :categoryId) AND " +
           "(:departmentId IS NULL OR dept.id = :departmentId) AND " +
           "(:branchId IS NULL OR b.id = :branchId) AND " +
           "(:warehouseZoneId IS NULL OR wz.id = :warehouseZoneId) AND " +
           "(:size IS NULL OR :size = '' OR LOWER(s.sizeCode) = LOWER(:size) OR LOWER(s.sizeName) LIKE LOWER(CONCAT('%', :size, '%'))) AND " +
           "(:color IS NULL OR :color = '' OR LOWER(c.colorCode) = LOWER(:color) OR LOWER(c.colorName) LIKE LOWER(CONCAT('%', :color, '%'))) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.productCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(wz.zoneName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.branchName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<InventorySummaryProjection> searchInventory(
            @Param("productId") Long productId,
            @Param("categoryId") Long categoryId,
            @Param("departmentId") Long departmentId,
            @Param("branchId") Long branchId,
            @Param("warehouseZoneId") Long warehouseZoneId,
            @Param("size") String size,
            @Param("color") String color,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT si FROM SizeInventory si " +
           "JOIN FETCH si.product p " +
           "JOIN FETCH si.warehouseZone wz " +
           "JOIN FETCH wz.branch b " +
           "LEFT JOIN FETCH si.size s " +
           "LEFT JOIN FETCH si.color c " +
           "WHERE si.isDeleted = false")
    List<SizeInventory> findAllWithAssociations();

    @Query("SELECT si FROM SizeInventory si " +
           "JOIN FETCH si.product p " +
           "JOIN FETCH si.warehouseZone wz " +
           "JOIN FETCH wz.branch b " +
           "WHERE si.isDeleted = false AND " +
           "p.minStock IS NOT NULL AND " +
           "(si.quantityPhysical - si.quantityAllocated) < p.minStock")
    List<SizeInventory> findLowStock();

    /** Tổng tồn kho vật lý của một sản phẩm trên tất cả zone */
    @Query("SELECT COALESCE(SUM(si.quantityPhysical), 0) FROM SizeInventory si " +
           "WHERE si.product.id = :productId AND si.isDeleted = false")
    java.math.BigDecimal sumOnHandByProductId(@Param("productId") Long productId);

    /** Tổng tồn kho vật lý theo lô sản phẩm */
    @Query("SELECT si.product.id, COALESCE(SUM(si.quantityPhysical), 0) FROM SizeInventory si " +
           "WHERE si.product.id IN :productIds AND si.isDeleted = false " +
           "GROUP BY si.product.id")
    List<Object[]> sumOnHandByProductIds(@Param("productIds") List<Long> productIds);
}
