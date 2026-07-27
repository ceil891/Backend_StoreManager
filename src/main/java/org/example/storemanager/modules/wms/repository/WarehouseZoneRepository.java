package org.example.storemanager.modules.wms.repository;

import org.example.storemanager.modules.wms.entity.WarehouseZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseZoneRepository extends JpaRepository<WarehouseZone, Long> {

    Optional<WarehouseZone> findByBranchIdAndZoneCodeAndIsDeletedFalse(Long branchId, String zoneCode);

    List<WarehouseZone> findByBranchIdAndIsDeletedFalse(Long branchId);

    Optional<WarehouseZone> findFirstByBranchIdAndIsDeletedFalseOrderByIdAsc(Long branchId);

    Optional<WarehouseZone> findByIdAndIsDeletedFalse(Long id);
}
