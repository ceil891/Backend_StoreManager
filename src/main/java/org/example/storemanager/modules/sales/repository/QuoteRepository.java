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
    boolean existsByQuoteCode(String quoteCode);

    @Query("SELECT q FROM Quote q WHERE " +
           "(:includeDeleted = true OR q.isDeleted = false) AND " +
           "(cast(:status as string) IS NULL OR q.status = cast(:status as string)) AND " +
           "(cast(:branchId as long) IS NULL OR q.branch.id = cast(:branchId as long)) AND " +
           "(cast(:search as string) IS NULL OR cast(:search as string) = '' OR " +
           "LOWER(q.quoteCode) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(q.customer.name) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "LOWER(q.note) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    Page<Quote> findAllQuotes(
            @Param("search") String search,
            @Param("status") String status,
            @Param("branchId") Long branchId,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}
