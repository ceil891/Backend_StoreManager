package org.example.storemanager.repository.wms;

import org.example.storemanager.entity.wms.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("wmsAreaRepository")
public interface AreaRepository extends JpaRepository<Area, Long> {

    boolean existsByAreaCode(String areaCode);

    Optional<Area> findByAreaCodeAndIsDeletedFalse(String areaCode);

    List<Area> findByZone_IdAndIsDeletedFalse(Long zoneId);

    List<Area> findByZone_Branch_IdAndIsDeletedFalse(Long branchId);

    @Query("SELECT a FROM WmsArea a WHERE a.isDeleted = false ORDER BY a.zone.zoneCode, a.areaCode")
    List<Area> findAllActive();

    @Query("SELECT a FROM WmsArea a WHERE a.zone.branch.id = :branchId AND a.isActive = true AND a.isDeleted = false ORDER BY a.areaCode")
    List<Area> findActiveByBranch(@Param("branchId") Long branchId);
}
