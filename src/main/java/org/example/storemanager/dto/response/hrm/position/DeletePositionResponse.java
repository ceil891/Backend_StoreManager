package org.example.storemanager.dto.response.hrm.position;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DeletePositionResponse {
    private Long id;
    private String positionCode;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
