package org.example.storemanager.dto.request.partnerarea.area;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAreaRequest {
    private String code;     // Thay cho areaCode
    private String name;     // Thay cho areaName
    private Integer level;
    private String type;     // Thêm trường này (PROVINCE, DISTRICT,...)
    private Long parentId;   // Để gắn kết cha con
    private Boolean isActive;
}