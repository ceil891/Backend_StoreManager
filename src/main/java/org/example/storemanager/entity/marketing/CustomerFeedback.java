package org.example.storemanager.entity.marketing;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.partnerarea.Customer;
import org.example.storemanager.entity.catalog.Product;

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
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer rating; // Số sao đánh giá từ 1 đến 5

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false, length = 30)
    private String status; // PENDING (Chờ duyệt), APPROVED (Hiển thị), REJECTED (Ẩn)
}