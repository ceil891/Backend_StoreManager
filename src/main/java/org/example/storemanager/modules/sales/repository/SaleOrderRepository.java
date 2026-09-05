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
    java.util.List<SaleOrder> findByCustomerIdAndIsDeletedFalseOrderByOrderDateDesc(Long customerId);
    java.util.List<SaleOrder> findByCustomerIdAndPaymentStatusNotAndIsDeletedFalseOrderByOrderDateDesc(Long customerId, String paymentStatus);
    java.util.List<SaleOrder> findByPosSessionIdAndIsDeletedFalse(Long posSessionId);

    @Query("SELECT COALESCE(SUM(COALESCE(o.finalAmount, o.totalAmount, 0)), 0) FROM SaleOrder o " +
           "WHERE o.posSessionId = :posSessionId " +
           "AND (o.isDeleted = false OR o.isDeleted IS NULL) " +
           "AND o.status <> 'CANCELLED'")
    java.math.BigDecimal sumSalesAmountByPosSessionId(@Param("posSessionId") Long posSessionId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"customer", "branch"})
    @Query("SELECT o FROM SaleOrder o WHERE " +
           "(:includeDeleted = true OR o.isDeleted = false) AND " +
           "(cast(:status as string) IS NULL OR cast(:status as string) = '' OR o.status = :status) AND " +
           "(cast(:branchId as long) IS NULL OR o.branch.id = :branchId) AND " +
           "(cast(:search as string) IS NULL OR cast(:search as string) = '' OR " +
           "LOWER(o.orderCode) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(o.customerName) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(o.customerPhone) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(o.note) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
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
           "AND o.createdAt >= :startDate AND o.createdAt < :endDate " +
           "AND (o.isDeleted = false OR o.isDeleted IS NULL) " +
           "AND o.status <> 'CANCELLED'")
    Double sumYtdByPaymentMethodCode(
            @Param("methodCode") String methodCode,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
}

