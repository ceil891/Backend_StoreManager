package org.example.storemanager.modules.wms.repository;

import org.example.storemanager.modules.wms.entity.DeliveryNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryNoteRepository extends JpaRepository<DeliveryNote, Long> {
    Optional<DeliveryNote> findByIdAndIsDeletedFalse(Long id);
    List<DeliveryNote> findByIsDeletedFalse();
}
