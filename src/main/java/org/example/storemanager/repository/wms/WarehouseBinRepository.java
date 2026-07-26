package org.example.storemanager.repository.wms;

import org.example.storemanager.entity.wms.WarehouseBin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseBinRepository extends JpaRepository<WarehouseBin, Long> {

    List<WarehouseBin> findByIsDeletedFalse();

    Optional<WarehouseBin> findByIdAndIsDeletedFalse(Long id);

    Optional<WarehouseBin> findByBinCodeAndIsDeletedFalse(String binCode);

    /** Lấy tất cả bins của 1 rack */
    List<WarehouseBin> findByRack_IdAndIsDeletedFalse(Long rackId);

    /** Lấy tất cả bins của 1 area (thông qua rack) */
    @Query("SELECT b FROM WarehouseBin b WHERE b.rack.area.id = :areaId AND b.isDeleted = false")
    List<WarehouseBin> findByAreaId(@Param("areaId") Long areaId);

    /** Lấy tất cả bins của 1 zone (thông qua area → rack) */
    @Query("SELECT b FROM WarehouseBin b WHERE b.rack.area.zone.id = :zoneId AND b.isDeleted = false")
    List<WarehouseBin> findByZoneId(@Param("zoneId") Long zoneId);

    /** Lấy tất cả bins của 1 branch */
    @Query("SELECT b FROM WarehouseBin b WHERE b.rack.area.zone.branch.id = :branchId AND b.isDeleted = false ORDER BY b.binCode")
    List<WarehouseBin> findByBranchId(@Param("branchId") Long branchId);
}
