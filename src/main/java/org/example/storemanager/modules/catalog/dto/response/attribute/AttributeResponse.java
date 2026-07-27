package org.example.storemanager.modules.catalog.dto.response.attribute;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttributeResponse {

    private Long id;
    private String attributeName;
    private String attributeCode;
    private String attributeType;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private String createdBy;

    // Only populated in detailed view
    private List<AttributeValueResponse> values;
}
