package org.example.storemanager.repository.warranty;

import org.example.storemanager.entity.warranty.WarrantyClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim, Long> {
    Optional<WarrantyClaim> findByIdAndIsDeletedFalse(Long id);
    List<WarrantyClaim> findByIsDeletedFalse();
}
