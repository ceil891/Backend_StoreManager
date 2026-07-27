package org.example.storemanager.modules.wms.repository;

import org.example.storemanager.modules.wms.entity.PackingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackingListRepository extends JpaRepository<PackingList, Long> {
    Optional<PackingList> findByIdAndIsDeletedFalse(Long id);
    List<PackingList> findByIsDeletedFalse();
}
