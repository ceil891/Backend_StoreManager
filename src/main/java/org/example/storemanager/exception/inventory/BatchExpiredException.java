package org.example.storemanager.exception.inventory;

import java.time.LocalDate;

public class BatchExpiredException extends RuntimeException {
    
    private final Long batchId;
    private final LocalDate expiryDate;
    
    public BatchExpiredException(Long batchId, LocalDate expiryDate) {
        super(String.format("Batch %d expired on %s", batchId, expiryDate));
        this.batchId = batchId;
        this.expiryDate = expiryDate;
    }
    
    public Long getBatchId() { return batchId; }
    public LocalDate getExpiryDate() { return expiryDate; }
}
