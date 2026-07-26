package org.example.storemanager.repository.warranty;

import org.example.storemanager.entity.warranty.WarrantyPartReplacement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface WarrantyPartReplacementRepository extends JpaRepository<WarrantyPartReplacement, Long> {
    Optional<WarrantyPartReplacement> findByIdAndIsDeletedFalse(Long id);
    List<WarrantyPartReplacement> findByIsDeletedFalse();
}
