package org.example.storemanager.shared.exception.inventory;

public class InvalidStatusTransitionException extends RuntimeException {
    
    private final String currentStatus;
    private final String targetStatus;
    
    public InvalidStatusTransitionException(String currentStatus, String targetStatus) {
        super(String.format(
            "Invalid status transition from '%s' to '%s'",
            currentStatus, targetStatus
        ));
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }
    
    public String getCurrentStatus() { return currentStatus; }
    public String getTargetStatus() { return targetStatus; }
}
