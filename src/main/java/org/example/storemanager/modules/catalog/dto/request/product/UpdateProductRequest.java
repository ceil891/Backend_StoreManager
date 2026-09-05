package org.example.storemanager.modules.catalog.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductRequest {

    @NotBlank(message = "Mã sản phẩm không được để trống")
    @Size(max = 50, message = "Mã sản phẩm không được quá 50 ký tự")
    private String productCode;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 150, message = "Tên sản phẩm không được quá 150 ký tự")
    private String name;

    private String description;

    @NotNull(message = "Giá bán cơ bản không được để trống")
    private BigDecimal basePrice;

    private BigDecimal costPrice;

    @Size(max = 100, message = "Thương hiệu không được quá 100 ký tự")
    private String brand;

    @Size(max = 2000, message = "Đường dẫn ảnh chính không được quá 2000 ký tự")
    private String mainImageUrl;

    @Size(max = 50, message = "Barcode không được quá 50 ký tự")
    private String barcode;

    private Boolean isActive;

    private BigDecimal weight;

    private BigDecimal reorderPoint;

    private BigDecimal minStock;

    private BigDecimal maxStock;

    private String dimensions;

    private Boolean allowNegativeStock;

    private String galleryImages;

    private String variants;

    @NotNull(message = "ID danh mục không được để trống")
    private Long categoryId;

    @NotNull(message = "ID đơn vị tính cơ bản không được để trống")
    private Long baseUnitId;

    private Boolean isSerialTracked;
    private Integer warrantyPeriodMonths;
    private String originCountry;
    private java.util.List<org.example.storemanager.modules.catalog.dto.request.productunit.ProductUnitRequest> conversionUnits;
    private org.example.storemanager.shared.enums.catalog.TaxClass taxClass;
}
