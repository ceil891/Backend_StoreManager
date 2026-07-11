package org.example.storemanager.repository.catalog;

import org.example.storemanager.entity.catalog.ComboDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboDetailRepository extends JpaRepository<ComboDetail, Long> {

    List<ComboDetail> findByComboIdAndIsDeletedFalse(Long comboId);

    void deleteByComboId(Long comboId);
}
