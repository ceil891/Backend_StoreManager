package org.example.storemanager.dto.response.partnerarea.partnergroup;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PartnerGroupListResponse {
    private Long id;
    private String groupCode;
    private String groupName;
    private String type;
    private Boolean isActive;
}