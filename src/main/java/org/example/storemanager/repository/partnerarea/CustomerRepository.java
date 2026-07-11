package org.example.storemanager.repository.partnerarea;

import org.example.storemanager.entity.partnerarea.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

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

    // 3. Các hàm bổ trợ
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
    // Thêm vào CustomerRepository.java
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByPhoneAndIdNot(String phone, Long id);
}
