package org.example.storemanager.modules.catalog.dto.request.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.storemanager.modules.catalog.dto.request.productunit.ProductUnitRequest;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductRequest {

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

    private Boolean isActive = true;

    private BigDecimal weight;

    private BigDecimal reorderPoint;

    private BigDecimal minStock;

    private BigDecimal maxStock;

    private String dimensions;

    private Boolean allowNegativeStock = false;

    private String galleryImages;

    // Structured variants list (preferred)
    @Valid
    private List<CreateVariantInput> variants;

    // Raw variants string fallback for legacy clients
    private String variantsRaw;

    @NotNull(message = "ID danh mục không được để trống")
    private Long categoryId;

    @NotNull(message = "ID đơn vị tính cơ bản không được để trống")
    private Long baseUnitId;

    private Boolean isSerialTracked = false;
    private Integer warrantyPeriodMonths;
    private String originCountry;

    private List<ProductUnitRequest> conversionUnits;
    private org.example.storemanager.shared.enums.catalog.TaxClass taxClass;

    // Top-level initial stock entries (for products with variantStrategy NONE)
    @Valid
    private List<InitialStockInput> initialStocks;

    @Data
    public static class CreateVariantInput {
        private List<Long> attributeValueIds;
        private String customSku;
        private String barcode;
        private BigDecimal price;
        private String imageUrl;
        @Valid
        private List<InitialStockInput> initialStocks;
    }

    @Data
    public static class InitialStockInput {
        @NotNull(message = "ID chi nhánh không được để trống")
        private Long branchId;

        @NotNull(message = "Số lượng không được để trống")
        @DecimalMin(value = "0", message = "Số lượng không được âm")
        private BigDecimal quantity;
    }
}

