package org.example.storemanager.modules.partnerarea.repository;

import java.util.Optional;
import org.example.storemanager.modules.partnerarea.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByIdAndIsDeletedFalse(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Customer c WHERE c.id = :id AND (c.isDeleted = false OR c.isDeleted IS NULL)")
    Optional<Customer> findByIdForUpdate(@Param("id") Long id);

    // 1. Khai báo phương thức search đã dùng trong Service
    // Lưu ý: @Param("keyword") phải khớp với :keyword trong Query
    @Query("SELECT c FROM Customer c WHERE c.isActive = true AND " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR c.phone LIKE %:keyword%)")
    Page<Customer> searchCustomers(@Param("keyword") String keyword, Pageable pageable);
    // Chỉ lọc trạng thái hoạt động, không quan tâm bị xóa hay chưa
    Page<Customer> findByIsActive(Boolean isActive, Pageable pageable);
    // 2. Tìm kiếm khách hàng chưa bị xóa mềm
    Page<Customer> findByIsDeletedFalse(Pageable pageable);

    Page<Customer> findByIsActiveAndIsDeletedFalse(Boolean isActive, Pageable pageable);

    Optional<Customer> findByEmailAndIsDeletedFalse(String email);
    Optional<Customer> findByPhoneAndIsDeletedFalse(String phone);
    Optional<Customer> findByNameIgnoreCaseAndIsDeletedFalse(String name);
    Optional<Customer> findByCustomerCodeAndIsDeletedFalse(String customerCode);
    java.util.List<Customer> findByIsDeletedFalse();

    @Query("SELECT c FROM Customer c WHERE (c.isDeleted = false OR c.isDeleted IS NULL) AND " +
            "(:isActive IS NULL OR c.isActive = :isActive) AND " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR c.phone LIKE %:keyword% OR LOWER(c.customerCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Customer> searchAllCustomers(@Param("keyword") String keyword, @Param("isActive") Boolean isActive, Pageable pageable);

    // 3. Các hàm bổ trợ
    Optional<Customer> findByPhone(String phone);
    Optional<Customer> findByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
    // Thêm vào CustomerRepository.java
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByPhoneAndIdNot(String phone, Long id);
}
