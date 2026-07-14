package org.example.storemanager.dto.response.hrm.position;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PositionDropdownResponse {
    private Long id;
    private String positionName;
}
