package org.example.storemanager.modules.finance.repository;

import org.example.storemanager.modules.finance.entity.PaymentMethodBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentMethodBranchRepository extends JpaRepository<PaymentMethodBranch, Long> {

    List<PaymentMethodBranch> findByPaymentMethodId(Long paymentMethodId);

    @Query("SELECT pmb.branchId FROM PaymentMethodBranch pmb WHERE pmb.paymentMethodId = :paymentMethodId")
    List<Long> findBranchIdsByPaymentMethodId(@Param("paymentMethodId") Long paymentMethodId);

    @Query("SELECT pmb.paymentMethodId FROM PaymentMethodBranch pmb WHERE pmb.branchId = :branchId")
    List<Long> findPaymentMethodIdsByBranchId(@Param("branchId") Long branchId);

    @Modifying
    @Query("DELETE FROM PaymentMethodBranch pmb WHERE pmb.paymentMethodId = :paymentMethodId")
    void deleteByPaymentMethodId(@Param("paymentMethodId") Long paymentMethodId);

    boolean existsByPaymentMethodIdAndBranchId(Long paymentMethodId, Long branchId);
}
