package org.example.storemanager.modules.system.repository;

import org.example.storemanager.modules.system.entity.PrintTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PrintTemplateRepository extends JpaRepository<PrintTemplate, Long> {
    Optional<PrintTemplate> findByIdAndIsDeletedFalse(Long id);
    List<PrintTemplate> findByIsDeletedFalse();
}
