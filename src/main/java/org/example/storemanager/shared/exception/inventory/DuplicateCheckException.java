package org.example.storemanager.shared.exception.inventory;

import java.time.LocalDate;

public class DuplicateCheckException extends RuntimeException {
    
    private final Long warehouseZoneId;
    private final LocalDate checkDate;
    
    public DuplicateCheckException(Long warehouseZoneId, LocalDate checkDate) {
        super(String.format(
            "Phiếu kiểm kho đã tồn tại cho khu vực kho %d vào ngày %s",
            warehouseZoneId, checkDate
        ));
        this.warehouseZoneId = warehouseZoneId;
        this.checkDate = checkDate;
    }
    
    public Long getWarehouseZoneId() { return warehouseZoneId; }
    public LocalDate getCheckDate() { return checkDate; }
}
