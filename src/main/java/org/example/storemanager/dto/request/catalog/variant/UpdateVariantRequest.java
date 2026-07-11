package org.example.storemanager.dto.request.catalog.variant;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateVariantRequest {

    /**
     * Barcode mới (không bắt buộc).
     */
    @Size(max = 100)
    private String barcode;

    /**
     * URL ảnh riêng của biến thể.
     */
    @Size(max = 500)
    private String imageUrl;

    /**
     * Giá override riêng. Nếu null thì biến thể dùng basePrice của Product.
     */
    private BigDecimal price;
}
