package org.example.storemanager.dto.response.catalog.attribute;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttributeValueResponse {

    private Long id;
    private Long attributeId;
    private String attributeCode;
    private String value;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String createdBy;
}
