package org.example.storemanager.modules.wms.repository;

import org.example.storemanager.modules.wms.entity.Rack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RackRepository extends JpaRepository<Rack, Long> {

    boolean existsByRackCode(String rackCode);

    Optional<Rack> findByRackCodeAndIsDeletedFalse(String rackCode);

    List<Rack> findByArea_IdAndIsDeletedFalse(Long areaId);

    @Query("SELECT r FROM Rack r WHERE r.isDeleted = false ORDER BY r.area.areaCode, r.rackCode")
    List<Rack> findAllActive();

    @Query("SELECT r FROM Rack r WHERE r.area.zone.branch.id = :branchId AND r.isActive = true AND r.isDeleted = false ORDER BY r.rackCode")
    List<Rack> findActiveByBranch(@Param("branchId") Long branchId);
}
