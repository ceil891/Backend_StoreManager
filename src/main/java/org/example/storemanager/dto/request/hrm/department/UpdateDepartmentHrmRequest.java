package org.example.storemanager.dto.request.hrm.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateDepartmentHrmRequest {

    @NotBlank(message = "Mã phòng ban không được để trống")
    @Size(max = 50)
    private String deptCode;

    @NotBlank(message = "Tên phòng ban không được để trống")
    @Size(max = 150)
    private String deptName;

    private String description;

    private Long managerId;

    private Boolean isActive;
}
