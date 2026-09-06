package org.example.storemanager.shared.exception.inventory;

import java.math.BigDecimal;

public class InsufficientStockException extends RuntimeException {
    
    private final Long productId;
    private final BigDecimal requested;
    private final BigDecimal available;
    
    public InsufficientStockException(Long productId, BigDecimal requested, BigDecimal available) {
        super(String.format(
            "Sản phẩm #%d không đủ tồn kho: yêu cầu %s, hiện có %s",
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
