package org.example.storemanager.repository.catalog;

import org.example.storemanager.entity.catalog.Combo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComboRepository extends JpaRepository<Combo, Long> {

    Optional<Combo> findByIdAndIsDeletedFalse(Long id);

    boolean existsByComboCodeAndIsDeletedFalse(String comboCode);

    boolean existsByComboCodeAndIdNotAndIsDeletedFalse(String comboCode, Long id);

    @Query("""
            SELECT c FROM Combo c
            WHERE c.isDeleted = false
              AND (:isActive IS NULL OR c.isActive = :isActive)
              AND (:search IS NULL OR :search = '' OR
                   LOWER(c.comboCode) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(c.comboName) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Combo> search(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
}
