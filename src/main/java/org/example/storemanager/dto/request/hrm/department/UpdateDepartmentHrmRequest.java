package org.example.storemanager.dto.request.hrm.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateDepartmentHrmRequest {

    @Size(max = 50, message = "Mã phòng ban tối đa 50 ký tự")
    private String deptCode;

    @Size(max = 150, message = "Tên phòng ban tối đa 150 ký tự")
    private String deptName;

    private String description;

    private Long managerId;

    private Boolean isActive;
}
