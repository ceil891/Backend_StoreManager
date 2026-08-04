package org.example.storemanager.modules.cart.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.cart.dto.request.AddCartItemRequest;
import org.example.storemanager.modules.cart.dto.request.UpdateCartItemRequest;
import org.example.storemanager.modules.cart.dto.response.CartResponse;
import org.example.storemanager.modules.cart.dto.response.CheckoutValidationResult;
import org.example.storemanager.modules.cart.service.CartService;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.system.repository.UserRepository;
import org.example.storemanager.shared.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * CartController – phân biệt Guest / User qua JWT hoặc Guest-Token header.
 *
 * Logic giải quyết danh tính:
 *   1. Nếu request có Bearer JWT hợp lệ → lấy userId từ JWT.
 *   2. Nếu không có JWT → đọc header "Guest-Token".
 *   3. Nếu Guest-Token rỗng → backend sinh UUID mới, gửi lại trong response header "Guest-Token".
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // ─── GET /api/v1/cart ──────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            HttpServletRequest req, HttpServletResponse res) {
        CartContext ctx = resolveContext(req, res);
        CartResponse cart = cartService.getCart(ctx.userId, ctx.guestToken);
        return ResponseEntity.ok(ApiResponse.ok(cart));
    }

    // ─── POST /api/v1/cart/items ───────────────────────
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @Valid @RequestBody AddCartItemRequest request,
            HttpServletRequest req, HttpServletResponse res) {
        CartContext ctx = resolveContext(req, res);
        CartResponse cart = cartService.addItem(ctx.userId, ctx.guestToken, request);
        return ResponseEntity.ok(ApiResponse.ok("Đã thêm sản phẩm vào giỏ hàng", cart));
    }

    // ─── PUT /api/v1/cart/items/{itemId} ───────────────
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            HttpServletRequest req, HttpServletResponse res) {
        CartContext ctx = resolveContext(req, res);
        CartResponse cart = cartService.updateItem(ctx.userId, ctx.guestToken, itemId, request);
        return ResponseEntity.ok(ApiResponse.ok("Đã cập nhật giỏ hàng", cart));
    }

    // ─── DELETE /api/v1/cart/items/{itemId} ────────────
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable Long itemId,
            HttpServletRequest req, HttpServletResponse res) {
        CartContext ctx = resolveContext(req, res);
        CartResponse cart = cartService.removeItem(ctx.userId, ctx.guestToken, itemId);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa sản phẩm khỏi giỏ hàng", cart));
    }

    // ─── DELETE /api/v1/cart ───────────────────────────
    @DeleteMapping
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(
            HttpServletRequest req, HttpServletResponse res) {
        CartContext ctx = resolveContext(req, res);
        CartResponse cart = cartService.clearCart(ctx.userId, ctx.guestToken);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa toàn bộ giỏ hàng", cart));
    }

    // ─── POST /api/v1/cart/merge ───────────────────────
    /**
     * Merge guest cart vào user cart.
     * Yêu cầu: Bearer JWT (bắt buộc) + Guest-Token header.
     * Gọi ngay sau khi đăng nhập thành công từ frontend.
     */
    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<CartResponse>> merge(
            HttpServletRequest req, HttpServletResponse res) {
        // Bắt buộc phải có JWT để merge
        Long userId = extractUserId(req);
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "Cần đăng nhập để merge giỏ hàng"));
        }
        String guestToken = req.getHeader("Guest-Token");
        CartResponse cart = cartService.mergeGuestCart(userId, guestToken);
        return ResponseEntity.ok(ApiResponse.ok("Đã gộp giỏ hàng thành công", cart));
    }

    // ─── GET /api/v1/cart/validate ─────────────────────
    /**
     * Validate giỏ hàng trước checkout (kiểm tra giá, variant còn bán).
     * Yêu cầu: Bearer JWT.
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<CheckoutValidationResult>> validateCheckout(
            HttpServletRequest req) {
        Long userId = extractUserId(req);
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "Cần đăng nhập để checkout"));
        }
        CheckoutValidationResult result = cartService.validateForCheckout(userId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ─── PRIVATE HELPERS ───────────────────────────────

    /**
     * Giải quyết danh tính người dùng từ request.
     * Tự động sinh Guest-Token mới nếu chưa có và gắn vào response header.
     */
    private CartContext resolveContext(HttpServletRequest req, HttpServletResponse res) {
        Long userId = extractUserId(req);
        if (userId != null) {
            return new CartContext(userId, null);
        }

        String guestToken = req.getHeader("Guest-Token");
        if (guestToken == null || guestToken.isBlank()) {
            guestToken = generateSecureToken();
            // Trả token mới về cho frontend lưu vào localStorage
            res.setHeader("Guest-Token", guestToken);
            res.setHeader("Access-Control-Expose-Headers", "Guest-Token");
        }
        return new CartContext(null, guestToken);
    }

    private Long extractUserId(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isTokenValid(token)) {
                String username = jwtUtil.extractUsername(token);
                return userRepository.findByUsername(username)
                        .map(u -> u.getId())
                        .orElse(null);
            }
        }
        return null;
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16]; // 128-bit
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Value object chứa danh tính đã giải quyết. */
    private record CartContext(Long userId, String guestToken) {}
}
