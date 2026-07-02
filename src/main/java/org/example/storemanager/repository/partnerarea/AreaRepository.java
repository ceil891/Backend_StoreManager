package org.example.storemanager.repository.partnerarea;

import org.example.storemanager.entity.partnerarea.Area;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AreaRepository extends JpaRepository<Area, Long> {
    boolean existsByAreaCode(String areaCode);

    List<Area> findByParentId(Long parentId);

    List<Area> findByLevel(Integer level);
}