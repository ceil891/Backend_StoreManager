package org.example.storemanager.shared.exception;

import lombok.Getter;
import org.example.storemanager.shared.enums.ErrorCode;

@Getter
public class JwtAuthenticationException extends RuntimeException {

    private final ErrorCode errorCode;

    public JwtAuthenticationException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public JwtAuthenticationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
