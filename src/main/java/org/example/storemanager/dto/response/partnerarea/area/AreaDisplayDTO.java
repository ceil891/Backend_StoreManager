package org.example.storemanager.dto.response.partnerarea.area;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
// Filter: Chỉ hiển thị các trường không bị null
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AreaDisplayDTO {
    private Long id;
    private String name;
    private String code;
    private String type;
    private Integer level;

    // Chỉ hiển thị children nếu nó không trống
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<AreaDisplayDTO> children;
}