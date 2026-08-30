package org.example.storemanager.modules.sales.repository;

import org.example.storemanager.modules.sales.entity.PurchaseOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderDetailRepository extends JpaRepository<PurchaseOrderDetail, Long> {
    Optional<PurchaseOrderDetail> findByIdAndIsDeletedFalse(Long id);
    List<PurchaseOrderDetail> findByIsDeletedFalse();
    List<PurchaseOrderDetail> findByPurchaseOrderIdAndIsDeletedFalse(Long poId);

    @org.springframework.data.jpa.repository.Query("SELECT pod.unitPrice FROM PurchaseOrderDetail pod " +
           "WHERE pod.product.id = :productId AND pod.purchaseOrder.supplier.id = :supplierId " +
           "AND pod.isDeleted = false ORDER BY pod.purchaseOrder.poDate DESC")
    List<java.math.BigDecimal> findPurchasePriceHistory(
            @org.springframework.data.repository.query.Param("supplierId") Long supplierId,
            @org.springframework.data.repository.query.Param("productId") Long productId);
}
