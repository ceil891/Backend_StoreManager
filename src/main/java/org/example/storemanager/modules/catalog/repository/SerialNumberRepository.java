package org.example.storemanager.modules.catalog.repository;

import org.example.storemanager.modules.catalog.entity.SerialNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SerialNumberRepository extends JpaRepository<SerialNumber, Long> {
    Optional<SerialNumber> findBySerialNumberAndIsDeletedFalse(String serialNumber);
    List<SerialNumber> findByIsDeletedFalse();
    List<SerialNumber> findByStatusAndIsDeletedFalse(String status);
    List<SerialNumber> findByProductIdAndStatusAndIsDeletedFalse(Long productId, String status);
    List<SerialNumber> findByProductIdAndIsDeletedFalse(Long productId);
}
