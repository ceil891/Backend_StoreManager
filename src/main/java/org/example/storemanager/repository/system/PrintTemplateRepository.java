package org.example.storemanager.repository.system;

import org.example.storemanager.entity.system.PrintTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PrintTemplateRepository extends JpaRepository<PrintTemplate, Long> {
    Optional<PrintTemplate> findByIdAndIsDeletedFalse(Long id);
    List<PrintTemplate> findByIsDeletedFalse();
}
