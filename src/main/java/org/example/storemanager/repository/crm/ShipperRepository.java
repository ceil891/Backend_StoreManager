package org.example.storemanager.repository.crm;

import org.example.storemanager.entity.crm.Shipper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ShipperRepository extends JpaRepository<Shipper, Long> {
    Optional<Shipper> findByIdAndIsDeletedFalse(Long id);
    List<Shipper> findByIsDeletedFalse();
}
