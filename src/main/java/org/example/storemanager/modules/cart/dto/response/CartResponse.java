package org.example.storemanager.modules.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.storemanager.shared.enums.cart.CartStatus;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private Long cartId;

    private CartStatus status;

    private List<CartItemResponse> items;

    /** Tổng số item (tính theo số dòng). */
    private int totalItems;

    /** Tổng số lượng (sum of all quantities). */
    private int totalQuantity;

    /** Tổng tiền snapshot (chỉ hiển thị – KHÔNG dùng cho checkout). */
    private BigDecimal totalAmount;
}
