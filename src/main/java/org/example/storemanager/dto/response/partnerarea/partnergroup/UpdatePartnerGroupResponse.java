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

    // Đầy đủ thông tin audit
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    private String message;
}