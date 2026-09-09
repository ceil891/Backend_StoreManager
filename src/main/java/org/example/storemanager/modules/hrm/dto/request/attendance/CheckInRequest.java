package org.example.storemanager.modules.hrm.dto.request.attendance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
public class CheckInRequest {

    @NotNull
    private Long userId;

    private String gpsLocation;

    private String deviceId;

    @AssertTrue(message = "Phải xác thực khuôn mặt trước khi vào ca")
    private Boolean faceVerified;
}
