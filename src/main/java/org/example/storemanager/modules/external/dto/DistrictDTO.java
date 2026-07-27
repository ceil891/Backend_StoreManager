package org.example.storemanager.modules.external.dto;

import lombok.Data;
import java.util.List;

@Data
public class DistrictDTO {
    private String code;
    private String name;
    private List<WardDTO> wards;
}