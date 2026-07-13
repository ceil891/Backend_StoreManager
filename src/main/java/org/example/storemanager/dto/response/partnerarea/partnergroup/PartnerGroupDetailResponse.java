package org.example.storemanager.dto.response.partnerarea.partnergroup;

import lombok.Builder;
import lombok.Data;
import org.example.storemanager.dto.response.partnerarea.customer.CustomerInfo;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PartnerGroupDetailResponse {
    private Long id;
    private String groupCode;
    private String groupName;
    private String type;
    private String description;
    private Integer initialMemberCount;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private boolean success;
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private List<CustomerInfo> customers;
}