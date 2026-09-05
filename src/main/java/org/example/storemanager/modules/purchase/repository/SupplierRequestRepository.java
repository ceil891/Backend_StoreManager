package org.example.storemanager.modules.purchase.repository;

import org.example.storemanager.modules.purchase.entity.SupplierRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRequestRepository extends JpaRepository<SupplierRequest, Long> {

    @Query("SELECT DISTINCT r FROM SupplierRequest r LEFT JOIN FETCH r.details WHERE r.isDeleted = false ORDER BY r.createdAt DESC")
    List<SupplierRequest> findAllWithDetails();

    @Query("SELECT r FROM SupplierRequest r LEFT JOIN FETCH r.details WHERE r.id = :id AND r.isDeleted = false")
    Optional<SupplierRequest> findByIdWithDetails(@Param("id") Long id);

    Optional<SupplierRequest> findByIdAndIsDeletedFalse(Long id);
    Optional<SupplierRequest> findByRfqCodeAndIsDeletedFalse(String rfqCode);
}
