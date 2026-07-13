package org.example.storemanager.exception;

import lombok.Getter;
import org.example.storemanager.enums.ErrorCode;

@Getter
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String detailMessage; // Thêm trường này để chứa lỗi chi tiết

    public AppException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.detailMessage = errorCode.getDefaultMessage();
    }

    // Constructor này cho phép cậu ném thêm chi tiết (ví dụ: "Tên nhóm 'VIP' đã tồn tại")
    public AppException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }
}
