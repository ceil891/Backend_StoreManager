package org.example.storemanager.modules.wms.repository;

import org.example.storemanager.modules.wms.entity.ProductLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductLocationRepository extends JpaRepository<ProductLocation, Long> {
    List<ProductLocation> findByProductIdAndIsDeletedFalse(Long productId);
    List<ProductLocation> findByBinIdAndIsDeletedFalse(Long binId);
    Optional<ProductLocation> findByProductIdAndBinIdAndIsDeletedFalse(Long productId, Long binId);
}
