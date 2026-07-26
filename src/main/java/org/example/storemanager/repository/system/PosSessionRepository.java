package org.example.storemanager.repository.system;

import org.example.storemanager.entity.system.PosSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PosSessionRepository extends JpaRepository<PosSession, Long> {
    Optional<PosSession> findByIdAndIsDeletedFalse(Long id);
    java.util.List<PosSession> findByIsDeletedFalse();
}
