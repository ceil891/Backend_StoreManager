package org.example.storemanager.dto.external;

import lombok.Data;
import java.util.List;

@Data
public class ProvinceDTO {
    private String code;
    private String name;
    private List<DistrictDTO> districts;
}