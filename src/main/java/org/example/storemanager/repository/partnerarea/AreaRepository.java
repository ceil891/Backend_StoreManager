package org.example.storemanager.repository.partnerarea;

import org.example.storemanager.entity.partnerarea.Area;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaRepository extends JpaRepository<Area, Long> {
    boolean existsByAreaCode(String areaCode);
    Page<Area> findByIsActive(Boolean isActive, Pageable pageable);
}