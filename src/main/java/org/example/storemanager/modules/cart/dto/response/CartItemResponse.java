package org.example.storemanager.modules.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private Long itemId;

    private Long variantId;

    // Snapshot fields – hiển thị đúng như lúc thêm vào giỏ
    private String productName;
    private String variantName;
    private String sku;
    private String thumbnail;

    /** Giá snapshot (chỉ để hiển thị – KHÔNG dùng cho checkout). */
    private BigDecimal unitPrice;

    private Integer quantity;

    /** Tổng tiền = unitPrice × quantity (tính tạm để hiển thị). */
    private BigDecimal subtotal;
}
