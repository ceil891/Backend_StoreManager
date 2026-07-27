package org.example.storemanager.modules.catalog.repository;

import org.example.storemanager.modules.catalog.entity.PriceListDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceListDetailRepository extends JpaRepository<PriceListDetail, Long> {

    List<PriceListDetail> findByPriceListIdAndIsDeletedFalse(Long priceListId);

    List<PriceListDetail> findByProductUnitIsNull();

    void deleteByPriceListId(Long priceListId);
}
