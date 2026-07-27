package org.example.storemanager.modules.catalog.repository;

import org.example.storemanager.modules.catalog.entity.ComboDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboDetailRepository extends JpaRepository<ComboDetail, Long> {

    List<ComboDetail> findByComboIdAndIsDeletedFalse(Long comboId);

    void deleteByComboId(Long comboId);
}
