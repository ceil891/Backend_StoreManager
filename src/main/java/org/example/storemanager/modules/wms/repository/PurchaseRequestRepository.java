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
           "(cast(:status as string) IS NULL OR pr.status = cast(:status as string)) AND " +
           "(cast(:branchId as long) IS NULL OR pr.branch.id = cast(:branchId as long)) AND " +
           "(cast(:search as string) IS NULL OR cast(:search as string) = '' OR " +
           "LOWER(pr.requestCode) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(pr.reason) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(pr.note) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    Page<PurchaseRequest> findAllRequests(
            @Param("search") String search,
            @Param("status") String status,
            @Param("branchId") Long branchId,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}
