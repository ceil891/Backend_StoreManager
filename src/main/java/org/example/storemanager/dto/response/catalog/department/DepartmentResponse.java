package org.example.storemanager.dto.response.catalog.department;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {
    private Long id;
    private String deptCode;
    private String deptName;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // Trường hỗ trợ xem ngành hàng đã xóa mềm
    private Boolean isDeleted;
}
