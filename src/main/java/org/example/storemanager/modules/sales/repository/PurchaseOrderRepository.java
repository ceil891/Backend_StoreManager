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
           "(cast(:status as string) IS NULL OR cast(:status as string) = '' OR po.status = :status) AND " +
           "(cast(:branchId as long) IS NULL OR po.branch.id = :branchId) AND " +
           "(cast(:search as string) IS NULL OR cast(:search as string) = '' OR " +
           "LOWER(po.poCode) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(po.supplier.name) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(po.note) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    Page<PurchaseOrder> findAllOrders(
            @Param("search") String search,
            @Param("status") String status,
            @Param("branchId") Long branchId,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}
