package org.example.storemanager.modules.warranty.repository;

import org.example.storemanager.modules.warranty.entity.ProductWarranty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ProductWarrantyRepository extends JpaRepository<ProductWarranty, Long> {
    Optional<ProductWarranty> findByIdAndIsDeletedFalse(Long id);
    List<ProductWarranty> findByIsDeletedFalse();
    Optional<ProductWarranty> findByWarrantyCodeAndIsDeletedFalse(String warrantyCode);
    Optional<ProductWarranty> findBySerialNumber_SerialNumberAndIsDeletedFalse(String serialNumber);
}
