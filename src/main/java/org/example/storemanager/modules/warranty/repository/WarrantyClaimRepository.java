package org.example.storemanager.modules.warranty.repository;

import org.example.storemanager.modules.warranty.entity.WarrantyClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim, Long> {
    Optional<WarrantyClaim> findByIdAndIsDeletedFalse(Long id);
    List<WarrantyClaim> findByIsDeletedFalse();
}
