package org.example.storemanager.modules.catalog.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.catalog.entity.ProductReview;
import org.example.storemanager.modules.catalog.repository.ProductRepository;
import org.example.storemanager.modules.catalog.repository.ProductReviewRepository;
import org.example.storemanager.shared.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductReviewController {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    @Transactional
    public synchronized void checkAndSeedSampleReviews() {
        if (reviewRepository.count() == 0) {
            List<Product> products = productRepository.findAll();
            Long pId1 = products.isEmpty() ? 1L : products.get(0).getId();
            Long pId2 = products.size() > 1 ? products.get(1).getId() : pId1;

            ProductReview r1 = ProductReview.builder()
                    .productId(pId1)
                    .customerId(1L)
                    .customerName("Nguyễn Lưu Hưng")
                    .rating(5)
                    .comment("Sản phẩm tuyệt vời, đóng gói rất kỹ lưỡng và giao hàng siêu nhanh. Sẽ ủng hộ shop dài dài!")
                    .isApproved(true)
                    .createdAt(LocalDateTime.now().minusDays(2))
                    .build();
            reviewRepository.save(r1);

            ProductReview r2 = ProductReview.builder()
                    .productId(pId1)
                    .customerId(2L)
                    .customerName("Trần Hoàng Nam")
                    .rating(5)
                    .comment("Chất lượng đúng mô tả, dùng rất thích. 10/10 điểm cho chất lượng và dịch vụ!")
                    .isApproved(true)
                    .createdAt(LocalDateTime.now().minusDays(5))
                    .build();
            reviewRepository.save(r2);

            ProductReview r3 = ProductReview.builder()
                    .productId(pId2)
                    .customerId(1L)
                    .customerName("Nguyễn Lưu Hưng")
                    .rating(4)
                    .comment("Hàng chuẩn chính hãng, dùng mượt mà. Giao hàng 2 ngày là nhận được.")
                    .isApproved(true)
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build();
            reviewRepository.save(r3);
        }
    }

    private Map<String, Object> enrichReview(ProductReview review) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", review.getId());
        map.put("productId", review.getProductId());
        map.put("customerId", review.getCustomerId());
        map.put("customerName", review.getCustomerName() != null ? review.getCustomerName() : "Khách hàng");
        map.put("rating", review.getRating());
        map.put("comment", review.getComment() != null ? review.getComment() : "");
        map.put("isApproved", review.getIsApproved() != null ? review.getIsApproved() : true);
        map.put("createdAt", review.getCreatedAt() != null ? review.getCreatedAt().toString() : LocalDateTime.now().toString());

        // Enrich with product details
        if (review.getProductId() != null) {
            Product product = productRepository.findById(review.getProductId()).orElse(null);
            if (product != null) {
                map.put("productName", product.getName());
                map.put("productCode", product.getProductCode());
                map.put("productImage", product.getMainImageUrl());
                map.put("productPrice", product.getBasePrice());
            }
        }
        return map;
    }

    // 1. GET /api/v1/products/{productId}/reviews - Lấy đánh giá đã duyệt của 1 sản phẩm
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getProductReviews(@PathVariable Long productId) {
        checkAndSeedSampleReviews();
        List<ProductReview> reviews = reviewRepository.findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(productId);
        List<Map<String, Object>> result = reviews.stream().map(this::enrichReview).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách đánh giá sản phẩm thành công"));
    }

    // 2. POST /api/v1/products/{productId}/reviews - Khách hàng gửi đánh giá sản phẩm
    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addProductReview(
            @PathVariable Long productId,
            @RequestBody ProductReview review) {
        review.setProductId(productId);
        if (review.getRating() == null) review.setRating(5);
        if (review.getIsApproved() == null) review.setIsApproved(true);
        if (review.getCustomerName() == null || review.getCustomerName().isBlank()) {
            review.setCustomerName("Khách hàng");
        }
        ProductReview saved = reviewRepository.save(review);
        return ResponseEntity.status(201).body(ApiResponse.success(enrichReview(saved), "Đánh giá của bạn đã được ghi nhận!"));
    }

    // 3. GET /api/v1/reviews/customer - Lấy tất cả đánh giá của 1 khách hàng (cho FE_WebOnline ProfilePage)
    @GetMapping("/reviews/customer")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCustomerReviews(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String customerName) {
        checkAndSeedSampleReviews();
        List<ProductReview> all = reviewRepository.findAllByOrderByCreatedAtDesc();
        List<ProductReview> filtered = all.stream().filter(r -> {
            if (customerId != null && customerId.equals(r.getCustomerId())) return true;
            if (customerName != null && !customerName.isBlank() && r.getCustomerName() != null) {
                return r.getCustomerName().trim().equalsIgnoreCase(customerName.trim());
            }
            return false;
        }).collect(Collectors.toList());

        List<Map<String, Object>> result = filtered.stream().map(this::enrichReview).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy lịch sử đánh giá của khách hàng thành công"));
    }

    // 4. GET /api/v1/reviews - Lấy toàn bộ đánh giá sản phẩm trong hệ thống (cho Admin trên RetailHub)
    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllReviews() {
        checkAndSeedSampleReviews();
        List<ProductReview> reviews = reviewRepository.findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> result = reviews.stream().map(this::enrichReview).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy toàn bộ đánh giá sản phẩm thành công"));
    }

    // 5. PATCH /api/v1/reviews/{id}/approve - Admin duyệt / ẩn đánh giá
    @PatchMapping("/reviews/{id}/approve")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleApproveReview(
            @PathVariable Long id,
            @RequestParam Boolean isApproved) {
        ProductReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá ID: " + id));
        review.setIsApproved(isApproved);
        ProductReview saved = reviewRepository.save(review);
        return ResponseEntity.ok(ApiResponse.success(enrichReview(saved), "Cập nhật trạng thái duyệt thành công"));
    }

    // 6. DELETE /api/v1/reviews/{id} - Admin xóa đánh giá
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa đánh giá thành công"));
    }
}
