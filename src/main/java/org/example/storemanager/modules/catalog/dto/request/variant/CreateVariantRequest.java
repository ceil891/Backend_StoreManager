package org.example.storemanager.modules.catalog.dto.request.variant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.storemanager.shared.enums.catalog.VariantStrategy;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateVariantRequest {

    /**
     * ID sản phẩm cha.
     */
    @NotNull(message = "ID sản phẩm không được để trống")
    private Long productId;

    /**
     * NONE → sinh 1 variant mặc định (SP đơn giản).
     * ATTRIBUTE_BASED → sinh tổ hợp biến thể từ attributeCombinations.
     * Nếu null thì lấy theo variantStrategy đã set trên Product.
     */
    private VariantStrategy variantStrategy;

    /**
     * Dùng khi variantStrategy = ATTRIBUTE_BASED.
     * Mỗi phần tử là 1 biến thể: danh sách attributeValueId của biến thể đó.
     *
     * Ví dụ Áo Polo Size S Màu Đen:
     * [
     *   { "attributeValueIds": [1, 3] },   // Size=S, Color=Đen
     *   { "attributeValueIds": [2, 3] }    // Size=M, Color=Đen
     * ]
     */
    private List<VariantAttributeInput> attributeCombinations;

    /**
     * SKU prefix dùng để sinh SKU tự động nếu không chỉ định rõ trong từng combination.
     * Ví dụ "POLO" → POLO-S-BLACK
     */
    @Size(max = 50)
    private String skuPrefix;

    @Data
    public static class VariantAttributeInput {
        /**
         * Danh sách ID của AttributeValue cho biến thể này.
         * Ví dụ: [valueId_Size_S, valueId_Color_Black]
         */
        private List<Long> attributeValueIds;

        /**
         * SKU tùy chỉnh. Nếu null → tự sinh từ skuPrefix + giá trị thuộc tính.
         */
        private String customSku;

        /**
         * Giá override riêng cho biến thể. Nếu null → dùng basePrice của Product.
         */
        private BigDecimal price;

        private String barcode;
        private String imageUrl;
    }
}
