package org.example.storemanager.dto.response.partnerarea.area;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class AreaListResponse {
    private Long id;
    @JsonProperty("code") private String areaCode;
    @JsonProperty("name") private String areaName;
    private String fullName;
    @JsonIgnore
    private String type;
    private Integer level;
    private Boolean isActive;
    private String parentName;
    @Builder.Default private List<AreaListResponse> children = new ArrayList<>();
}