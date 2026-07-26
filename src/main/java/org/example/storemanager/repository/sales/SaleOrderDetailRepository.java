package org.example.storemanager.repository.sales;

import org.example.storemanager.entity.sales.SaleOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaleOrderDetailRepository extends JpaRepository<SaleOrderDetail, Long> {
    Optional<SaleOrderDetail> findByIdAndIsDeletedFalse(Long id);
    List<SaleOrderDetail> findByOrderIdAndIsDeletedFalse(Long orderId);
}
