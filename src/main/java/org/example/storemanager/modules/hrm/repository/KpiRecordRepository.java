package org.example.storemanager.modules.hrm.repository;

import org.example.storemanager.modules.hrm.entity.KpiRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface KpiRecordRepository extends JpaRepository<KpiRecord, Long> {
    @Query("SELECT k FROM KpiRecord k LEFT JOIN FETCH k.user WHERE k.id = :id AND k.isDeleted = false")
    Optional<KpiRecord> findByIdAndIsDeletedFalse(@Param("id") Long id);

    @Query("SELECT k FROM KpiRecord k LEFT JOIN FETCH k.user WHERE k.isDeleted = false ORDER BY k.id DESC")
    List<KpiRecord> findByIsDeletedFalse();
}
