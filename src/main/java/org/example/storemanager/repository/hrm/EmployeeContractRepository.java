package org.example.storemanager.repository.hrm;

import org.example.storemanager.entity.hrm.EmployeeContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface EmployeeContractRepository extends JpaRepository<EmployeeContract, Long> {
    Optional<EmployeeContract> findByIdAndIsDeletedFalse(Long id);
    List<EmployeeContract> findByIsDeletedFalse();
}
