package org.example.storemanager.modules.sales.repository;

import org.example.storemanager.modules.sales.entity.QuoteSurvey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuoteSurveyRepository extends JpaRepository<QuoteSurvey, Long> {

    Optional<QuoteSurvey> findByIdAndIsDeletedFalse(Long id);

    boolean existsBySurveyCode(String surveyCode);

    @Query("SELECT s FROM QuoteSurvey s WHERE " +
           "(:includeDeleted = true OR s.isDeleted = false) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:branchId IS NULL OR s.branch.id = :branchId) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(s.surveyCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.customer.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.contactPhone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.note) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<QuoteSurvey> findAllSurveys(
            @Param("search") String search,
            @Param("status") String status,
            @Param("branchId") Long branchId,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}
