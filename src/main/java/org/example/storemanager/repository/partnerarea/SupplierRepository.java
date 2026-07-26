package org.example.storemanager.repository.partnerarea;

import org.example.storemanager.entity.partnerarea.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByIdAndIsDeletedFalse(Long id);
    Page<Supplier> findByIsActive(Boolean isActive, Pageable pageable);
    boolean existsBySupplierCode(String supplierCode);

    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByName(String name);
    boolean existsByBankNameAndBankAccount(String bankName, String bankAccount);

    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsByPhoneAndIdNot(String phone, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByBankNameAndBankAccountAndIdNot(String bankName, String bankAccount, Long id);
    @Query("SELECT s FROM Supplier s WHERE (:isActive IS NULL OR s.isActive = :isActive) AND " +
            "(LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR s.supplierCode LIKE %:keyword%)")
    Page<Supplier> searchSuppliers(@Param("isActive") Boolean isActive, @Param("keyword") String keyword, Pageable pageable);
}