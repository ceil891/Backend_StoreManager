package org.example.storemanager.repository.sales;

import org.example.storemanager.entity.sales.SaleOrder;
import org.example.storemanager.enums.sales.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaleOrderRepository extends JpaRepository<SaleOrder, Long> {

    Optional<SaleOrder> findByIdAndIsDeletedFalse(Long id);

    List<SaleOrder> findByIsActiveTrueAndIsDeletedFalse();

    @Query("SELECT s FROM SaleOrder s WHERE s.isDeleted = false " +
            "AND (:keyword IS NULL OR s.orderCode LIKE %:keyword%) " +
            "AND (:status IS NULL OR s.status = :status) " +
            "AND (:branchId IS NULL OR s.branchId = :branchId)")
    Page<SaleOrder> searchOrders(@Param("keyword") String keyword,
                                 @Param("status") OrderStatus status,
                                 @Param("branchId") Long branchId,
                                 Pageable pageable);
}