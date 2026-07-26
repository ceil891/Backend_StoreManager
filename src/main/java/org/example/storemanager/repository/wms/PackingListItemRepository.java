package org.example.storemanager.repository.wms;

import org.example.storemanager.entity.wms.PackingListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackingListItemRepository extends JpaRepository<PackingListItem, Long> {
    List<PackingListItem> findByPackingListIdAndIsDeletedFalse(Long packingListId);
    Optional<PackingListItem> findByIdAndIsDeletedFalse(Long id);
    void deleteByPackingListId(Long packingListId);
}
