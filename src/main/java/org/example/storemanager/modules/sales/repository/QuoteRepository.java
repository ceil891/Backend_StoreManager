package org.example.storemanager.modules.sales.repository;

import org.example.storemanager.modules.sales.entity.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    Optional<Quote> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT q FROM Quote q WHERE " +
           "(:includeDeleted = true OR q.isDeleted = false) AND " +
           "(:status IS NULL OR q.status = :status) AND " +
           "(:branchId IS NULL OR q.branch.id = :branchId) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(q.quoteCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(q.customer.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(q.note) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Quote> findAllQuotes(
            @Param("search") String search,
            @Param("status") String status,
            @Param("branchId") Long branchId,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}
