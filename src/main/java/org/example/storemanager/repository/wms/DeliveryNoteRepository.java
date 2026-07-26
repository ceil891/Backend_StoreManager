package org.example.storemanager.repository.wms;

import org.example.storemanager.entity.wms.DeliveryNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryNoteRepository extends JpaRepository<DeliveryNote, Long> {
    Optional<DeliveryNote> findByIdAndIsDeletedFalse(Long id);
    List<DeliveryNote> findByIsDeletedFalse();
}
