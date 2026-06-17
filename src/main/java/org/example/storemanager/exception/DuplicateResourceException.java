package org.example.storemanager.exception;

import lombok.Getter;
import org.example.storemanager.enums.ErrorCode;

@Getter
public class DuplicateResourceException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s với %s = '%s' đã tồn tại", resourceName, fieldName, fieldValue));
        this.errorCode = ErrorCode.DUPLICATE_RESOURCE;
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
