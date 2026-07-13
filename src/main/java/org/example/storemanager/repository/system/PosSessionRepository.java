package org.example.storemanager.repository.system;

import org.example.storemanager.entity.system.PosSession;
import org.example.storemanager.enums.system.PosSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PosSessionRepository extends JpaRepository<PosSession, Long>, JpaSpecificationExecutor<PosSession> {
    Optional<PosSession> findTopByUser_IdAndStatusOrderByStartTimeDesc(Long userId, PosSessionStatus status);
    boolean existsByUser_IdAndStatus(Long userId, PosSessionStatus status);


}
