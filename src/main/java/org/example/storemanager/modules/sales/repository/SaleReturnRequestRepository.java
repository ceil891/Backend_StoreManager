package org.example.storemanager.modules.sales.repository;

import org.example.storemanager.modules.sales.entity.SaleReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaleReturnRequestRepository extends JpaRepository<SaleReturnRequest, Long> {
    List<SaleReturnRequest> findByIsDeletedFalseOrderByCreatedAtDesc();
    Optional<SaleReturnRequest> findByIdAndIsDeletedFalse(Long id);
    Optional<SaleReturnRequest> findByRequestCodeAndIsDeletedFalse(String requestCode);
}
