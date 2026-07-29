package org.example.storemanager.modules.catalog.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.catalog.entity.ProductReview;
import org.example.storemanager.modules.catalog.repository.ProductReviewRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductReviewController {

    private final ProductReviewRepository reviewRepository;

    @GetMapping
    public ResponseEntity<List<ProductReview>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewRepository.findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(productId));
    }

    @PostMapping
    public ResponseEntity<ProductReview> addProductReview(@PathVariable Long productId, @RequestBody ProductReview review) {
        review.setProductId(productId);
        ProductReview saved = reviewRepository.save(review);
        return ResponseEntity.ok(saved);
    }
}
