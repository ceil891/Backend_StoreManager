package org.example.storemanager.dto.response.hrm.attendance;

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
