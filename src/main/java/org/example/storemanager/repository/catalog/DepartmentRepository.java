package org.example.storemanager.repository.catalog;

import org.example.storemanager.entity.catalog.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByIdAndIsDeletedFalse(Long id);

    Optional<Department> findById(Long id);

    boolean existsByDeptCodeAndIsDeletedFalse(String deptCode);

    boolean existsByDeptCodeAndIdNotAndIsDeletedFalse(String deptCode, Long id);

    // ==== Query CHỈ lấy chưa xóa (isDeleted = false) ====
    @Query("SELECT d FROM CatalogDepartment  d WHERE d.isDeleted = false AND " +
           "(:isActive IS NULL OR d.isActive = :isActive) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(d.deptName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.deptCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Department> findAllDepartments(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("SELECT d FROM CatalogDepartment d WHERE d.isDeleted = false AND " +
           "(:isActive IS NULL OR d.isActive = :isActive) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(d.deptName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.deptCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Department> findAllDepartmentsList(
            @Param("search") String search,
            @Param("isActive") Boolean isActive);

    // ==== Query lấy TẤT CẢ kể cả đã xóa (includeDeleted = true) ====
    @Query("SELECT d FROM CatalogDepartment d WHERE " +
           "(:includeDeleted = true OR d.isDeleted = false) AND " +
           "(:isActive IS NULL OR d.isActive = :isActive) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(d.deptName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.deptCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Department> findAllDepartmentsIncludeDeleted(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);

    @Query("SELECT d FROM CatalogDepartment d WHERE " +
           "(:includeDeleted = true OR d.isDeleted = false) AND " +
           "(:isActive IS NULL OR d.isActive = :isActive) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(d.deptName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.deptCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Department> findAllDepartmentsListIncludeDeleted(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("includeDeleted") boolean includeDeleted);
}
