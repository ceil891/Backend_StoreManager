package org.example.storemanager.modules.finance.repository;

import org.example.storemanager.modules.finance.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    Optional<Payroll> findByIdAndIsDeletedFalse(Long id);
    List<Payroll> findByIsDeletedFalse();
}
