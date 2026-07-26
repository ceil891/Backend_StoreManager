package org.example.storemanager.repository.partnerarea;

import org.example.storemanager.entity.partnerarea.PartnerGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PartnerGroupRepository extends JpaRepository<PartnerGroup, Long> {
    Optional<PartnerGroup> findByIdAndIsDeletedFalse(Long id);
    List<PartnerGroup> findByIsDeletedFalse();
}
