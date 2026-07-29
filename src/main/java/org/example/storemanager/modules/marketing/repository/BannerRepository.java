package org.example.storemanager.modules.marketing.repository;

import org.example.storemanager.modules.marketing.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByIsActiveTrueOrderBySortOrderAsc();
    List<Banner> findAllByOrderBySortOrderAsc();
}
