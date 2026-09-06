package org.example.storemanager.shared.exception.inventory;

public class InvalidStatusTransitionException extends RuntimeException {
    
    private final String currentStatus;
    private final String targetStatus;
    
    public InvalidStatusTransitionException(String currentStatus, String targetStatus) {
        super(String.format(
            "Trạng thái chuyển đổi không hợp lệ từ '%s' sang '%s'",
            currentStatus, targetStatus
        ));
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }
    
    public String getCurrentStatus() { return currentStatus; }
    public String getTargetStatus() { return targetStatus; }
}
