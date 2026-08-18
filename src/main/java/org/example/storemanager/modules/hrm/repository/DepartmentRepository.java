package org.example.storemanager.modules.hrm.repository;

import org.example.storemanager.modules.hrm.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("hrmDepartmentRepository")
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByIsDeletedFalse();
    Optional<Department> findByIdAndIsDeletedFalse(Long id);
    Optional<Department> findByDeptCodeAndIsDeletedFalse(String deptCode);
    boolean existsByDeptCodeAndIsDeletedFalse(String deptCode);
}
