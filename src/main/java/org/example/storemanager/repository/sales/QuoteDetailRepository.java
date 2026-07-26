package org.example.storemanager.repository.sales;

import org.example.storemanager.entity.sales.QuoteDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteDetailRepository extends JpaRepository<QuoteDetail, Long> {
    Optional<QuoteDetail> findByIdAndIsDeletedFalse(Long id);
    List<QuoteDetail> findByQuoteIdAndIsDeletedFalse(Long quoteId);
}
