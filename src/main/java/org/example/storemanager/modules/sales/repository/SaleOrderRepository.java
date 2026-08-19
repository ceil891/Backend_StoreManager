package org.example.storemanager.modules.sales.repository;

import org.example.storemanager.modules.sales.entity.SaleOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SaleOrderRepository extends JpaRepository<SaleOrder, Long> {
    Optional<SaleOrder> findByIdAndIsDeletedFalse(Long id);
    java.util.List<SaleOrder> findByIsDeletedFalse();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"customer", "branch"})
    @Query("SELECT o FROM SaleOrder o WHERE " +
           "(:includeDeleted = true OR o.isDeleted = false) AND " +
           "(:status IS NULL OR :status = '' OR o.status = :status) AND " +
           "(:branchId IS NULL OR o.branch.id = :branchId) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.customerPhone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.note) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<SaleOrder> findAllOrders(
            @Param("search") String search,
            @Param("status") String status,
            @Param("branchId") Long branchId,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);

    /**
     * Tính tổng doanh thu YTD theo mã phương thức thanh toán (chỉ đơn hàng không bị hủy và không bị xóa)
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM SaleOrder o " +
           "WHERE o.paymentMethodCode = :methodCode " +
           "AND FUNCTION('YEAR', o.createdAt) = :year " +
           "AND (o.isDeleted = false OR o.isDeleted IS NULL) " +
           "AND o.status <> 'CANCELLED'")
    Double sumYtdByPaymentMethodCode(@Param("methodCode") String methodCode, @Param("year") int year);
}

