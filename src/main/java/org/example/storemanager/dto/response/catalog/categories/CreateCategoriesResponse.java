package org.example.storemanager.dto.response.catalog.categories;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.storemanager.dto.response.catalog.department.DepartmentResponse;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoriesResponse {
    private Long id;
    private String categoryCode;
    private String categoryName;
    private String description;
    private Boolean isActive;
    private DepartmentResponse department;
    private Long parentId;
    private String imageUrl;
    private LocalDateTime createdAt;
    private String createdBy;
}
