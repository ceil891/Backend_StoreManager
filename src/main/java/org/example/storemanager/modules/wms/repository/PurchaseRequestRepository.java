package org.example.storemanager.modules.wms.repository;

import org.example.storemanager.modules.wms.entity.PurchaseRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {
    Optional<PurchaseRequest> findByIdAndIsDeletedFalse(Long id);
    java.util.List<PurchaseRequest> findByIsDeletedFalse();

    @Query("SELECT pr FROM PurchaseRequest pr WHERE " +
           "(:includeDeleted = true OR pr.isDeleted = false) AND " +
           "(:status IS NULL OR pr.status = :status) AND " +
           "(:branchId IS NULL OR pr.branch.id = :branchId) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(pr.requestCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pr.reason) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pr.note) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PurchaseRequest> findAllRequests(
            @Param("search") String search,
            @Param("status") String status,
            @Param("branchId") Long branchId,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}
