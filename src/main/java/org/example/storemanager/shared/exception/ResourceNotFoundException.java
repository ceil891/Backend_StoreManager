package org.example.storemanager.shared.exception;

import lombok.Getter;
import org.example.storemanager.shared.enums.ErrorCode;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Không tìm thấy %s với %s = '%s'", resourceName, fieldName, fieldValue));
        this.errorCode = ErrorCode.RESOURCE_NOT_FOUND;
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public ResourceNotFoundException(ErrorCode errorCode, String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Không tìm thấy %s với %s = '%s'", resourceName, fieldName, fieldValue));
        this.errorCode = errorCode;
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
