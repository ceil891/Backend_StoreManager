package org.example.storemanager.repository.omnichannel;

import org.example.storemanager.entity.omnichannel.ShippingCarrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ShippingCarrierRepository extends JpaRepository<ShippingCarrier, Long> {
    Optional<ShippingCarrier> findByIdAndIsDeletedFalse(Long id);
    List<ShippingCarrier> findByIsDeletedFalse();
}
