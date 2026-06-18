package org.example.storemanager.dto.request.catalog.categories;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.storemanager.entity.catalog.ProductCategory;

public class CreateCategoriesRequest {
    @NotBlank(message = "Mã danh mục không được để trống ")
    @Size(max = 30 ,message = "Mã danh mục không được vượt quá 30 ký tự ")
    private String categoryCode;
    @NotBlank(message = "Tên danh mục không được để trống ")
    @Size(max = 50 ,message = "Tên danh mục không được vượt quá 50 ký tự ")
    private String categoryName;

    private String description;


    private String department;

    private String manager;

    private String inventoryGlCode;

    private String cogsGlCode;


}
