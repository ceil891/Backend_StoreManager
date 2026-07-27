package org.example.storemanager.modules.omnichannel.repository;

import org.example.storemanager.modules.omnichannel.entity.ShippingCarrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ShippingCarrierRepository extends JpaRepository<ShippingCarrier, Long> {
    Optional<ShippingCarrier> findByIdAndIsDeletedFalse(Long id);
    List<ShippingCarrier> findByIsDeletedFalse();
}
