package org.example.storemanager.repository.advancedaccounting;

import org.example.storemanager.entity.advancedaccounting.AssetMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface AssetMaintenanceRepository extends JpaRepository<AssetMaintenance, Long> {
    Optional<AssetMaintenance> findByIdAndIsDeletedFalse(Long id);
    List<AssetMaintenance> findByIsDeletedFalse();
}
