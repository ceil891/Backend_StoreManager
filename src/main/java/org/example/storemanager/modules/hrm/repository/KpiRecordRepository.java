package org.example.storemanager.modules.hrm.repository;

import org.example.storemanager.modules.hrm.entity.KpiRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface KpiRecordRepository extends JpaRepository<KpiRecord, Long> {
    Optional<KpiRecord> findByIdAndIsDeletedFalse(Long id);
    List<KpiRecord> findByIsDeletedFalse();
}
