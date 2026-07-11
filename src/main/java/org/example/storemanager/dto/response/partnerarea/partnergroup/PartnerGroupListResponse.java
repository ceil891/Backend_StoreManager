package org.example.storemanager.dto.response.partnerarea.partnergroup;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartnerGroupListResponse {
    private Long id;
    private String groupCode;
    private String groupName;
    private String type;
    private Boolean isActive;
}