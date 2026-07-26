package org.example.storemanager.repository.catalog;

import org.example.storemanager.entity.catalog.SerialNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SerialNumberRepository extends JpaRepository<SerialNumber, Long> {
    Optional<SerialNumber> findBySerialNumberAndIsDeletedFalse(String serialNumber);
    List<SerialNumber> findByProductIdAndStatusAndIsDeletedFalse(Long productId, String status);
    List<SerialNumber> findByProductIdAndIsDeletedFalse(Long productId);
}
