package org.example.storemanager.modules.wms.repository;

import org.example.storemanager.modules.wms.entity.LocationTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationTransferRepository extends JpaRepository<LocationTransfer, Long> {

    boolean existsByTransferCode(String transferCode);

    Optional<LocationTransfer> findByTransferCodeAndIsDeletedFalse(String transferCode);

    List<LocationTransfer> findByBranch_IdAndIsDeletedFalseOrderByTransferDateDesc(Long branchId);

    List<LocationTransfer> findByProductVariant_IdAndIsDeletedFalseOrderByTransferDateDesc(Long variantId);

    @Query("SELECT lt FROM LocationTransfer lt WHERE lt.fromBin.id = :binId OR lt.toBin.id = :binId ORDER BY lt.transferDate DESC")
    List<LocationTransfer> findByBin(@Param("binId") Long binId);

    @Query("SELECT COUNT(lt) FROM LocationTransfer lt WHERE lt.branch.id = :branchId AND lt.status = 'PENDING'")
    long countPendingByBranch(@Param("branchId") Long branchId);
}
