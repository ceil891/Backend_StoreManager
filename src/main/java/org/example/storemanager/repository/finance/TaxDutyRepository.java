package org.example.storemanager.repository.finance;

import org.example.storemanager.entity.finance.TaxDuty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface TaxDutyRepository extends JpaRepository<TaxDuty, Long> {
    Optional<TaxDuty> findByIdAndIsDeletedFalse(Long id);
    List<TaxDuty> findByIsDeletedFalse();
}
