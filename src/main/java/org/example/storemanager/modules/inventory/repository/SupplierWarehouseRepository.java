package org.example.storemanager.modules.inventory.repository;

import org.example.storemanager.modules.inventory.entity.SupplierWarehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierWarehouseRepository extends JpaRepository<SupplierWarehouse, Long> {
    List<SupplierWarehouse> findByIsDeletedFalse();
    Optional<SupplierWarehouse> findByIdAndIsDeletedFalse(Long id);
    Optional<SupplierWarehouse> findByWarehouseCodeAndIsDeletedFalse(String warehouseCode);
}
