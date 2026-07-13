package org.example.storemanager.dto.response.partnerarea.partnergroup;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UpdatePartnerGroupResponse {
    private Long id;
    private String groupCode;
    private String groupName;
    private String type;
    private String description;
    private Integer initialMemberCount;
    private Boolean isActive;
    private LocalDateTime updatedAt;
    private String updatedBy;

    private boolean success;
    private int status;
    private String message;
    private LocalDateTime timestamp;
}