package org.example.storemanager.modules.inventory.repository;

import org.example.storemanager.modules.inventory.entity.SupplierStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierStorageRepository extends JpaRepository<SupplierStorage, Long> {
    List<SupplierStorage> findByIsDeletedFalse();
    Optional<SupplierStorage> findByIdAndIsDeletedFalse(Long id);
    Optional<SupplierStorage> findByStorageCodeAndIsDeletedFalse(String storageCode);
}
