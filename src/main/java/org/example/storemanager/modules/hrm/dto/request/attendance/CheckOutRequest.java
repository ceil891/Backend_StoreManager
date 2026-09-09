package org.example.storemanager.modules.hrm.dto.request.attendance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
public class CheckOutRequest {

    @NotNull
    private Long userId;

    @AssertTrue(message = "Phải xác thực khuôn mặt đã đăng ký trước khi tan ca")
    private Boolean faceVerified;
}
