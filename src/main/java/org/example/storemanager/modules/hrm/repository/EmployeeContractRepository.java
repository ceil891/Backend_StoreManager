package org.example.storemanager.modules.hrm.repository;

import org.example.storemanager.modules.hrm.entity.EmployeeContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface EmployeeContractRepository extends JpaRepository<EmployeeContract, Long> {
    Optional<EmployeeContract> findByIdAndIsDeletedFalse(Long id);
    List<EmployeeContract> findByIsDeletedFalse();
}
