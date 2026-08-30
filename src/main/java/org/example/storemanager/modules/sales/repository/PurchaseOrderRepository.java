package org.example.storemanager.modules.sales.repository;

import org.example.storemanager.modules.sales.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByIdAndIsDeletedFalse(Long id);
    Optional<PurchaseOrder> findByPoCodeAndIsDeletedFalse(String poCode);
    java.util.List<PurchaseOrder> findByIsDeletedFalse();
    java.util.List<PurchaseOrder> findByPurchaseRequestIdAndIsDeletedFalse(Long requestId);

    @Query("SELECT po FROM PurchaseOrder po WHERE " +
           "(:includeDeleted = true OR po.isDeleted = false) AND " +
           "(:status IS NULL OR po.status = :status) AND " +
           "(:branchId IS NULL OR po.branch.id = :branchId) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(po.poCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(po.supplier.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(po.note) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PurchaseOrder> findAllOrders(
            @Param("search") String search,
            @Param("status") String status,
            @Param("branchId") Long branchId,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}
