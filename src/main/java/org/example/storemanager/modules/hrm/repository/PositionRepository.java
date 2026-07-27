package org.example.storemanager.modules.hrm.repository;

import org.example.storemanager.modules.hrm.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    Optional<Position> findByIdAndIsDeletedFalse(Long id);
    List<Position> findByIsDeletedFalse();
}
