package org.example.storemanager.modules.cart.service;

import org.example.storemanager.modules.cart.dto.request.AddCartItemRequest;
import org.example.storemanager.modules.cart.dto.request.UpdateCartItemRequest;
import org.example.storemanager.modules.cart.dto.response.CartResponse;
import org.example.storemanager.modules.cart.dto.response.CheckoutValidationResult;
import org.example.storemanager.modules.cart.entity.Cart;
import org.example.storemanager.modules.cart.entity.CartItem;
import org.example.storemanager.modules.cart.repository.CartItemRepository;
import org.example.storemanager.modules.cart.repository.CartRepository;
import org.example.storemanager.modules.cart.service.impl.CartServiceImpl;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.catalog.entity.ProductVariant;
import org.example.storemanager.modules.catalog.repository.ProductVariantRepository;
import org.example.storemanager.modules.system.entity.User;
import org.example.storemanager.modules.system.repository.UserRepository;
import org.example.storemanager.shared.enums.cart.CartStatus;
import org.example.storemanager.shared.enums.catalog.VariantStatus;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Unit Tests")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User sampleUser;
    private Product sampleProduct;
    private ProductVariant sampleVariant;
    private Cart sampleCart;
    private CartItem sampleCartItem;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .username("user1")
                .fullName("User Test")
                .build();
        sampleUser.setId(1L);

        sampleProduct = Product.builder()
                .name("Áo Thun Nam Cotton")
                .productCode("PRD001")
                .basePrice(new BigDecimal("199000"))
                .mainImageUrl("https://example.com/ao-thun.jpg")
                .build();
        sampleProduct.setId(10L);

        sampleVariant = ProductVariant.builder()
                .variantCode("VAR001")
                .sku("ATN-L-BLACK")
                .price(new BigDecimal("199000"))
                .status(VariantStatus.ACTIVE)
                .isActive(true)
                .product(sampleProduct)
                .build();
        sampleVariant.setId(20L);

        sampleCart = Cart.builder()
                .user(sampleUser)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();
        sampleCart.setId(100L);

        sampleCartItem = CartItem.builder()
                .cart(sampleCart)
                .productVariantId(sampleVariant.getId())
                .productName(sampleProduct.getName())
                .sku(sampleVariant.getSku())
                .unitPrice(sampleVariant.getPrice())
                .quantity(2)
                .build();
        sampleCartItem.setId(1001L);
    }

    @Nested
    @DisplayName("1. Lấy thông tin Giỏ hàng (Get Cart)")
    class GetCartTests {

        @Test
        @DisplayName("Lấy giỏ hàng rỗng khi chưa có Cart nào được tạo")
        void getCart_EmptyWhenNotFound() {
            when(cartRepository.findByUserIdAndStatus(1L, CartStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            CartResponse response = cartService.getCart(1L, null);

            assertThat(response).isNotNull();
            assertThat(response.getTotalItems()).isZero();
            assertThat(response.getItems()).isEmpty();
        }

        @Test
        @DisplayName("Lấy giỏ hàng có chứa sản phẩm của user")
        void getCart_UserWithItems_Success() {
            when(cartRepository.findByUserIdAndStatus(1L, CartStatus.ACTIVE))
                    .thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findByCartId(100L))
                    .thenReturn(List.of(sampleCartItem));

            CartResponse response = cartService.getCart(1L, null);

            assertThat(response).isNotNull();
            assertThat(response.getCartId()).isEqualTo(100L);
            assertThat(response.getTotalItems()).isEqualTo(1);
            assertThat(response.getTotalQuantity()).isEqualTo(2);
            assertThat(response.getItems()).hasSize(1);
            assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("398000"));
        }
    }

    @Nested
    @DisplayName("2. Thêm sản phẩm vào giỏ (Add Item)")
    class AddItemTests {

        @Test
        @DisplayName("Thêm mới một sản phẩm chưa có trong giỏ hàng")
        void addItem_NewItem_Success() {
            AddCartItemRequest req = new AddCartItemRequest();
            req.setProductVariantId(sampleVariant.getId());
            req.setQuantity(1);

            when(productVariantRepository.findById(sampleVariant.getId()))
                    .thenReturn(Optional.of(sampleVariant));
            when(cartRepository.findByUserIdAndStatus(1L, CartStatus.ACTIVE))
                    .thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findByCartIdAndProductVariantId(100L, sampleVariant.getId()))
                    .thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(sampleCartItem);
            when(cartRepository.findById(100L)).thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findByCartId(100L)).thenReturn(List.of(sampleCartItem));

            CartResponse res = cartService.addItem(1L, null, req);

            assertThat(res).isNotNull();
            verify(cartItemRepository, times(1)).save(any(CartItem.class));
        }

        @Test
        @DisplayName("Thêm sản phẩm đã có sẵn trong giỏ -> Cộng dồn số lượng")
        void addItem_ExistingItem_IncrementsQuantity() {
            AddCartItemRequest req = new AddCartItemRequest();
            req.setProductVariantId(sampleVariant.getId());
            req.setQuantity(3);

            when(productVariantRepository.findById(sampleVariant.getId()))
                    .thenReturn(Optional.of(sampleVariant));
            when(cartRepository.findByUserIdAndStatus(1L, CartStatus.ACTIVE))
                    .thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findByCartIdAndProductVariantId(100L, sampleVariant.getId()))
                    .thenReturn(Optional.of(sampleCartItem));
            when(cartRepository.findById(100L)).thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findByCartId(100L)).thenReturn(List.of(sampleCartItem));

            CartResponse res = cartService.addItem(1L, null, req);

            assertThat(res).isNotNull();
            assertThat(sampleCartItem.getQuantity()).isEqualTo(5); // 2 + 3 = 5
            verify(cartItemRepository, times(1)).save(sampleCartItem);
        }

        @Test
        @DisplayName("Thêm sản phẩm không tồn tại -> Báo lỗi ResourceNotFoundException")
        void addItem_VariantNotFound_ThrowsException() {
            AddCartItemRequest req = new AddCartItemRequest();
            req.setProductVariantId(999L);
            req.setQuantity(1);

            when(productVariantRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> cartService.addItem(1L, null, req));
        }
    }

    @Nested
    @DisplayName("3. Cập nhật & Xóa sản phẩm (Update & Remove)")
    class UpdateRemoveTests {

        @Test
        @DisplayName("Cập nhật số lượng sản phẩm trong giỏ")
        void updateItem_Success() {
            UpdateCartItemRequest req = new UpdateCartItemRequest();
            req.setQuantity(5);

            when(cartItemRepository.findById(1001L))
                    .thenReturn(Optional.of(sampleCartItem));
            when(cartRepository.findById(100L))
                    .thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findByCartId(100L))
                    .thenReturn(List.of(sampleCartItem));

            CartResponse res = cartService.updateItem(1L, null, 1001L, req);

            assertThat(res).isNotNull();
            assertThat(sampleCartItem.getQuantity()).isEqualTo(5);
            verify(cartItemRepository, times(1)).save(sampleCartItem);
        }

        @Test
        @DisplayName("Cập nhật số lượng về 0 -> Tự động xóa khỏi giỏ")
        void updateItem_QuantityZero_DeletesItem() {
            UpdateCartItemRequest req = new UpdateCartItemRequest();
            req.setQuantity(0);

            when(cartItemRepository.findById(1001L))
                    .thenReturn(Optional.of(sampleCartItem));
            when(cartRepository.findById(100L))
                    .thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findByCartId(100L))
                    .thenReturn(List.of());

            CartResponse res = cartService.updateItem(1L, null, 1001L, req);

            assertThat(res).isNotNull();
            verify(cartItemRepository, times(1)).delete(sampleCartItem);
        }

        @Test
        @DisplayName("Xóa hoàn toàn giỏ hàng (Clear Cart)")
        void clearCart_Success() {
            when(cartRepository.findByUserIdAndStatus(1L, CartStatus.ACTIVE))
                    .thenReturn(Optional.of(sampleCart));

            CartResponse res = cartService.clearCart(1L, null);

            assertThat(res).isNotNull();
            verify(cartItemRepository, times(1)).deleteByCartId(100L);
        }
    }

    @Nested
    @DisplayName("4. Kiểm tra trước khi thanh toán (Checkout Validation)")
    class CheckoutValidationTests {

        @Test
        @DisplayName("Validate giỏ hàng thành công khi tất cả sản phẩm đều active")
        void validateForCheckout_Success() {
            when(cartRepository.findByUserIdAndStatus(1L, CartStatus.ACTIVE))
                    .thenReturn(Optional.of(sampleCart));
            when(cartItemRepository.findByCartId(100L))
                    .thenReturn(List.of(sampleCartItem));
            when(productVariantRepository.findById(sampleVariant.getId()))
                    .thenReturn(Optional.of(sampleVariant));

            CheckoutValidationResult result = cartService.validateForCheckout(1L);

            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
            assertThat(result.getUnavailableVariantIds()).isEmpty();
        }
    }
}
