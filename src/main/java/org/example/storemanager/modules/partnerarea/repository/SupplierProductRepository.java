package org.example.storemanager.modules.partnerarea.repository;

import org.example.storemanager.modules.partnerarea.entity.SupplierProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierProductRepository extends JpaRepository<SupplierProduct, Long> {

    boolean existsBySupplier_IdAndProduct_Id(Long supplierId, Long productId);

    Optional<SupplierProduct> findBySupplier_IdAndProduct_IdAndIsDeletedFalse(Long supplierId, Long productId);

    List<SupplierProduct> findBySupplier_IdAndIsDeletedFalse(Long supplierId);

    List<SupplierProduct> findByProduct_IdAndIsDeletedFalse(Long productId);

    @Query("SELECT sp FROM SupplierProduct sp WHERE sp.product.id = :productId AND sp.isPreferred = true AND sp.isActive = true AND sp.isDeleted = false")
    Optional<SupplierProduct> findPreferredSupplierForProduct(@Param("productId") Long productId);

    @Query("SELECT sp FROM SupplierProduct sp WHERE sp.isDeleted = false ORDER BY sp.supplier.name, sp.product.name")
    List<SupplierProduct> findAllActive();
}
