package org.example.storemanager.modules.wms.repository;

import org.example.storemanager.modules.wms.entity.PurchaseRequestDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRequestDetailRepository extends JpaRepository<PurchaseRequestDetail, Long> {
    Optional<PurchaseRequestDetail> findByIdAndIsDeletedFalse(Long id);
    List<PurchaseRequestDetail> findByPurchaseRequestIdAndIsDeletedFalse(Long requestId);
}
