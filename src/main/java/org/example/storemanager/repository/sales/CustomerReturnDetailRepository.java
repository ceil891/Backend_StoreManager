package org.example.storemanager.repository.sales;

import org.example.storemanager.entity.sales.CustomerReturnDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerReturnDetailRepository extends JpaRepository<CustomerReturnDetail, Long> {
    Optional<CustomerReturnDetail> findByIdAndIsDeletedFalse(Long id);
    List<CustomerReturnDetail> findByCustomerReturnIdAndIsDeletedFalse(Long returnId);
}
