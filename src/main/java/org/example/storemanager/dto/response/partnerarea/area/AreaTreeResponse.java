package org.example.storemanager.dto.response.partnerarea.area;

import lombok.Data;
import org.example.storemanager.entity.partnerarea.Area;

import java.util.List;

@Data
public class AreaTreeResponse {
    private Long id;
    private String name;
    private List<AreaTreeResponse> children;

    public AreaTreeResponse(Area a) {
        this.id = a.getId();
        this.name = a.getAreaName();
    }
}
