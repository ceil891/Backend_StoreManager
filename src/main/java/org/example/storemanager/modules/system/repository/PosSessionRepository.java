package org.example.storemanager.modules.system.repository;

import org.example.storemanager.modules.system.entity.PosSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PosSessionRepository extends JpaRepository<PosSession, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"user", "branch"})
    Optional<PosSession> findByIdAndIsDeletedFalse(Long id);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"user", "branch"})
    java.util.List<PosSession> findByIsDeletedFalse();
}
