package org.example.storemanager.modules.marketing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.partnerarea.entity.Customer;
import org.example.storemanager.modules.catalog.entity.Product;
import java.time.LocalDateTime;
import org.example.storemanager.modules.system.entity.User;

@Entity
@Table(name = "customer_feedbacks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class CustomerFeedback extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    @Column(nullable = false)
    private Integer rating; // Số sao đánh giá từ 1 đến 5

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false, length = 30)
    private String status; // PENDING (Chờ duyệt), APPROVED (Hiển thị), REJECTED (Ẩn)

    @Column(length = 200)
    private String title; // Tiêu đề đánh giá

    @Column(name = "image_url", length = 500)
    private String imageUrl; // Hình ảnh

    @Column(columnDefinition = "TEXT")
    private String reply; // Phản hồi của cửa hàng

    @Column(name = "replied_at")
    private LocalDateTime repliedAt; // Thời gian phản hồi

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replied_by")
    private User repliedBy; // Nhân viên phản hồi

    @Builder.Default
    @Column(name = "is_verified_purchase", columnDefinition = "boolean default false")
    private Boolean isVerifiedPurchase = false; // Đã mua hàng hay chưa

    @Builder.Default
    @Column(name = "helpful_count", columnDefinition = "integer default 0")
    private Integer helpfulCount = 0; // Lượt đánh giá hữu ích
}