package org.example.storemanager.shared.exception.inventory;

public class InventoryNotFoundException extends RuntimeException {
    
    private final String resourceType;
    private final Long resourceId;
    
    public InventoryNotFoundException(String resourceType, Long resourceId) {
        super(String.format("%s not found with ID: %d", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    public String getResourceType() {
        return resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }
}
