package org.example.storemanager.modules.catalog.dto.response.categories;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import org.example.storemanager.modules.catalog.dto.response.department.DepartmentResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriesResponse {
    private Long id;
    private String categoryCode;
    private String categoryName;
    private String description;
    private Boolean isActive;
    private DepartmentResponse department;
    private Long parentId;
    private String imageUrl;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime updatedAt;

    private List<CategoriesResponse> children;
}
