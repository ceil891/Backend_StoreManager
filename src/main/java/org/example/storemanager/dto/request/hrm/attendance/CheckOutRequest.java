package org.example.storemanager.dto.request.hrm.attendance;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckOutRequest {

    @NotNull
    private Long userId;
}
