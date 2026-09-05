package org.example.storemanager.modules.purchase.repository;

import org.example.storemanager.modules.purchase.entity.SupplierRequestDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRequestDetailRepository extends JpaRepository<SupplierRequestDetail, Long> {
    List<SupplierRequestDetail> findBySupplierRequestIdAndIsDeletedFalse(Long supplierRequestId);
}
