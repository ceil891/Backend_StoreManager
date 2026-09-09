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

    @Modifying
    @Query("UPDATE CustomerAddress a SET a.isDefault = false WHERE a.isDeleted = false AND a.customerId = :customerId")
    void resetDefaultFlagForCustomerId(@Param("customerId") Long customerId);

    @Modifying
    @Query("UPDATE CustomerAddress a SET a.isDefault = false WHERE a.isDeleted = false AND a.customerPhone = :phone")
    void resetDefaultFlagForCustomerPhone(@Param("phone") String phone);
}
