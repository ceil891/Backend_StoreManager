package org.example.storemanager.modules.hrm.dto.request.attendance;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckInRequest {

    @NotNull
    private Long userId;

    private String gpsLocation;

    private String deviceId;
}
