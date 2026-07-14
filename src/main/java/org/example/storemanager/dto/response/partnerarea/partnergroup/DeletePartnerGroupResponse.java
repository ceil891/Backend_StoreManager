package org.example.storemanager.dto.response.partnerarea.partnergroup;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DeletePartnerGroupResponse {
    private Long id;
    private LocalDateTime deletedAt;
    private String deletedBy;
    private Boolean isDeleted;
    private Boolean isActive;
}