package org.example.storemanager.repository.warranty;

import org.example.storemanager.entity.warranty.WarrantyAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface WarrantyAttachmentRepository extends JpaRepository<WarrantyAttachment, Long> {
    Optional<WarrantyAttachment> findByIdAndIsDeletedFalse(Long id);
    List<WarrantyAttachment> findByIsDeletedFalse();
}
