package org.example.storemanager.dto.response.partnerarea.area;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AreaListResponse {
    private Long id;
    private String areaCode;
    private String areaName;
    private Integer level;
    private String parentName;
    private Boolean isActive;
}