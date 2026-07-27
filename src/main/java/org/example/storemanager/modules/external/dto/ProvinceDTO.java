package org.example.storemanager.modules.external.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProvinceDTO {
    private String code;
    private String name;
    private List<DistrictDTO> districts;
}