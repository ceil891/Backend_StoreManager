package org.example.storemanager.modules.cart.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.modules.cart.dto.request.AddCartItemRequest;
import org.example.storemanager.modules.cart.dto.request.UpdateCartItemRequest;
import org.example.storemanager.modules.cart.dto.response.CartItemResponse;
import org.example.storemanager.modules.cart.dto.response.CartResponse;
import org.example.storemanager.modules.cart.dto.response.CheckoutValidationResult;
import org.example.storemanager.modules.cart.dto.response.PriceChangeAlert;
import org.example.storemanager.modules.cart.entity.Cart;
import org.example.storemanager.modules.cart.entity.CartItem;
import org.example.storemanager.modules.cart.repository.CartItemRepository;
import org.example.storemanager.modules.cart.repository.CartRepository;
import org.example.storemanager.modules.cart.service.CartService;
import org.example.storemanager.modules.catalog.entity.ProductVariant;
import org.example.storemanager.modules.catalog.repository.ProductVariantRepository;
import org.example.storemanager.modules.system.entity.User;
import org.example.storemanager.modules.system.repository.UserRepository;
import org.example.storemanager.shared.enums.cart.CartStatus;
import org.example.storemanager.shared.enums.ErrorCode;
import org.example.storemanager.shared.exception.BusinessException;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private static final int MAX_QUANTITY = 999;
    private static final int GUEST_CART_TTL_DAYS = 30;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    // ────────────────────────────────────────────────
    // GET CART
    // ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId, String guestToken) {
        Optional<Cart> cartOpt = resolveExistingCart(userId, guestToken);
        if (cartOpt.isEmpty()) {
            return emptyCartResponse();
        }
        return toCartResponse(cartOpt.get());
    }

    // ────────────────────────────────────────────────
    // ADD ITEM
    // ────────────────────────────────────────────────

    @Override
    public CartResponse addItem(Long userId, String guestToken, AddCartItemRequest request) {
        // 1. Validate variant
        ProductVariant variant = validateVariant(request.getProductVariantId());

        // 2. Lấy hoặc tạo cart
        Cart cart = getOrCreateCart(userId, guestToken);

        // 3. Kiểm tra item đã tồn tại chưa
        Optional<CartItem> existing = cartItemRepository
                .findByCartIdAndProductVariantId(cart.getId(), variant.getId());

        if (existing.isPresent()) {
            // Cộng thêm quantity
            CartItem item = existing.get();
            int newQty = item.getQuantity() + request.getQuantity();
            if (newQty > MAX_QUANTITY) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "Số lượng tối đa cho mỗi sản phẩm là " + MAX_QUANTITY);
            }
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            // Thêm item mới với snapshot data
            String productName = variant.getProduct() != null ? variant.getProduct().getName() : "Sản phẩm";
            String variantName = buildVariantName(variant);
            String thumbnail = variant.getImageUrl() != null
                    ? variant.getImageUrl()
                    : (variant.getProduct() != null ? variant.getProduct().getMainImageUrl() : null);
            BigDecimal price = variant.getPrice() != null
                    ? variant.getPrice()
                    : (variant.getProduct() != null ? variant.getProduct().getBasePrice() : BigDecimal.ZERO);

            CartItem item = CartItem.builder()
                    .cart(cart)
                    .productVariantId(variant.getId())
                    .productName(productName)
                    .variantName(variantName)
                    .sku(variant.getSku())
                    .thumbnail(thumbnail)
                    .unitPrice(price)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(item);
        }

        return toCartResponse(cartRepository.findById(cart.getId()).orElse(cart));
    }

    // ────────────────────────────────────────────────
    // UPDATE ITEM
    // ────────────────────────────────────────────────

    @Override
    public CartResponse updateItem(Long userId, String guestToken, Long itemId, UpdateCartItemRequest request) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        // Security: đảm bảo item thuộc về cart của user/guest này
        Cart cart = item.getCart();
        verifyCartOwnership(cart, userId, guestToken);

        if (request.getQuantity() == 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(request.getQuantity());
            cartItemRepository.save(item);
        }

        return toCartResponse(cartRepository.findById(cart.getId()).orElse(cart));
    }

    // ────────────────────────────────────────────────
    // REMOVE ITEM
    // ────────────────────────────────────────────────

    @Override
    public CartResponse removeItem(Long userId, String guestToken, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        Cart cart = item.getCart();
        verifyCartOwnership(cart, userId, guestToken);

        cartItemRepository.delete(item);
        return toCartResponse(cartRepository.findById(cart.getId()).orElse(cart));
    }

    // ────────────────────────────────────────────────
    // CLEAR CART
    // ────────────────────────────────────────────────

    @Override
    public CartResponse clearCart(Long userId, String guestToken) {
        Optional<Cart> cartOpt = resolveExistingCart(userId, guestToken);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cartItemRepository.deleteByCartId(cart.getId());
            cart.getItems().clear();
            cartRepository.save(cart);
        }
        return emptyCartResponse();
    }

    // ────────────────────────────────────────────────
    // MERGE GUEST → USER
    // ────────────────────────────────────────────────

    @Override
    public CartResponse mergeGuestCart(Long userId, String guestToken) {
        if (guestToken == null || guestToken.isBlank()) {
            log.debug("mergeGuestCart: guestToken rỗng, bỏ qua merge.");
            return getCart(userId, null);
        }

        Optional<Cart> guestCartOpt = cartRepository
                .findByGuestTokenAndStatus(guestToken, CartStatus.ACTIVE);

        if (guestCartOpt.isEmpty()) {
            log.debug("mergeGuestCart: không tìm thấy guest cart với token {}", guestToken);
            return getCart(userId, null);
        }

        Cart guestCart = guestCartOpt.get();
        Cart userCart = getOrCreateCart(userId, null);

        List<CartItem> guestItems = cartItemRepository.findByCartId(guestCart.getId());

        for (CartItem guestItem : guestItems) {
            Optional<CartItem> existingUserItem = cartItemRepository
                    .findByCartIdAndProductVariantId(userCart.getId(), guestItem.getProductVariantId());

            if (existingUserItem.isPresent()) {
                // Cộng quantity
                CartItem userItem = existingUserItem.get();
                int merged = Math.min(userItem.getQuantity() + guestItem.getQuantity(), MAX_QUANTITY);
                userItem.setQuantity(merged);
                cartItemRepository.save(userItem);
            } else {
                // Di chuyển item sang user cart
                CartItem newItem = CartItem.builder()
                        .cart(userCart)
                        .productVariantId(guestItem.getProductVariantId())
                        .productName(guestItem.getProductName())
                        .variantName(guestItem.getVariantName())
                        .sku(guestItem.getSku())
                        .thumbnail(guestItem.getThumbnail())
                        .unitPrice(guestItem.getUnitPrice())
                        .quantity(guestItem.getQuantity())
                        .build();
                cartItemRepository.save(newItem);
            }
        }

        // Mark guest cart MERGED
        guestCart.setStatus(CartStatus.MERGED);
        cartRepository.save(guestCart);

        log.info("mergeGuestCart: {} items merged từ guest {} sang user {}", guestItems.size(), guestToken, userId);
        return toCartResponse(cartRepository.findById(userCart.getId()).orElse(userCart));
    }

    // ────────────────────────────────────────────────
    // VALIDATE FOR CHECKOUT
    // ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public CheckoutValidationResult validateForCheckout(Long userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE);
        if (cartOpt.isEmpty()) {
            return CheckoutValidationResult.ok();
        }

        Cart cart = cartOpt.get();
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        List<PriceChangeAlert> priceChanges = new ArrayList<>();
        List<Long> unavailableVariantIds = new ArrayList<>();

        for (CartItem item : items) {
            Optional<ProductVariant> variantOpt = productVariantRepository.findById(item.getProductVariantId());

            if (variantOpt.isEmpty() || !Boolean.TRUE.equals(variantOpt.get().getIsActive())) {
                unavailableVariantIds.add(item.getProductVariantId());
                continue;
            }

            ProductVariant current = variantOpt.get();
            BigDecimal currentPrice = current.getPrice() != null
                    ? current.getPrice()
                    : (current.getProduct() != null ? current.getProduct().getBasePrice() : BigDecimal.ZERO);

            // So sánh giá thực với snapshot
            if (currentPrice.compareTo(item.getUnitPrice()) != 0) {
                priceChanges.add(PriceChangeAlert.builder()
                        .variantId(current.getId())
                        .productName(item.getProductName())
                        .variantName(item.getVariantName())
                        .sku(item.getSku())
                        .cartPrice(item.getUnitPrice())
                        .currentPrice(currentPrice)
                        .priceDiff(currentPrice.subtract(item.getUnitPrice()))
                        .build());
            }
        }

        boolean isValid = unavailableVariantIds.isEmpty();
        return CheckoutValidationResult.builder()
                .valid(isValid)
                .priceChanges(priceChanges)
                .unavailableVariantIds(unavailableVariantIds)
                .outOfStockVariantIds(List.of())
                .build();
    }

    // ────────────────────────────────────────────────
    // MARK ORDERED
    // ────────────────────────────────────────────────

    @Override
    public void markOrdered(Long cartId) {
        cartRepository.findById(cartId).ifPresent(cart -> {
            cart.setStatus(CartStatus.ORDERED);
            cartRepository.save(cart);
        });
    }

    // ────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ────────────────────────────────────────────────

    private Cart getOrCreateCart(Long userId, String guestToken) {
        if (userId != null) {
            return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                    .orElseGet(() -> createUserCart(userId));
        } else {
            if (guestToken != null && !guestToken.isBlank()) {
                return cartRepository.findByGuestTokenAndStatus(guestToken, CartStatus.ACTIVE)
                        .orElseGet(() -> createGuestCart(guestToken));
            } else {
                String newToken = generateSecureToken();
                return createGuestCart(newToken);
            }
        }
    }

    private Optional<Cart> resolveExistingCart(Long userId, String guestToken) {
        if (userId != null) {
            return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE);
        } else if (guestToken != null && !guestToken.isBlank()) {
            return cartRepository.findByGuestTokenAndStatus(guestToken, CartStatus.ACTIVE);
        }
        return Optional.empty();
    }

    private Cart createUserCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Cart cart = Cart.builder()
                .user(user)
                .status(CartStatus.ACTIVE)
                .build();
        return cartRepository.save(cart);
    }

    private Cart createGuestCart(String token) {
        Cart cart = Cart.builder()
                .guestToken(token)
                .status(CartStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusDays(GUEST_CART_TTL_DAYS))
                .build();
        return cartRepository.save(cart);
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16]; // 128-bit
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private ProductVariant validateVariant(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", variantId));
        if (!Boolean.TRUE.equals(variant.getIsActive())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Sản phẩm này hiện không còn bán");
        }
        return variant;
    }

    private void verifyCartOwnership(Cart cart, Long userId, String guestToken) {
        boolean owned = false;
        if (userId != null && cart.getUser() != null) {
            owned = cart.getUser().getId().equals(userId);
        } else if (guestToken != null) {
            owned = guestToken.equals(cart.getGuestToken());
        }
        if (!owned) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Bạn không có quyền thao tác với giỏ hàng này");
        }
    }

    private String buildVariantName(ProductVariant variant) {
        // Có thể mở rộng để ghép các attribute (màu, size...)
        return variant.getVariantCode();
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        List<CartItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalQuantity = items.stream().mapToInt(CartItem::getQuantity).sum();

        return CartResponse.builder()
                .cartId(cart.getId())
                .status(cart.getStatus())
                .items(itemResponses)
                .totalItems(itemResponses.size())
                .totalQuantity(totalQuantity)
                .totalAmount(totalAmount)
                .build();
    }

    private CartItemResponse toItemResponse(CartItem item) {
        BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return CartItemResponse.builder()
                .itemId(item.getId())
                .variantId(item.getProductVariantId())
                .productName(item.getProductName())
                .variantName(item.getVariantName())
                .sku(item.getSku())
                .thumbnail(item.getThumbnail())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .build();
    }

    private CartResponse emptyCartResponse() {
        return CartResponse.builder()
                .cartId(null)
                .status(CartStatus.ACTIVE)
                .items(List.of())
                .totalItems(0)
                .totalQuantity(0)
                .totalAmount(BigDecimal.ZERO)
                .build();
    }
}
