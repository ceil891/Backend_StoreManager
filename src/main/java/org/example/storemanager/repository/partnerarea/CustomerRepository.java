package org.example.storemanager.repository.partnerarea;

import org.example.storemanager.entity.partnerarea.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // 1. Tìm kiếm khách hàng theo tên hoặc SĐT
    @Query("SELECT c FROM Customer c WHERE c.isActive = true AND " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR c.phone LIKE %:keyword%)")
    Page<Customer> searchCustomers(String keyword, Pageable pageable);

    // 2. Lấy danh sách đang hoạt động (isActive = true)
    Page<Customer> findByIsActiveTrue(Pageable pageable);

    // 3. PHƯƠNG THỨC BỊ THIẾU: Dùng để lấy danh sách khách hàng chưa bị xóa mềm (isDeleted = false)
    Page<Customer> findByIsDeletedFalse(Pageable pageable);

    // 4. Kiểm tra trùng SĐT
    boolean existsByPhone(String phone);
}