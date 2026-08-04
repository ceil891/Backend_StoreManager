package org.example.storemanager.modules.cart.service;

import org.example.storemanager.modules.cart.dto.request.AddCartItemRequest;
import org.example.storemanager.modules.cart.dto.request.UpdateCartItemRequest;
import org.example.storemanager.modules.cart.dto.response.CartResponse;
import org.example.storemanager.modules.cart.dto.response.CheckoutValidationResult;

public interface CartService {

    /**
     * Lấy giỏ hàng hiện tại.
     * @param userId null nếu là guest
     * @param guestToken null nếu là user đã đăng nhập
     */
    CartResponse getCart(Long userId, String guestToken);

    /**
     * Thêm sản phẩm vào giỏ.
     * Backend validate: variant active, quantity hợp lệ.
     * Nếu variant đã tồn tại → cộng thêm quantity.
     */
    CartResponse addItem(Long userId, String guestToken, AddCartItemRequest request);

    /**
     * Cập nhật số lượng.
     * quantity = 0 → xóa item.
     */
    CartResponse updateItem(Long userId, String guestToken, Long itemId, UpdateCartItemRequest request);

    /**
     * Xóa 1 sản phẩm khỏi giỏ hàng.
     */
    CartResponse removeItem(Long userId, String guestToken, Long itemId);

    /**
     * Xóa toàn bộ giỏ hàng.
     */
    CartResponse clearCart(Long userId, String guestToken);

    /**
     * Merge guest cart vào user cart sau khi đăng nhập.
     * Guest items được cộng quantity vào user cart.
     * Guest cart status → MERGED.
     */
    CartResponse mergeGuestCart(Long userId, String guestToken);

    /**
     * Validate giỏ hàng trước checkout.
     * Load lại giá thực từ variant, kiểm tra variant còn bán.
     */
    CheckoutValidationResult validateForCheckout(Long userId);

    /**
     * Mark cart là ORDERED sau khi tạo SaleOrder thành công.
     */
    void markOrdered(Long cartId);
}
