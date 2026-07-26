package org.example.storemanager.exception.inventory;

import java.time.LocalDate;

public class DuplicateCheckException extends RuntimeException {
    
    private final Long warehouseZoneId;
    private final LocalDate checkDate;
    
    public DuplicateCheckException(Long warehouseZoneId, LocalDate checkDate) {
        super(String.format(
            "Inventory check already exists for warehouse zone %d on %s",
            warehouseZoneId, checkDate
        ));
        this.warehouseZoneId = warehouseZoneId;
        this.checkDate = checkDate;
    }
    
    public Long getWarehouseZoneId() { return warehouseZoneId; }
    public LocalDate getCheckDate() { return checkDate; }
}
