package org.example.storemanager.modules.hrm.repository;

import org.example.storemanager.modules.hrm.entity.EmployeeContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface EmployeeContractRepository extends JpaRepository<EmployeeContract, Long> {
    @Query("SELECT c FROM EmployeeContract c LEFT JOIN FETCH c.user LEFT JOIN FETCH c.position WHERE c.id = :id AND c.isDeleted = false")
    Optional<EmployeeContract> findByIdAndIsDeletedFalse(@Param("id") Long id);

    @Query("SELECT c FROM EmployeeContract c LEFT JOIN FETCH c.user LEFT JOIN FETCH c.position WHERE c.isDeleted = false ORDER BY c.id DESC")
    List<EmployeeContract> findByIsDeletedFalse();
}
