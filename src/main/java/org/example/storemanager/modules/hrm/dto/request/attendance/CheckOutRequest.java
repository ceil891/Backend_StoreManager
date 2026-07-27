package org.example.storemanager.modules.hrm.dto.request.attendance;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckOutRequest {

    @NotNull
    private Long userId;
}
