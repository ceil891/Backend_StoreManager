package org.example.storemanager.modules.partnerarea.repository;

import org.example.storemanager.modules.partnerarea.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {

    Optional<CustomerAddress> findByIdAndIsDeletedFalse(Long id);

    List<CustomerAddress> findByCustomerIdAndIsDeletedFalseOrderByIdDesc(Long customerId);

    List<CustomerAddress> findByCustomerPhoneAndIsDeletedFalseOrderByIdDesc(String customerPhone);

    @Query("SELECT a FROM CustomerAddress a WHERE a.isDeleted = false AND " +
           "((:customerId IS NOT NULL AND a.customerId = :customerId) OR " +
           " (:phone IS NOT NULL AND (a.customerPhone = :phone OR a.phoneNumber = :phone))) " +
           "ORDER BY a.isDefault DESC, a.id DESC")
    List<CustomerAddress> findByCustomerIdOrPhone(@Param("customerId") Long customerId, @Param("phone") String phone);

    @Modifying
    @Query("UPDATE CustomerAddress a SET a.isDefault = false WHERE a.isDeleted = false AND " +
           "((:customerId IS NOT NULL AND a.customerId = :customerId) OR " +
           " (:phone IS NOT NULL AND (a.customerPhone = :phone OR a.phoneNumber = :phone)))")
    void resetDefaultFlagForCustomer(@Param("customerId") Long customerId, @Param("phone") String phone);
}
