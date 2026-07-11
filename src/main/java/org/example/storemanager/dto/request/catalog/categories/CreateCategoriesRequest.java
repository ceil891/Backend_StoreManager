package org.example.storemanager.dto.request.catalog.categories;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class CreateCategoriesRequest {
    @NotBlank(message = "Mã danh mục không được để trống")
    @Size(max = 50, message = "Mã danh mục không được quá 50 ký tự")
    private String categoryCode;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 150, message = "Tên danh mục không được quá 150 ký tự")
    private String categoryName;

    private String description;
    private Long parentId;
    private Boolean isActive = true;
    private Long departmentId;
    private String imageUrl;
}