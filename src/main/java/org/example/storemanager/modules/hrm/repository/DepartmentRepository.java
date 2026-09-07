package org.example.storemanager.modules.hrm.repository;

import org.example.storemanager.modules.hrm.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository("hrmDepartmentRepository")
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.manager WHERE d.isDeleted = false ORDER BY d.id DESC")
    List<Department> findByIsDeletedFalse();

    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.manager WHERE d.id = :id AND d.isDeleted = false")
    Optional<Department> findByIdAndIsDeletedFalse(@Param("id") Long id);

    Optional<Department> findByDeptCodeAndIsDeletedFalse(String deptCode);
    boolean existsByDeptCodeAndIsDeletedFalse(String deptCode);
}
