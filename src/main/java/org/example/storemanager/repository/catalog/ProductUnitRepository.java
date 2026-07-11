package org.example.storemanager.repository.catalog;

import org.example.storemanager.entity.catalog.ProductUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductUnitRepository extends JpaRepository<ProductUnit, Long> {

    List<ProductUnit> findByProductIdAndIsDeletedFalse(Long productId);

    Optional<ProductUnit> findByIdAndIsDeletedFalse(Long id);

    boolean existsByProductIdAndUnitIdAndIsDeletedFalse(Long productId, Long unitId);

    boolean existsByBarcodeAndIsDeletedFalse(String barcode);

    boolean existsByBarcodeAndIdNotAndIsDeletedFalse(String barcode, Long id);

    Optional<ProductUnit> findByProductIdAndIsBaseUnitTrueAndIsDeletedFalse(Long productId);

    Optional<ProductUnit> findByProductIdAndUnitIdAndIsDeletedFalse(Long productId, Long unitId);
}
