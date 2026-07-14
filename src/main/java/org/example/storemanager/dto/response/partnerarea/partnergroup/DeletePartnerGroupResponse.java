package org.example.storemanager.dto.response.partnerarea.partnergroup;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DeletePartnerGroupResponse {
    private Long id;
    private String message;
    private LocalDateTime deletedAt;
    private String deletedBy;
    private boolean success;
    private int status;
    private LocalDateTime timestamp;
    private Boolean isDeleted;
    private Boolean isActive;

}