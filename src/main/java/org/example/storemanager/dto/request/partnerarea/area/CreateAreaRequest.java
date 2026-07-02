package org.example.storemanager.dto.request.partnerarea.area;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAreaRequest {
    // Xóa hết @NotBlank, @Size hoặc bất kỳ annotation nào ở đây
    private String areaCode;

    @NotBlank(message = "Tên khu vực không được để trống")
    private String areaName;

    @NotNull(message = "Cấp độ không được để trống")
    private Integer level;

    private Long parentId;
}