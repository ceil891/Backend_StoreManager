package org.example.storemanager.modules.hrm.dto.response.attendance;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DeleteAttendanceResponse {
    private Long id;
    private Long userId;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
