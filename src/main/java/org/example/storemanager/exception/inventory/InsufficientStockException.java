package org.example.storemanager.exception.inventory;

import java.math.BigDecimal;

public class InsufficientStockException extends RuntimeException {
    
    private final Long productId;
    private final BigDecimal requested;
    private final BigDecimal available;
    
    public InsufficientStockException(Long productId, BigDecimal requested, BigDecimal available) {
        super(String.format(
            "Insufficient stock for product %d: requested %s, available %s",
            productId, requested, available
        ));
        this.productId = productId;
        this.requested = requested;
        this.available = available;
    }
    
    public Long getProductId() { return productId; }
    public BigDecimal getRequested() { return requested; }
    public BigDecimal getAvailable() { return available; }
}
