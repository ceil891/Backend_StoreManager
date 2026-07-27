package org.example.storemanager.modules.warranty.repository;

import org.example.storemanager.modules.warranty.entity.WarrantyAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface WarrantyAttachmentRepository extends JpaRepository<WarrantyAttachment, Long> {
    Optional<WarrantyAttachment> findByIdAndIsDeletedFalse(Long id);
    List<WarrantyAttachment> findByIsDeletedFalse();
}
